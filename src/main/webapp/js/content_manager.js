MyDesktop.ContentManagementModule = Ext.extend(Ext.app.Module, {
	id:'content-win',
    init : function(){
        this.launcher = {
            text: 'Content Management',
            iconCls:'content-man-win',
            handler : this.createWindow,
            scope: this,
            windowId:windowIndex
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('content_management_win');
        if(!win){
        	
            win = desktop.createWindow({
            	id: 'content_management_win',
                title: 'Content Management Window',
                width:640,
                height:480,
                iconCls: 'content-man-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
				
				
				  items: new Ext.TabPanel({
					autoTabs:true,
					border:true,
					bodyBorder : true,
					items:[
				       {
						contentEl:'tab1', 
						title:'Keyword Management',
						autoheight: true,
						html:'<p></p>'
						},
						
                       {
						contentEl:'tab2', 
						title:'Content Management', 
						autoheight: true,
						html:'<p>Content Upload Test</p>'
						}
                    ]
                }),

                buttons: [{
                    text:'Submit',
                    disabled:true
                },{
                    text: 'Close',
                    handler: function(){
                        win.hide();
                    }
                }]
            });
        }
        win.show(this);
    }
});