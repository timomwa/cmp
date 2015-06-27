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
			autoTabs:false,
			border:false,
			bodyBorder : false,
			deferredRender: false,
			listeners: {
                tabchange: function(tabPanel, tab){
                    console.log(tabPanel.id + ' : ' + tab.id);
					if(tab.id == 'generic_stats'){
						Ext.Ajax.request({
							url :  '/Stats.action?currentSubscriberDistribution=&',
							scope : this,
							success : function(response, opts) {
										var pg = response.responseText;
										var resp = eval( '('+pg+')' );
										var ctx = document.getElementById("genericStats").getContext("2d");
										window.chart = new Chart(ctx).Pie(resp.data, {
											responsive : true,
											tooltipTemplate : "<%if (label){%> <%=label%> Service : <%}%> <%=Math.round(value)%> subscribers",
											scaleBeginAtZero: false,
											scaleBackdropPaddingY: 15,
											scaleBackdropPaddingX: 15,
											scaleShowLine: false,
											segmentStrokeColor: "#fff",
											legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\">&nbsp;&nbsp;&nbsp;</span>&nbsp;<%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>"
										});
										var legend = window.chart.generateLegend();
										var legend_Div = Ext.get("legend_Div");
										legend_Div.update(legend);
									},
							failure : function() {
									Ext.Msg.alert('Failed complete action!');
							}
						});
					}
				}
            },
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
				id : 'generic_stats',
				title: 'Traffic Statistics',
				width:840,
				height:550,
				layout:'border',
				items : [
				
					{
						region:'west',
						id:'statsLinkPanel',
						title:'Statistics',
						split:true,
						fill: false,
						width: 150,
						minSize: 10,
						maxSize: 15,
						minheight: 50,
						height: 50,
						collapsible: true,
						margins:'15 0 5 5',
						cmargins:'15 5 5 5',
						layout:'accordion',
						layoutConfig:{
							type: 'accordion',
							titleCollapse: true,
							animate: true,
							activeOnTop: true
						},
						items: []
					},{
						region:'center',
						margins:'15 5 5 0',
						layout:'column',
						autoScroll:true,
						items:[{
							columnWidth: .99,
							baseCls:'x-plain',
							bodyStyle:'padding:5px 0 5px 5px',
							items:[{
								id: 'statscanvas',
								title: 'Subscription Distribution',
								items : [
										{
											html:'<br/><div style="width: 98%; height: 86%; padding:5px;"> <canvas id="genericStats" ></canvas><div id="legend_Div"></div></div><br/>'
										}
										]
								}]
							}]
					}
				
				]
				
				
			}]
		});
		
		
		var tab = Ext.getCmp('statistics_win');
		tab.items.add(x);
		tab.doLayout();
		x.doLayout();
		
		setTimeout(plotGraph,1000);

}