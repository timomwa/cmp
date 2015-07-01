window.onload = function(){
	var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Collecting latency data. Please wait..."});
	myMask.show();
	Ext.Ajax.request({
		url :  '/Stats.action?getLinksLatencyStats',
		scope : this,
		success : function(response, opts) {
				var pg = response.responseText;
				var resp = eval( '('+pg+')' );
				myMask.hide();
				var ctx = document.getElementById("billingmonitor").getContext("2d");
				window.myLine = new Chart(ctx).Line(resp, {
					responsive: true,
					scaleShowGridLines : false,
					scaleGridLineWidth : 10,
					bezierCurve : true,
					pointDot : false,
					 scaleShowLabels: true,
					 scaleLabel: "   <%=value%> ms  ",
					 scaleFontColor: "#666",
					  scaleShowHorizontalLines: false,
					  showXLabels: 10,
					  scaleBackdropPaddingY: 2,
					  scaleBackdropPaddingX: 2,
					  scaleFontSize: 12
				});
			},
		failure : function() {
					Ext.Msg.alert('Failed complete action!');
				}
	});
}