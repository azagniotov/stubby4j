var divFactory = EE('div');
var mask = divFactory()[0];

$(mask).set({'@id': 'cgmp-popup-mask'});
$('body').add(mask);

var body = document.body;
var html = document.documentElement;

var maskHeight = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight );

$(mask).set({
    $display: 'block',
    $opacity: '0.3',
    $width: '100%',
    $height: maskHeight + 'px'
});

alert('yes');
$(mask).set({$display: 'none'});
$(mask).remove();