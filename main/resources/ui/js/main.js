var MINI = require('minified');
var $ = MINI.$, $$ = MINI.$$, EE = MINI.EE;
var divFactory = EE('div');
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
   $(parentTD).set("innerHTML", "&nbsp;<img align='middle' src='/images/loading.gif' border='0' />");

   $.request('get', thisLink.href).then(
      function success(content) {
         var id = Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3);
         var highlightableAjaxResponse = "<pre><code id='" + id + "'>" + content.replace(/^\s+|\s+$/g, '') + "</code></pre>";
         displayPopupWithContent(thisLink, $(parentTD), highlightableAjaxResponse);
         hljs.highlightBlock($$("code#" + id));
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
   var popup = divFactory()[0];
   $('body').add([mask, popup]);

   var body = document.body;
   var html = document.documentElement;
   var maskHeight = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
   var maskWidth = Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth);
   $(mask).set({'@id': 'popup-mask', $display: 'block', $opacity: '0.4', $width: maskWidth + 'px', $height: maskHeight + 'px'});

   var content = "<div id='inner-dialog'><b>" + ajaxContent + "</b></div><br />";
   var id = Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3);
   var popupContent = "<div class='dismiss-container'><a class='dialog-dismiss' href='javascript:void(0)'>×</a></div><p style='padding: 2px 0 0 0'>" + content + "</p><div align='center'><input type='button' class='close-dialog' value='Close' /></div>";
   $(popup).set({'@id': id, $display: 'block', $: '+popup-dialog +popup-window'});
   var topCoord = window.innerHeight / 2 - $$(popup).offsetHeight / 2;
   var leftCoord = window.innerWidth / 2 - $$(popup).offsetWidth / 2;
   $(popup).set({$top: topCoord + "px", $left: leftCoord + "px", 'innerHTML': popupContent});

   $('.popup-window .close-dialog').on('click', function () {
      closePopupAndResetHandler();
   });

   $('.popup-window .dialog-dismiss').on('click', function () {
      closePopupAndResetHandler();
   });

   $(mask).on('click', function () {
      closePopupAndResetHandler();
   });

   document.onkeydown = function (event) {
      if (event.keyCode === 27) { //ESC key
         closePopupAndResetHandler();
      }
   }

   function closePopupAndResetHandler() {
      closeDialog();
      reAjaxifyLink(parentTD, thisLink);
   }

   function closeDialog() {
      $('.popup-window').remove();
      $(mask).remove();
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