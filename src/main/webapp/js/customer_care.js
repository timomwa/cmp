var subscriptionTab;
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
        {name: 'subscriptionDate', mapping: 'subscriptionDate'},
	    {name: 'pricepointkeyword', mapping: 'pricepointkeyword'},
		{name: 'status', mapping: 'status'},
		{name: 'price', mapping: 'price', type: 'float'}
        ])
});
	
var grid_datasource = new Ext.data.Store({
    proxy: new Ext.data.ScriptTagProxy({
        url: '/CustomerCare.action'
    }),
    reader: new Ext.data.JsonReader({
        root: 'subscriptions',
        totalProperty: 'totalCount',
        id: 'id'
    }, [{name: 'id', mapping: 'id'},
		{name: 'msisdn', mapping: 'msisdn'},
        {name: 'servicename', mapping: 'servicename'},
        {name: 'subscriptionDate', mapping: 'subscriptionDate'},
		{name: 'pricepointkeyword', mapping: 'pricepointkeyword'},
		{name: 'status', mapping: 'status'},
		{name: 'price', mapping: 'price', type: 'float'}
    ])
});		
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
var countriescbo;
var search;
var tabbedPannel;
var messageLogTab;
var tbar;
var grid;

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
                shim:true,
                animCollapse:false,
                constrainHeader:true
            });
			buildComponents();
        }
		
		win.show();
    }
});



/**
     * Custom function used for column renderer
     * @param {Object} val
     */
    function change(val) {
        if (val == 'confirmed') {
            return '<span style="color:green;">' + val + '</span>';
        } else{
            return '<span style="color:red;">' + val + '</span>';
        }
        return val;
    }
	
	/**
     * Custom function used for column renderer
     * @param {Object} val
     */
    function pctChange(val) {
		val = Ext.util.Format.number(val,'0.00');
        if (val > 0) {
            return '<span style="color:green;"> KES.' + val + '</span>';
        } else if (val < 0) {
            return '<span style="color:red;"> KES.' + val + '</span>';
        }
        return val;
    }

	
function changeSubscriptionStatus(rec,status){
	
	Ext.Ajax.request({
		url :  '/CustomerCare.action?changeSubscriptionStatus=&subscription_id='+rec.get('id')+'&msisdn='+rec.get('msisdn')+'&status='+status,
		scope : this,
		success : function(response, opts) {
					var pg = response.responseText;
					var resp = eval( '('+pg+')' );
					if(resp.success){
						rec.set('status', status);
						
						Ext.Msg.show({
							title:'Status : '+rec.get('msisdn')+' '+status,
							msg: resp.message,
							buttons: Ext.Msg.OK,
							animEl: 'elId',
							icon: Ext.MessageBox.INFO
						});
						
					}else{
						Ext.Msg.show({
							title:'Status : '+rec.get('msisdn')+' '+status,
							msg: resp.message,
							buttons: Ext.Msg.OK,
							animEl: 'elId',
							icon: Ext.MessageBox.ERROR
						});
					}
			
			Ext.getCmp('subscriptionsgrid').setTitle('Subscriptions status');
			Ext.getCmp('customer_care_win').setTitle('Customer care');

				},
		failure : function() {
				Ext.Msg.alert('Failed complete action!');
				Ext.getCmp('subscriptionsgrid').setTitle('Subscriptions status');
				Ext.getCmp('customer_care_win').setTitle('Customer care');
		}
	});
	
}	

function buildComponents(){
	
	//if(!grid){
grid = new Ext.grid.GridPanel({
		xtype: 'grid',
		id : 'subscriptionsgrid',
        store: grid_datasource,
        columns: [
            {
                id       :'id',
                header   : 'ID', 
                width    : 100, 
                sortable : true, 
				hidden: true,
                dataIndex: 'id'
            },{
                id       :'servicename',
                header   : 'Service Name', 
                width    : 100, 
                sortable : true, 
                dataIndex: 'servicename'
            },
            {
                header   : 'Subscription Date', 
                width    : 105, 
                sortable : true, 
                renderer : Ext.util.Format.dateRenderer('m/d/Y'), 
                dataIndex: 'subscriptionDate'
            },
            {
                header   : 'Price', 
                width    : 75, 
                sortable : true, 
                renderer : pctChange, 
                dataIndex: 'price'
            },
            {
                header   : 'Status', 
                width    : 95, 
                sortable : true, 
                renderer : change, 
                dataIndex: 'status'
            },
            {
                header   : 'Price point keywrd', 
                width    : 95, 
                sortable : true, 
                dataIndex: 'pricepointkeyword'
            },
            {
                header   : 'Sub/Unsub',
				xtype: 'actioncolumn',
                width: 50,
                items: [{
                    icon   : '../shared/icons/fam/delete.gif',  // Use a URL in the icon config
                    tooltip: 'Unsubscribe',
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid_datasource.getAt(rowIndex);
						if(rec.get('status')=='confirmed'){
	                        Ext.Msg.show({
							   title:'Confurm action',
							   msg: "Unsubscribe " +  rec.get('msisdn') + " from "+ rec.get('servicename'),
							   buttons: Ext.Msg.OKCANCEL,
							   fn: function(btn, text){
								   if (btn == 'ok'){
										changeSubscriptionStatus(rec,'unsubscribed');
									}
								},
							   animEl: 'elId',
							   icon: Ext.MessageBox.QUESTION
							});
						}else{
							Ext.Msg.show({
								   title:'No action at this stage',
								   msg: 'Subscriber is already unsubscribed',
								   animEl: 'elId',
								   icon: Ext.MessageBox.WARNING
								});
						}
                    }
                }, {
					icon   : '../shared/icons/fam/accept.png',
					tooltip: 'Renew subscription',
                    getClass: function(v, meta, rec) {
					    if(rec.get('status') == 'subscribed'){
							 this.items[0].tooltip = ('Unsubscribe ' +  rec.get('msisdn') + ' from '+ rec.get('servicename') );
                        }
						if (rec.get('status') == 'unsubscribed' ) {
                            this.items[0].tooltip = 'No action required';	 
							this.items[1].tooltip = ('Renew subscription to '+rec.get('servicename') + ' for ' + rec.get('msisdn'));
                        	return 'alert-col';
                        } else {
                            this.items[1].tooltip = 'Renew subscription';
                           return 'buy-col';
                        }
						
						
                    },
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid_datasource.getAt(rowIndex);
						
                        if(rec.get('status')!='confirmed'){
							Ext.Msg.show({
							   title:'Confurm action',
							   msg: "Renew subscription for " +  rec.get('msisdn') + " to "+  rec.get('servicename'),
							   buttons: Ext.Msg.OKCANCEL,
							   fn: function(btn, text){
									if (btn == 'ok'){
										changeSubscriptionStatus(rec,'confirmed');
									}
								},
							   animEl: 'elId',
							   icon: Ext.MessageBox.QUESTION
							});
                        }else{
							Ext.Msg.show({
								   title:'No action required',
								   msg: 'Subscriber is already subscribed',
								   animEl: 'elId',
								   icon: Ext.MessageBox.WARNING
								});
						}
                    }
                }]
            }
        ],
		 // paging bar on the bottom
        bbar: new Ext.PagingToolbar({
            pageSize: 10,
            store: grid_datasource,
            displayInfo: true,
            displayMsg: 'Displaying subscriptions {0} - {1} of {2}',
            emptyMsg: "No subscriptions"
        }),
        stripeRows: true,
        autoExpandColumn: 'servicename',
        autoWidth : 'auto',
		height: 400,
		title: 'Subscriptions ',
		autoScroll: true,
		 // config options for stateful behavior
        stateful: true,
        stateId: 'grid'
    });
//}
	
	//if(!countriescbo){
		countriescbo = new Ext.form.ComboBox({
		   xtype: 'combo',
		   id : 'countriescbo',	
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
	//}
	
	//if(!search){
		search = new Ext.form.ComboBox({
		   xtype: 'combo',
		   id: 'searchcbox',
		   name: 'msisdn',
		   hiddenName: 'msisdn',
		   hideTrigger:true,
		   emptyText : 'Type to search msisdn',
		   store: ds,
		   loadingText: 'Searching...',
		   displayField:'msisdn',
		   typeAhead: false,
		   tpl: resultTpl,
		   width: 200,
		   pageSize:10,
		   itemSelector: 'div.search-item',
		   onSelect: function(record){ 
			   grid_datasource.reload({
					params : {
						'query' : record.data.msisdn
					}
				});
				this.collapse();
				this.setValue(record.data.msisdn);
				Ext.getCmp('subscriptionsgrid').setTitle('<b>Subscriptions status '+record.data.msisdn+'</b>');
				Ext.getCmp('customer_care_win').setTitle('<b>Customer care :: '+record.data.msisdn+'</b>');
			}
		   
		});
	//}
	
	var countryLabel = {xtype:'label',text:'Country:'};
	var searchLabel = {xtype:'label',text:'MSISDN:'};
	//if(!tbar){
		tbar = new Ext.Toolbar({
				xtype: 'toolbar',	
				id: 'ccisearchtbar',
				enableOverflow: true,
				items: [countryLabel,'-',countriescbo,'-',searchLabel,'-',search]
			   });
	//}
	
	//if(!subscriptionTab){
		subscriptionTab = {
			xtype: 'panel',
			layout: 'fit',
			id: 'subscriptionTab_',
			title:'Subscription Management',
			tbar : tbar,
			autoHeight: 'auto',
			autoWidth : 'auto',
			autoScroll: true,
			items :  [grid]
		};
	//}
	
	
	//if(!messageLogTab){
		messageLogTab = {
			xtype: 'panel',
			id: 'messageLogTab_',
			title:'Message Log',
			html: 'Message Log'
		}
	//}
	
	//if(!tabbedPannel){
		tabbedPannel =  new Ext.TabPanel({
			xtype: 'tabpanel',	
			id : 'content_management_panel',
			autoTabs:true,
			border:false,
			bodyBorder : false,
			activeTab: 0,
			items : [subscriptionTab]//messageLogTab
		});
	//}
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

