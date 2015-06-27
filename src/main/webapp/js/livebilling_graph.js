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
							tooltipTemplate : "<%if (label){%> <%=label%> Revenue : <%}%>KES. <%=formatD(value)%>"
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
				width:630,
				height:550,
				items : [{
					title: 'Live Billing & Revenue Statistics',
					html: '<div> <canvas id="canvas" height="190" width="auto"></canvas></div>'
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