function formatD(dgt){
	var ddd =  dgt.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
	return ddd;
}
function plotGraph(){
			Ext.Ajax.request({
			url :  '/Stats.action',
			scope : this,
			success : function(response, opts) {
						var pg = response.responseText;
						var resp = eval( '('+pg+')' );
						var ctx = document.getElementById("canvas").getContext("2d");
						window.myBar = new Chart(ctx).Bar(resp, {
							responsive : true,
							tooltipTemplate : "<%if (label){%> <%=label%> Revenue : <%}%>KES. <%=formatD(value)%>",
							scaleBeginAtZero: false,
							scaleBackdropPaddingY: 15,
							scaleBackdropPaddingX: 15,
							scaleShowLine: false,
							segmentStrokeColor: "#fff",
							legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>"
						});
					},
			failure : function() {
					Ext.Msg.alert('Failed complete action!');
			}
		});
}
	
	
	
function populate(){
		
		var x = new Ext.TabPanel({
			activeTab: 0,
			items: [{
			    id : 'billing_stats',
				title: 'Billing Stats',
				width:530,
				height:580,
				items : [{
					title: 'Live Billing & Revenue Statistics',
					html: '<div style="width: 98%; height: 86%; padding:5px;"> <canvas id="canvas" height="410" width="590"></canvas></div>'
				}]
			},{
				title: 'Tab 2',
				html: ''
				
				
			}]
		});
		
		
		var tab = Ext.getCmp('statistics_win');
		tab.items.add(x);
		tab.doLayout();
		x.doLayout();
		
		setTimeout(plotGraph,1000);

}