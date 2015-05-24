
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
                items : [],
                iconCls: 'customer-care-win',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
		
		buildComponents();
        
		win.show();
    }
});


function buildComponents(){
	
	var ds = new Ext.data.Store({
        proxy: new Ext.data.ScriptTagProxy({
            url: '/CustomerCare.action'
        }),
        reader: new Ext.data.JsonReader({
            root: 'subscriptions',
            totalProperty: 'totalCount',
            id: 'id'
        }, [{name: 'msisdn', mapping: 'msisdn'},
            {name: 'servicename', mapping: 'servicename'},
            {name: 'subscriptionDate', mapping: 'subscriptionDate'}
        ])
    });
	
	// Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<h3><span>{msisdn}<br /> </h3>    {servicename}</span> ',
            '<b>subscription date:</b> {subscriptionDate:date("M j, Y")}',
        '</div></tpl>'
    );
    
	var countries = new Ext.data.SimpleStore({
	   fields: ['countries','code'],
	   data : [['Kenya','254']]
	});
	var countriescbo = new Ext.form.ComboBox({
       store : countries,
	   name: 'msisdn',
	   hiddenName: 'msisdn',
	   emptyText : 'Select country',
	   mode: 'local',
	   triggerAction: 'all',
	   displayField:'countries',
	   valueField : 'code',
	   forceSelection : true,
	   listeners : {
		   afterrender : function(el){
			   this.setValue('254');
		   }
	   }
    });
	
    var search = new Ext.form.ComboBox({
       name: 'msisdn',
	   hiddenName: 'msisdn',
	   hideTrigger:true,
	   emptyText : 'Type to search msisdn',
	   store: ds,
	   loadingText: 'Searching...',
	   displayField:'msisdn',
	   typeAhead: false,
	   tpl: resultTpl,
	   width: 370,
       pageSize:10,
	   itemSelector: 'div.search-item',
	    onSelect: function(record){ // override default onSelect to do redirect
            //window.location =
              //  String.format('http://extjs.com/forum/showthread.php?t={0}&p={1}', record.data.topicId, record.id);
			  alert('yay!');
        }
	   
    });
	
	var tbar = new Ext.Toolbar({
					enableOverflow: true,
					items: [countriescbo,'-',search]
			   });
	
	var subscriptionTab = {
		xtype: 'panel',
		id: 'subscriptionTab_',
		title:'Subscription Stats',
		html: 'Subscription Tab',
		tbar : tbar
	};
	
	var messageLogTab = {
		xtype: 'panel',
		id: 'messageLogTab_',
		title:'Message Log',
		html: 'Message Log'
	}
	
	var tabbedPannel =  new Ext.TabPanel({
		id : 'content_management_panel',
		autoTabs:true,
		border:false,
		bodyBorder : false,
		activeTab: 0,
		items : [subscriptionTab,messageLogTab]
	});
	
	var disWin = Ext.getCmp('customer_care_win');
	
	disWin.items.add(tabbedPannel);
}


var handleAction = function(action){
      //  Ext.msg('<b>Action</b>', 'You clicked "'+action+'"');
		Ext.Msg.show({
			title: '<b>Action</b>',
			msg:  'You clicked "'+action+'"',
			buttons: Ext.Msg.OK,
			icon: Ext.Msg.INFO
	   });
};	

