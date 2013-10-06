var MINI = require('minified');
var $ = MINI.$, $$ = MINI.$$, EE = MINI.EE;
var divFactory = EE('div');
var anchorFactory = EE('a');
var strongFactory = EE('strong');
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
   // TD/STRONG/A
   var parentTD = thisLink.parentNode.parentNode;
   $(parentTD).set("innerHTML", "&nbsp;<img align='middle' src='/images/loading.gif' border='0' />");

   $.request('get', thisLink.href).then(
      function success(content) {
         displayPopupWithContent(thisLink, $(parentTD), content.replace(/^\s+|\s+$/g, ''));
         hljs.highlightBlock($$("code#ajax-response"));
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

function displayPopupWithContent(thisLink, parentTD, popupHtmlWithContent) {
   var divRemovable = divFactory()[0];
   $(divRemovable).set({'@id': "popup-placeholder", 'innerHTML': popupHtmlWithContent});
   $('body').add($(divRemovable));

   var body = document.body;
   var html = document.documentElement;
   var maskHeight = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
   var maskWidth = Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth);
   var divPopupMask = 'div#popup-mask';
   $(divPopupMask).set({$display: 'block', $opacity: '0.4', $width: maskWidth + 'px', $height: maskHeight + 'px'});

   var divPopupWindow = 'div#popup-window';
   $(divPopupWindow).set({$display: 'block'});
   var topCoord = window.innerHeight / 2 - $$(divPopupWindow).offsetHeight / 2;
   var leftCoord = window.innerWidth / 2 - $$(divPopupWindow).offsetWidth / 2;
   $(divPopupWindow).set({$top: topCoord + "px", $left: leftCoord + "px"});

   $(divPopupWindow + ' .close-dialog').on('click', function () {
      closePopupAndResetHandler();
   });

   $(divPopupWindow + ' .dialog-dismiss').on('click', function () {
      closePopupAndResetHandler();
   });

   $(divPopupMask).on('click', function () {
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
      $(divPopupWindow).animate({$$fade: 0}, 250).then(function() {
         $(divPopupWindow).remove();
      });
      $(divPopupMask).animate({$$fade: 0}, 250).then(function() {
         $(divPopupMask).remove();
      });
      $("div#popup-placeholder").remove();
   }

   function reAjaxifyLink(parentTD, href) {
      var anchor = anchorFactory()[0];
      $(anchor).set({'@href': href, $: '+ajaxable', 'innerHTML': '[view]'});
      $(anchor).on('click', ajaxClickHandler);

      var strong = strongFactory()[0];
      parentTD.set('innerHTML', '&nbsp;');
      parentTD.add($(strong).add($(anchor)));
   }
}