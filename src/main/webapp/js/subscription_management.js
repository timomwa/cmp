MyDesktop.SubscriptionManagementModule = Ext.extend(Ext.app.Module, {
	id:'subscription-win',
    init : function(){
        this.launcher = {
            text: 'Subscription Management',
            iconCls:'subscription-man-win',
            handler : this.createWindow,
            scope: this,
            windowId:windowIndex
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('subscription_management_win');
        if(!win){
            win = desktop.createWindow({
                id: 'subscription_management_win',
                title: 'Subscription Management',
                width:640,
                height:480,
                html : '<p>Subscription Management Module.</p>',
                iconCls: 'subscription-man-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
});