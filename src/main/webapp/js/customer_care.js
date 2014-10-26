MyDesktop.CustomerCareModule = Ext.extend(Ext.app.Module, {
	id:'customer_care-win',
    init : function(){
        this.launcher = {
            text: 'Customer Care',
            iconCls:'customer-care-win',
            handler : this.createWindow,
            scope: this,
            windowId:windowIndex
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('customer_care_win');
        if(!win){
            win = desktop.createWindow({
                id: 'customer_care_win',
                title: 'Customer Care',
                width:640,
                height:480,
                html : '<p>Customer Care Module.</p>',
                iconCls: 'customer-care-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
});