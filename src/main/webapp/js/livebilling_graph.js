function populate(){


		/*var store = new Ext.data.JsonStore({
        fields:['name', 'visits', 'views'],
        data: [
            {name:'Jul 11', visits: 245000, views: 3000000},
            {name:'Aug 11', visits: 240000, views: 3500000},
            {name:'Sep 11', visits: 355000, views: 4000000},
            {name:'Oct 07', visits: 375000, views: 4200000},
            {name:'Nov 07', visits: 490000, views: 4500000},
            {name:'Dec 07', visits: 495000, views: 5800000},
            {name:'Jan 08', visits: 520000, views: 6000000},
            {name:'Feb 08', visits: 620000, views: 7500000}
        ]
		});*/
		
		
		var store = new Ext.data.JsonStore({
			url: '/Stats.action',
			root: 'data',
			fields:['name', 'revenue', 'hits'],
		});
		
		store.load();
		
		var x = new Ext.TabPanel({
			activeTab: 0,
			items: [{
			    id : 'billing_stats',
				title: 'Billing Stats',
				width:630,
				height:460,
				items : [
				
					new Ext.Panel({
					iconCls:'chart',
					title: 'Live Billing & Revenue Statistics',
					frame:true,
					width:625,
					height:420,
					layout:'fit',

					items: {
						xtype: 'columnchart',
						store: store,
						url:'/js/charts.swf',
						xField: 'name',
						yAxis: new Ext.chart.NumericAxis({
							displayName: 'Hits',
							labelRenderer : Ext.util.Format.numberRenderer('0,0')
						}),
						tipRenderer : function(chart, record, index, series){
							if(series.yField == 'hits'){
								return Ext.util.Format.number(record.data.hits, '0,0') + ' Hits on ' + record.data.name;
							}else{
								return Ext.util.Format.number(record.data.revenue, '0,0') + ' KES Revenue on ' + record.data.name;
							}
						},
						chartStyle: {
							padding: 10,
							animationEnabled: true,
							font: {
								name: 'Tahoma',
								color: 0x444444,
								size: 11
							},
							dataTip: {
								padding: 5,
								border: {
									color: 0x99bbe8,
									size:1
								},
								background: {
									color: 0xDAE7F6,
									alpha: .9
								},
								font: {
									name: 'Tahoma',
									color: 0x15428B,
									size: 10,
									bold: true
								}
							},
							xAxis: {
								color: 0x69aBc8,
								majorTicks: {color: 0x69aBc8, length: 4},
								minorTicks: {color: 0x69aBc8, length: 2},
								majorGridLines: {size: 1, color: 0xeeeeee}
							},
							yAxis: {
								color: 0x69aBc8,
								majorTicks: {color: 0x69aBc8, length: 4},
								minorTicks: {color: 0x69aBc8, length: 2},
								majorGridLines: {size: 1, color: 0xdfe8f6}
							}
						},
						series: [{
							type: 'column',
							displayName: 'Page Views',
							yField: 'revenue',
							style: {
								image:'bar.gif',
								mode: 'stretch',
								color:0x99BBE8
							}
						},{
							type:'line',
							displayName: 'Visits',
							yField: 'hits',
							style: {
								color: 0x15428B
							}
						}]
					}
				})]
			},{
				title: 'Tab 2',
				html: 'Another one'
			}]
		});
		
		
		var tab = Ext.getCmp('statistics_win');
		tab.items.add(x);
		tab.doLayout();
		x.doLayout();
	
	
		
}