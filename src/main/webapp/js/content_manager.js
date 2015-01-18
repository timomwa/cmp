var smsForm = new Ext.FormPanel({
							id : 'contentUploadForm',
							labelWidth: 50,
							height : 'auto',
							frame : true,
							autoScroll: true,
							items: [{
									autoScroll: true,
									layout : 'column',
									border : false,
									items : [{
											columnWidth : .99,
											layout : 'form',
											border : false,
												items: [
												    {
														xtype : 'textarea',
														id: 'sms',
														fieldLabel: 'SMS',
														name: 'sms',
														maxLength: 160,
													    anchor : '100%'
													},
													{
														xtype : 'textfield',
														labelSeparator : '',
														hidden : true,
														fieldLabel: 'SMS Menu id',
														id : 'smsmenuid',
														name : 'smsmenu.id',
														anchor : '95%'
													},
													{
													buttons: [
																{
																	text: 'Save',
																	iconCls : 'x-icon-save',
																	handler : function submitForm(){
																	
																		
																		 var form = Ext.getCmp('contentUploadForm').getForm();
																		 form.submit({
																				clientValidation: true,
																				url: '/SMSMenuManagement.action?saveOrUpdate=&',
																				waitMsg: 'Just a moment...',
																				params: {
																					_eventName: 'saveOrUpdate'
																				},
																				success: function (form, action) {
																					//form.findField('membership.id')
																					//	.setValue(action.result.id);
																					successAction(form, action);
																					//Ext.getCmp("customFieldSet").setValue("{'fields' : []}");
																					// Ext.getCmp('basicSchemeFormDetWin').close()
																				},
																				failure: function (form, action) {
																					failureAction(form, action);
																				}
																			});
																		 
																		 
																		}
																	},{
																	text: 'Cancel',
																	iconCls : 'x-icon-close',
																	handler : function cancel(){
																		alert('Cancelled!');
																	}
																	}
															]
													}
												]
										}
										
										]
									}]
						});	

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
var myForm = Ext.get('keywords_combo');		
		if(!myForm)
		var simpleForm = new Ext.FormPanel({
							id : 'myForm',
							labelWidth: 150,
							height : 'auto',
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
													displayField : 'cmd',
													valueField : 'id',
													anchor : '95%',
													selectOnFocus:true,
													listeners: {
																	'select': function(cmb, rec, idx) {
																		for(var k = 0; k<keyords.reader.jsonData.size;k++){
																			if(keyords.reader.jsonData.keywords[k].id==cmb.getValue()){
																				simpleForm.findById('id').setValue(keyords.reader.jsonData.keywords[k].id);
																				simpleForm.findById('mo_processorFK').setValue(keyords.reader.jsonData.keywords[k].mo_processorFK);
																				simpleForm.findById('cmd').setValue(keyords.reader.jsonData.keywords[k].cmd);
																				
																				var radio ;
																				var radio2;
																				if(keyords.reader.jsonData.keywords[k].push_unique==false){
																					radio = Ext.getCmp("push_unique0");
																					radio.setValue(true);
																					radio2 = Ext.getCmp("push_unique1");
																					radio2.setValue(false);
																				}else{
																					var radio = Ext.getCmp("push_unique1");
																					radio.setValue(true);
																					var radio2 = Ext.getCmp("push_unique0");
																					radio2.setValue(false);
																				}
																				
																				
																				if(keyords.reader.jsonData.keywords[k].enabled==false){
																					radio = Ext.getCmp("enabled0");
																					radio.setValue(true);
																					radio2 = Ext.getCmp("enabled1");
																					radio2.setValue(false);
																				}else{
																					radio = Ext.getCmp("enabled1");
																					radio.setValue(true);
																					radio2 = Ext.getCmp("enabled0");
																					radio2.setValue(false);
																				}
																				
																				
																				//var radio1 = simpleForm.findById('pushuniquer').items;
																				
																				//for(var x = 0; x<radio1.length; x++){
																				//	var radioButon = radio1[x];
																				//	alert(radioButon.boxLabel);
																				//}
																				
																				simpleForm.findById('service_name').setValue(keyords.reader.jsonData.keywords[k].service_name);
																				simpleForm.findById('service_description').setValue(keyords.reader.jsonData.keywords[k].service_description);
																				simpleForm.findById('price').setValue(keyords.reader.jsonData.keywords[k].price);
																				
																				
																				simpleForm.findById('price_point_keyword').setValue(keyords.reader.jsonData.keywords[k].price_point_keyword);
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
													labelSeparator : '',
													hidden : true,
													fieldLabel: 'ServiceID',
													id : 'id',
													name : 'smsservice.id',
													anchor : '95%'
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
													    hidden : true,
														fieldLabel: 'Keyword',
														name: 'smsservice.cmd',
													    anchor : '95%'
													},{
														xtype : 'radiogroup',
														fieldLabel : 'Push Unique?',
														columns : 2,
														id: 'pushuniquer',
														items : [{
															boxLabel : 'YES',
															id: 'push_unique1',
															name : 'smsservice.push_unique',
															inputValue : 'true'
														}, {
															boxLabel : 'NO',
															id: 'push_unique0',
															name : 'smsservice.push_unique',
															inputValue : 'false'
														}],
														anchor : '100%'
													}/*{
														 xtype : 'textfield',
														 id: 'push_unique',
														fieldLabel: 'Push Unique',
														name: 'smsservice.push_unique',
													    anchor : '95%'
													}*/,{
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
														xtype : 'radiogroup',
														fieldLabel : 'Enabled',
														columns : 2,
														id: 'enabledid',
														items : [{
															boxLabel : 'YES',
															id: 'enabled1',
															name : 'smsservice.enabled',
															inputValue : 'true'
														}, {
															boxLabel : 'NO',
															id: 'enabled0',
															name : 'smsservice.enabled',
															inputValue : 'false'
														}],
														anchor : '100%'
													},/*{
														 xtype : 'textfield',
														 id: 'enabled',
														fieldLabel: 'Enabled',
														name: 'smsservice.enabled',
													    anchor : '95%'
													}*/{
														 xtype : 'textfield',
														 id: 'split_mt',
														fieldLabel: 'Split SMS into 160 xters',
														name: 'smsservice.split_mt',
													    anchor : '95%'
													},{
														 xtype : 'textfield',
														 id: 'event_type',
														fieldLabel: 'Event Type',
														name: 'smsservice.event_type',
													    anchor : '95%'
													}]
											},{
													columnWidth : .5,
													layout : 'form',
													items: [{
														 xtype : 'textarea',
														 id: 'subscriptionText',
														 emptyText : 'You\'ve successfully subscribed to <KEYWORD>. Reply STOP <KEYWORD> to unsubscribe. <PRICE>/- @ sms',
														fieldLabel: 'Subscription Text',
														name: 'smsservice.subscriptionText',
													    anchor : '100%'
													},
													{
														 xtype : 'textarea',
														 id: 'unsubscriptionText',
														// emptyText: 'Thank you for having subscribed to our service. Your subscription to this service has now been stopped',
														fieldLabel: 'Unsubscription Text',
														name: 'smsservice.unsubscriptionText',
													    anchor : '100%'
													},{
														 xtype : 'textarea',
														 id: 'tailText_subscribed',
														// emptyText : 'Cost 5/- @SMS. To unsubscribe reply "<KEYWORD HERE>" ',
														fieldLabel: 'Tail Text When Subscribed',
														name: 'smsservice.tailText_subscribed',
													    anchor : '100%'
													},{
														 xtype : 'textarea',
														 id: 'tailText_notsubscribed',
														//emptyText:  'To confirm your subscription, reply with "BUY"',
														fieldLabel: 'Tail Text When not Subscribed',
														name: 'smsservice.tailText_notsubscribed',
													    anchor : '100%'
													},{
													buttons: [
																{
																	text: 'Save',
																	iconCls : 'x-icon-save',
																	handler : function submitForm(){
																	
																		
																		 var form = Ext.getCmp('myForm').getForm();
																		 form.submit({
																				clientValidation: true,
																				url: '/ContentManagement.action?saveOrUpdateService=&',
																				waitMsg: 'Just a moment...',
																				params: {
																					_eventName: 'saveOrUpdate'
																				},
																				success: function (form, action) {
																					//form.findField('membership.id')
																					//	.setValue(action.result.id);
																					successAction(form, action);
																					//Ext.getCmp("customFieldSet").setValue("{'fields' : []}");
																					// Ext.getCmp('basicSchemeFormDetWin').close()
																				},
																				failure: function (form, action) {
																					Ext.getCmp("customFieldSet").setValue("{'fields' : []}");
																					failureAction(form, action);
																				}
																			});
																		 
																		 
																		}
																	},{
																	text: 'Cancel',
																	iconCls : 'x-icon-close',
																	handler : function cancel(){
																		alert('Cancelled!');
																	}
																	}
															]
													}
												]
										}
										
										]
									}]
						});					

function loadLeftPanel(e){

	var l = e.autoEl.cn.length;
	var content_id = e.id.split('_')[1];
	console.log('content_id : '+content_id);
	var uploadpannel = Ext.getCmp('uploadpannel');
	uploadpannel.setTitle('SMS Content Upload ['+e.autoEl.cn.substring(0,l-6)+']');
	var form = Ext.getCmp('contentUploadForm').getForm();
	form.findField('smsmenu.id').setValue(content_id);
	

}						
		
	var menuitem = new Ext.data.JsonStore({
			url : 'SMSMenuManagement.action?listMenu=&',
			root : 'menuitem',
			fields : [{
						name : 'id',
						type : 'int'
					}, {
						name : 'name'
					}, {
						name : 'language_id'
					}, {
						name : 'parent_level_id'
					}, {
						name : 'menu_id'
					}, {
						name : 'serviceid'
					}, {
						name : 'visible'
					}, {
						name : 'children'
					}]
		});
		menuitem.load();

		
		var staticAction = function(action){
 
			var smsmenuNavPanel = Ext.getCmp('smsmenuNavPanel');
		   for(var k = 0; k<menuitem.reader.jsonData.size;k++){
				
				var subList = [];
				if(menuitem.reader.jsonData.menuitem[k].children.length>0)
				for(var i = 0; i<menuitem.reader.jsonData.menuitem[k].children.length; i++){
				   var child = menuitem.reader.jsonData.menuitem[k].children[i];
					console.log(child.name + ".. " + child.id);
					subList[i] = 
					           {
								    xtype:'box',
									isFormField: true,
									id: "link_"+child.id,
									style: "padding: 5px",
									autoEl:{
										tag: 'a',
										href: '#',
										cn: child.name +" <br/>"
									},
									listeners: {
											render: function(component) {
												component.getEl().on('click', function(e) {
													loadLeftPanel(component);
												});    
											}
									}
								};
//					+= "<button class='x-menu-item' id='"+child.id+"'>"+child.name+"</button><br/>";
				}
				
				console.log('subList.length: '+subList.length);
				
				smsmenuNavPanel.add({
					title: menuitem.reader.jsonData.menuitem[k].name,
					border:false,
					autoScroll:true,
					iconCls:'settings',
					items : subList
					});
		    }

		//smsmenuNavPanel.add(anotj);
		smsmenuNavPanel.doLayout();
				
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
                width:840,
                height:580,
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
							items: [simpleForm],
							width:840,
							height:550,
						},
						{
					    id: 'content_upload_tab',
						title:'Content Upload', 
						width:840,
						height:550,
						 layout:'border',
						tbar: new Ext.Toolbar({
								enableOverflow: true,
								items: [{
									xtype:'splitbutton',
									text: 'Content Type',
									iconCls: 'add16',
									handler: handleAction.createCallback('Menu Button'),
									menu: [
									{text: 'Static Content', handler: staticAction.createCallback('Static Content')},
									{text: 'Dynamic Content', handler: handleAction.createCallback('Dynamic Content')}]
								},'-',{
									xtype:'splitbutton',
									text: 'Cut',
									iconCls: 'add16',
									handler: handleAction.createCallback('Cut'),
									menu: [{text: 'Cut menu', handler: handleAction.createCallback('Cut menu')}]
								},{
									text: 'Copy',
									iconCls: 'add16',
									handler: handleAction.createCallback('Copy')
								},{
									text: 'Paste',
									iconCls: 'add16',
									menu: [{text: 'Paste menu', handler: handleAction.createCallback('Paste menu')}]
								},'-',{
									text: 'Format',
									iconCls: 'add16',
									handler: handleAction.createCallback('Format')
								},'->',{
									text: 'Right',
									iconCls: 'add16',
									handler: handleAction.createCallback('Right')
								}]
						}),
						items : [
							{
								region:'west',
								id:'smsmenuNavPanel',
								title:'SMS Menu',
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
									    id: 'uploadpannel',
										title: 'SMS Content Upload',
										items : [
										smsForm
										]
									}]
								}]
							}
						
						]
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
							
							
		staticAction();
	}						
		
		
});




function failureAction(form, action) {
	switch (action.failureType) {
		case Ext.form.Action.CLIENT_INVALID:
			Ext.Msg.show({
				title:'Failure',
				msg: 'Please fill the required fileds (Marked with *)',
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.ERROR
			});
			break;
		case Ext.form.Action.CONNECT_FAILURE:
			Ext.Msg.show({
				title:'Connection Failure',
				msg: 'We could not reach the server',
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.ERROR
			});
			break;
		case Ext.form.Action.SERVER_INVALID:
		   Ext.Msg.show({
				title:'Failure',
				msg: action.result.msg,
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.ERROR
		   });
		}
}
function successAction(form, action){
	Ext.Msg.show({
		title:'Success',
		msg: action.result.msg,
		buttons: Ext.Msg.OK,
		icon: Ext.Msg.INFO
	});
	//$.noticeAdd(action.result.msg)
	//$('.notification').addClass('notification').slideUp(200)
	//console.log('not ' + $('.notification'))
	//alert('alert')
}