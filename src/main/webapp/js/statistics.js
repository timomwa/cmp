Ext.chart.Chart.CHART_URL = '/js/charts.swf';
Ext.QuickTips.init();
    
    // create some portlet tools using built in Ext tool ids
    var tools = [{
        id:'gear',
        handler: function(){
            Ext.Msg.alert('Message', 'The Settings tool was clicked.');
        }
    },{
        id:'close',
        handler: function(e, target, panel){
            panel.ownerCt.remove(panel, true);
        }
    }];
	
MyDesktop.StatisticsModule = Ext.extend(Ext.app.Module, {
	id:'statistics-win',
    init : function(){
        this.launcher = {
            text: 'Statistics',
            iconCls:'statistics-win',
            handler : this.createWindow,
            scope: this,
            windowId:windowIndex
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('statistics_win');
        if(!win){
            win = desktop.createWindow({
                id: 'statistics_win',
                title: 'Statistics',
                width:760,
                height:600,
				items :  [],
                iconCls: 'statistics-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
				bodyStyle:'padding:5px;background: transparent; border: none; ',
				border:false
            });
        }
        win.show();
		populate();
                                    
    }
});