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
                width:640,
                height:480,
                html : '<p>Statistics Module.</p>',
                iconCls: 'statistics-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
});