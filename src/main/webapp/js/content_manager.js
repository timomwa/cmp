MyDesktop.ContentManagementModule = Ext.extend(Ext.app.Module, {
	id:'content-win',
	keyords : {},
	
    init : function(){
        this.launcher = {
            text: 'Content Management',
            iconCls:'content-man-win',
            handler : this.createWindow,
            scope: this,
            windowId:windowIndex
        }
		
		
    },
	
	
	loadContent : function(src){
		
	},
	
	loadStores : function(src){
		this.keyords = new Ext.data.JsonStore({
			url : 'ContentManagement.action?listServices=&',
			root : 'keywords',
			fields : [{
						name : 'id',
						type : 'int'
					}, {
						name : 'mo_processorFK'
					}, {
						name : 'cmd'
					}, {
						name : 'push_queue'
					}, {
						name : 'service_name'
					}, {
						name : 'service_description'
					}, {
						name : 'price'
					}, {
						name : 'price_point_keyword'
					}, {
						name : 'enabled'
					}, {
						name : 'split_mt'
					}, {
						name : 'subscriptionText'
					}, {
						name : 'unsubscriptionText'
					}, {
						name : 'tailText_subscribed'
					}, {
						name : 'tailText_notsubscribed'
					}, {
						name : 'event_type'
					}]
		});
		this.keyords.load();
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
				    id : 'content_management_panel',
					autoTabs:false,
					border:false,
					bodyBorder : false,
					items:[
				       {
					    id: 'keyword_management_tab',
						title:'Keyword Management',
						html: '<div id="toolbar"></div><div id="keywords_div"></div>',
						width:640,
						height:480,
						},
						
                       {
					    id: 'content_upload_tab',
						title:'Content Upload', 
						autoheight: true
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
        win.show();
		
		var keyords = new Ext.data.JsonStore({
			url : 'ContentManagement.action?listServices=&',
			root : 'keywords',
			fields : [{
						name : 'id',
						type : 'int'
					}, {
						name : 'mo_processorFK'
					}, {
						name : 'cmd'
					}, {
						name : 'push_queue'
					}, {
						name : 'service_name'
					}, {
						name : 'service_description'
					}, {
						name : 'price'
					}, {
						name : 'price_point_keyword'
					}, {
						name : 'enabled'
					}, {
						name : 'split_mt'
					}, {
						name : 'subscriptionText'
					}, {
						name : 'unsubscriptionText'
					}, {
						name : 'tailText_subscribed'
					}, {
						name : 'tailText_notsubscribed'
					}, {
						name : 'event_type'
					}]
		});
		keyords.load();
		var keyword_management_tab = win.findById('keyword_management_tab');
		keyword_management_tab.show();
		
		var keywords_combo = Ext.get('keywords_combo');
		if(!keywords_combo);
		keyword_combo = new Ext.form.ComboBox({
								id : 'keywords_combo',
								lazyRender:false,
								displayField: 'keyword',
								valueField: 'id',
								store: keyords,
								mode: 'remote',
								typeAhead: true,
								triggerAction: 'all',
								emptyText:'Select a Keyword...',
								name: 'Keywords',
								editable: true,
								loadingText: 'Loading...',
								minChars: 2,
								width: 120,
								selectOnFocus:true,
							});
							
		var myForm = Ext.get('keywords_combo');		
		if(!myForm)
		var simpleForm = new Ext.FormPanel({
							id : 'myForm',
							renderTo: 'keywords_div',
							labelAlign : 'left',
							height : 'auto',
							 labelWidth : 150,
							frame : true,
							autoScroll: true,
							items: [{
							autoScroll: true,
								layout : 'column',
								border : false,
								items : [{
											columnWidth : .5,
											layout : 'form',
											border : false,
												items: [
												{
													xtype : 'combo',
													fieldLabel: 'Keyword',
													id : 'keywords_combo',
													lazyRender:false,
													displayField: 'keyword',
													valueField: 'id',
													store: keyords,
													mode: 'remote',
													typeAhead: true,
													scope: this,
													triggerAction: 'all',
													emptyText:'Select a Keyword...',
													name: 'Keywords',
													editable: true,
													loadingText: 'Loading...',
													minChars: 5,
													labelWidth : 150,
													displayField : 'cmd',
													valueField : 'id',
													anchor : '100%',
													width: 150,
													selectOnFocus:true,
													listeners: {
																	'select': function(cmb, rec, idx) {
																		for(var k = 0; k<keyords.reader.jsonData.size;k++){
																			if(keyords.reader.jsonData.keywords[k].id==cmb.getValue()){
																				simpleForm.findById('mo_processorFK').setValue(keyords.reader.jsonData.keywords[k].mo_processorFK);
																				simpleForm.findById('cmd').setValue(keyords.reader.jsonData.keywords[k].cmd);
																				simpleForm.findById('push_unique').setValue(keyords.reader.jsonData.keywords[k].push_unique);
																				
																				simpleForm.findById('service_name').setValue(keyords.reader.jsonData.keywords[k].service_name);
																				simpleForm.findById('service_description').setValue(keyords.reader.jsonData.keywords[k].service_description);
																				simpleForm.findById('price').setValue(keyords.reader.jsonData.keywords[k].price);
																				
																				
																				simpleForm.findById('price_point_keyword').setValue(keyords.reader.jsonData.keywords[k].price_point_keyword);
																				simpleForm.findById('enabled').setValue(keyords.reader.jsonData.keywords[k].enabled);
																				simpleForm.findById('split_mt').setValue(keyords.reader.jsonData.keywords[k].split_mt);
																				
																				
																				simpleForm.findById('subscriptionText').setValue(keyords.reader.jsonData.keywords[k].subscriptionText);
																				simpleForm.findById('unsubscriptionText').setValue(keyords.reader.jsonData.keywords[k].unsubscriptionText);
																				simpleForm.findById('tailText_subscribed').setValue(keyords.reader.jsonData.keywords[k].tailText_subscribed);
																				
																																								
																				simpleForm.findById('tailText_notsubscribed').setValue(keyords.reader.jsonData.keywords[k].tailText_notsubscribed);
																				simpleForm.findById('event_type').setValue(keyords.reader.jsonData.keywords[k].event_type);
																			}
																		}
																	}
																}
												 },{
													xtype : 'textfield',
													id: 'mo_processorFK',
													fieldLabel: 'MOProcessor',
													name: 'smsservice.mo_processorFK',
													allowBlank:false,
													anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'cmd',
														fieldLabel: 'Keyword',
														name: 'smsservice.cmd',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'push_unique',
														fieldLabel: 'Push Unique',
														name: 'smsservice.push_unique',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'service_name',
														fieldLabel: 'Service Name',
														name: 'smsservice.service_name',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'service_description',
														fieldLabel: 'Service Description',
														name: 'smsservice.service_description',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'price',
														fieldLabel: 'Price',
														name: 'smsservice.price',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'price_point_keyword',
														fieldLabel: 'Price Point KW',
														name: 'smsservice.price_point_keyword',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'enabled',
														fieldLabel: 'Enabled',
														name: 'smsservice.enabled',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'split_mt',
														fieldLabel: 'Split SMS into 160 xters',
														name: 'smsservice.split_mt',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'subscriptionText',
														fieldLabel: 'Subscription Text',
														name: 'smsservice.subscriptionText',
													    anchor : '95%'
													}]
											},{
													columnWidth : .5,
													layout : 'form',
													border : false,
													items: [
													{
														 xtype : 'textfield',
														 id: 'unsubscriptionText',
														fieldLabel: 'Unsubscription Text',
														name: 'smsservice.unsubscriptionText',
													    anchor : '100%'
													},{
														 xtype : 'textfield',
														 id: 'tailText_subscribed',
														fieldLabel: 'Tail Text When Subscribed',
														name: 'smsservice.tailText_subscribed',
													    anchor : '100%'
													},{
														 xtype : 'textfield',
														 id: 'tailText_notsubscribed',
														fieldLabel: 'Tail Text When not Subscribed',
														name: 'smsservice.tailText_notsubscribed',
													    anchor : '100%'
													},{
														 xtype : 'textfield',
														 id: 'event_type',
														fieldLabel: 'Event Type',
														name: 'smsservice.event_type',
													    anchor : '100%'
													}
												]
										}]
									}],
							buttons: [
							{
								text: 'Save',
								iconCls : 'x-icon-save',
								handler : function submitForm(){
								
									alert('Save!!');
									}
								},{
								text: 'Cancel',
								iconCls : 'x-icon-close',
								handler : function cancel(){
									alert('Cancelled!');
								}
								}
							]
						});					
							
		
		
	}
});