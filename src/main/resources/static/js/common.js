(function(doc, win) {
	var docEl = doc.documentElement,
		resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize';
	var recalc = function() {
		var clientWidth = docEl.clientWidth;
		if (!clientWidth) return;
		var fontSize = 100 * (clientWidth / 500);
		// if (fontSize > 205) fontSize = 100;
		docEl.style.fontSize = fontSize + 'px';
	};
	if (!doc.addEventListener) return;
	recalc();
	win.addEventListener(resizeEvt, recalc, false);
	doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);
var u = navigator.userAgent;
var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
// function operator_close () {
//     window.local_obj.close()
// }
function postMyMessageA(){
    var message = { 'message' : 'You choose the A'};
    window.webkit.messageHandlers.open_meiqia.postMessage(message);
}
function postMessageTel(){
	var message = {'message' : '4007026677'};
	window.webkit.messageHandlers.open_tel.postMessage(message);
}
$('#tel').click(function(){
    $('.shadow_qq').hide();
    $('.shadow').show();
})
$('.hide').click(function(){
    $('.shadow').hide();
})
$('#btn_qq').click(function(){
	console.log("美恰客服");
    if(isAndroid==true){
		local_obj.open_meiqia();
	}else if(isiOS==true){
		postMyMessageA();
	}
})