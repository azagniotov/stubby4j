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
   $(parentTD).set("innerHTML", "<img align='baseline' src='/images/loading.gif' border='0' />");

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
   var body = document.body;
   var html = document.documentElement;

   var divRemovable = divFactory()[0];
   $(divRemovable).set({'@id': "popup-placeholder", 'innerHTML': popupHtmlWithContent});
   $('body').add($(divRemovable));

   var divPopupMask = 'div#popup-mask';
   $(divPopupMask).set({$display: 'block', $opacity: '0.4', $width: getMaskWidth() + 'px', $height: getMaskHeight() + 'px'});

   var divPopupWindow = 'div#popup-window';
   $(divPopupWindow).set({$display: 'block'});
   $(divPopupWindow).set({$top: getTopCoord() + "px", $left: getLeftCoord() + "px"});

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

   function getMaskHeight() {
      return Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight, window.innerHeight);
   }

   function getMaskWidth() {
      return Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth, window.innerWidth);
   }

   function getTopCoord() {
      return (window.innerHeight / 2 - $$(divPopupWindow).offsetHeight / 2);
   }

   function getLeftCoord() {
      return (window.innerWidth / 2 - $$(divPopupWindow).offsetWidth / 2);
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
      parentTD.set('innerHTML', '');
      parentTD.add($(strong).add($(anchor)));
   }

   window.onresize = function(event) {
      $(divPopupMask).set({$width: getMaskWidth() + 'px', $height: getMaskHeight() + 'px'});
      $(divPopupWindow).set({$top: getTopCoord() + 'px', $left: getLeftCoord() + 'px'});
   }
}