var MINI = require('minified');
var $ = MINI.$, $$ = MINI.$$, EE = MINI.EE;
var divFactory = EE('div');
var highlighted = {};
$(function () {
   $.ready(function () {
      bindLinks();
   });
});

function bindLinks() {
   $('.ajaxable').on('click', ajaxClickHandler);
}

function ajaxClickHandler() {
   var thisLink = $$(this);
   var parentTD = thisLink.parentNode.parentNode;
   $(parentTD).set("innerHTML", "&nbsp;<img align='middle' src='/images/loading-3.gif' border='0' />");

   $.request('get', thisLink.href).then(
      function success(content) {
         var divHighlightedAjax = divFactory()[0];
         $(divHighlightedAjax).set('innerHTML', "<pre><code>" + content.replace(/^\s+|\s+$/g, '') + "</code></pre>");
         displayPopupWithContent(thisLink, $(parentTD), $(divHighlightedAjax).get('innerHTML'));
         $('pre code').each(function (item, index) {
         if (typeof highlighted[index] === "undefined") {
            hljs.highlightBlock(item)
            highlighted[index] = true;
         }
      });
      }, function error(status, statusText, responseText) {
         var status = parseInt(status);
         if (status === 0) {
            alert("Could not contact the stubby4j backend when fetching resource:\n" + thisLink + "\n\nIs stubby4j app UP?");
         } else {
            alert("Error fetching resource:\n" + thisLink + "\n\nstatus: " + status + "\nstatusText: " + statusText + "\nresponseText: " + responseText);
         }
      });
   return false;
}

function displayPopupWithContent(thisLink, parentTD, ajaxContent) {
   var mask = divFactory()[0];
   $(mask).set({'@id': 'popup-mask'});
   $('body').add(mask);

   var popup = divFactory()[0];
   $('body').add(popup);

   var body = document.body;
   var html = document.documentElement;
   var maskHeight = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
   var maskWidth = Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth);
   $(mask).set({
      $display: 'block',
      $opacity: '0.3',
      $width: maskWidth + 'px',
      $height: maskHeight + 'px'
   });

   var content = "<div id='inner-dialog'><b>" + ajaxContent + "</b></div><br />";
   var id = Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3);
   $(popup).set({'@id': id, $: '+popup-dialog +popup-window'});
   var popupContent = "<div class='dismiss-container'><a class='dialog-dismiss' href='javascript:void(0)'>×</a></div><p style='padding: 2px 0 0 0'>" + content + "</p><div align='center'><input type='button' class='close-dialog' value='Close' /></div>";
   $(popup).set({$display: 'block', 'innerHTML': popupContent});

   var topCoord = window.innerHeight / 2 - $$(popup).offsetHeight / 2;
   var leftCoord = window.innerWidth / 2 - $$(popup).offsetWidth / 2;
   $(popup).set({$top: topCoord + "px", $left: leftCoord + "px"});

   $('.popup-window .close-dialog').on('click', function () {
      close_dialog($(this));
      reAjaxifyLink(parentTD, thisLink);
   });

   $('.popup-window .dialog-dismiss').on('click', function () {
      close_dialog($(this));
      reAjaxifyLink(parentTD, thisLink);
   });

   $(mask).on('click', function () {
      $(this).remove();
      $('.popup-window').remove();
      reAjaxifyLink(parentTD, thisLink);
   });

   function close_dialog(source) {
      var parentDialog = source.trav('parentNode', 'div.popup-dialog');
      if (parentDialog) {
         $(parentDialog).remove();
      }
      if ($("div.popup-dialog").length == 0) {
         $(mask).remove();
      }
   }

   function reAjaxifyLink(parentTD, href) {
      var anchorFactory = EE('a');
      var anchor = anchorFactory()[0];
      $(anchor).set({'@href': href, $: '+ajaxable'});
      $(anchor).set('innerHTML', '[view]');
      $(anchor).on('click', ajaxClickHandler);
      var strongFactory = EE('strong');
      var strong = strongFactory()[0];
      parentTD.set('innerHTML', '&nbsp;');
      parentTD.add($(strong).add($(anchor)));
   }
}