function displayPopupWithContent(ajaxContent)  {
   var divFactory = EE('div');

   var mask = divFactory()[0];
   $('body').add(mask);

   var popup = divFactory()[0];
   $('body').add(popup);

   $(mask).set({'@id': 'cgmp-popup-mask'});
   var body = document.body;
   var html = document.documentElement;
   var maskHeight = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight );
   $(mask).set({$display: 'block', $opacity: '0.3', $width: '100%', $height: maskHeight + 'px'});

   var content = "<div id='inner-shortcode-dialog'><b>" + ajaxContent + "</b></div><br />";
   var id = Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3);
   $(popup).set({'@id': id, $: '+cgmp-popup-shortcode-dialog +cgmp-popup-window'});
   var popupContent = "<div class='dismiss-container'><a class='dialog-dismiss' href='javascript:void(0)'>×</a></div><p style='padding: 10px 10px 0 10px'>" + content + "</p><div align='center'><input type='button' class='close-dialog' value='Close' /></div>";
   $(popup).set('innerHTML', popupContent);
   $(popup).set({$display: 'block'});

   var topCoord = window.innerHeight/2 - $$(popup).offsetHeight/2;
   var leftCoord = window.innerWidth/2 - $$(popup).offsetWidth/2;
   $(popup).set({$top:  topCoord + "px", $left:  leftCoord + "px"});

   $('.cgmp-popup-window .close-dialog').on('click', function() {
       close_dialog($(this));
   });

   $('.cgmp-popup-window .dialog-dismiss').on('click', function() {
      close_dialog($(this));
   });

   $(mask).on('click', function() {
       $(this).remove();
       $('.cgmp-popup-window').remove();
   });

   function close_dialog(source) {
       var parentDialog = source.trav('parentNode', 'div.cgmp-popup-shortcode-dialog');
   	 if (parentDialog) {
   	   $(parentDialog).remove();
       }
       if ($("div.cgmp-popup-shortcode-dialog").length == 0) {
   	   $(mask).remove();
   	 }
   }
}