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
			url : 'ContentManagement.action?listKeywords=&',
			root : 'keywords',
			fields : [{
						name : 'id',
						type : 'int'
					}, {
						name : 'price'
					}, {
						name : 'subscription_push_tail_text'
					}, {
						name : 'description'
					}, {
						name : 'keyword'
					}, {
						name : 'size'
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
			url : 'ContentManagement.action?listKeywords=&',
			root : 'keywords',
			fields : [{
						name : 'id',
						type : 'int'
					}, {
						name : 'price'
					}, {
						name : 'subscription_push_tail_text'
					}, {
						name : 'description'
					}, {
						name : 'keyword'
					}, {
						name : 'size'
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
							labelWidth: 75, // label settings here cascade unless overridden
							url:'ContentManagement.action?saveOrUpdateKeyword=&',
							frame:true,
							title: 'Keyword Management',
							bodyStyle:'padding:5px 5px 0',
							width: 450,
							scope: this,
							defaults: {width: 330},
							defaultType: 'textfield',
							margins : {
								top: 50,
								right: 50,
								bottom: 50,
								left: 50
							},

							items: [
								new Ext.form.ComboBox({
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
										minChars: 2,
										selectOnFocus:true,
										listeners: {
										'select': function(cmb, rec, idx) {
											for(var k = 0; k<keyords.reader.jsonData.size;k++){
												if(keyords.reader.jsonData.keywords[k].id==cmb.getValue()){
													simpleForm.findById('keyword_description').setValue(keyords.reader.jsonData.keywords[k].description);
													simpleForm.findById('keyword_price').setValue(keyords.reader.jsonData.keywords[k].price);
													simpleForm.findById('keyword_subscription').setValue(keyords.reader.jsonData.keywords[k].subscription_push_tail_text);
												}
											}
											 
										}
								}
									}),{
									id: 'keyword_description',
									fieldLabel: 'Description',
									name: 'keyword.description',
									allowBlank:false
								},{
									id: 'keyword_price',
									fieldLabel: 'Price',
									name: 'keyword.price'
								},{
								    id: 'keyword_subscription',
									fieldLabel: 'Subscription Push Text',
									name: 'keyword.subscription'
								}
							],

							buttons: [{
								text: 'Save'
							},{
								text: 'Cancel'
							}]
						});					
							
		Ext.QuickTips.init();

    // Menus can be prebuilt and passed by reference
    var dateMenu = new Ext.menu.DateMenu({
        handler: function(dp, date){
            Ext.example.msg('Date Selected', 'You chose {0}.', date.format('M j, Y'));
        }
    });

    var colorMenu = new Ext.menu.ColorMenu({
        handler: function(cm, color){
            Ext.example.msg('Color Selected', 'You chose {0}.', color);
        }
    });

    var store = new Ext.data.ArrayStore({
        fields: ['abbr', 'state'],
        data : keyword_combo // from states.js
    });

    var combo = new Ext.form.ComboBox({
        store: store,
        displayField: 'state',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select a state...',
        selectOnFocus: true,
        width: 135,
        getListParent: function() {
            return this.el.up('.x-menu');
        },
        iconCls: 'no-icon'
    });

    var menu = new Ext.menu.Menu({
        id: 'mainMenu',
        style: {
            overflow: 'visible'     // For the Combo popup
        },
        items: [
            combo,                  // A Field in a Menu
            {
                text: 'I like Ext',
                checked: true,       // when checked has a boolean value, it is assumed to be a CheckItem
                checkHandler: onItemCheck
            }, '-', {
                text: 'Radio Options',
                menu: {        // <-- submenu by nested config object
                    items: [
                        // stick any markup in a menu
                        '<b class="menu-title">Choose a Theme</b>',
                        {
                            text: 'Aero Glass',
                            checked: true,
                            group: 'theme',
                            checkHandler: onItemCheck
                        }, {
                            text: 'Vista Black',
                            checked: false,
                            group: 'theme',
                            checkHandler: onItemCheck
                        }, {
                            text: 'Gray Theme',
                            checked: false,
                            group: 'theme',
                            checkHandler: onItemCheck
                        }, {
                            text: 'Default Theme',
                            checked: false,
                            group: 'theme',
                            checkHandler: onItemCheck
                        }
                    ]
                }
            },{
                text: 'Choose a Date',
                iconCls: 'calendar',
                menu: dateMenu // <-- submenu by reference
            },{
                text: 'Choose a Color',
                menu: colorMenu // <-- submenu by reference
            }
        ]
    });

	var tb = Ext.get('myTolbar');
	if(!tb){
			tb = new Ext.Toolbar({id:'myTolbar' });
			tb.render('toolbar');
			tb.id = 'myTolbar',
			tb.add({
					text:'Button w/ Menu',
					iconCls: 'bmenu',  // <-- icon
					menu: menu  // assign menu by instance
				}, {
					text: 'Users',
					iconCls: 'user',
					menu: {
						xtype: 'menu',
						plain: true,
						items: {
							xtype: 'buttongroup',
							title: 'User options',
							autoWidth: true,
							columns: 2,
							defaults: {
								xtype: 'button',
								scale: 'large',
								width: '100%',
								iconAlign: 'left'
							},
							items: [{
								text: 'User<br/>manager',
								iconCls: 'edit'
							},{
								iconCls: 'add',
								width: 'auto',
								tooltip: 'Add user'
							},{
								colspan: 2,
								text: 'Import',
								scale: 'small'
							},{
								colspan: 2,
								text: 'Who is online?',
								scale: 'small'
							}]
						}
					}
				},
				new Ext.Toolbar.SplitButton({
					text: 'Split Button',
					handler: onButtonClick,
					tooltip: {text:'This is a an example QuickTip for a toolbar item', title:'Tip Title'},
					iconCls: 'blist',
					// Menus can be built/referenced by using nested menu config objects
					menu : {
						items: [{
							text: '<b>Bold</b>', handler: onItemClick
						}, {
							text: '<i>Italic</i>', handler: onItemClick
						}, {
							text: '<u>Underline</u>', handler: onItemClick
						}, '-', {
							text: 'Pick a Color',
							handler: onItemClick,
							menu: {
								items: [
									new Ext.ColorPalette({
										listeners: {
											select: function(cp, color){
												Ext.example.msg('Color Selected', 'You chose {0}.', color);
											}
										}
									}), '-',
									{
										text: 'More Colors...',
										handler: onItemClick
									}
								]
							}
						}, {
							text: 'Extellent!',
							handler: onItemClick
						}]
					}
				}), '-', {
				text: 'Toggle Me',
				enableToggle: true,
				toggleHandler: onItemToggle,
				pressed: true
			});

			menu.addSeparator();
			// Menus have a rich api for
			// adding and removing elements dynamically
			var item = menu.add({
				text: 'Dynamically added Item'
			});
			// items support full Observable API
			item.on('click', onItemClick);

			// items can easily be looked up
			menu.add({
				text: 'Disabled Item',
				id: 'disableMe'  // <-- Items can also have an id for easy lookup
				// disabled: true   <-- allowed but for sake of example we use long way below
			});
			// access items by id or index
			menu.items.get('disableMe').disable();

			// They can also be referenced by id in or components
			tb.add('-', {
				icon: 'list-items.gif', // icons can also be specified inline
				cls: 'x-btn-icon',
				tooltip: '<b>Quick Tips</b><br/>Icon only button with tooltip'
			}, '-');

			var scrollMenu = new Ext.menu.Menu();
			for (var i = 0; i < 50; ++i){
				scrollMenu.add({
					text: 'Item ' + (i + 1)
				});
			}
			// scrollable menu
			tb.add({
				icon: 'preview.png',
				cls: 'x-btn-text-icon',
				text: 'Scrolling Menu',
				menu: scrollMenu
			});

			// add a combobox to the toolbar
			var combo = new Ext.form.ComboBox({
				store: store,
				displayField: 'state',
				typeAhead: true,
				mode: 'local',
				triggerAction: 'all',
				emptyText:'Select a state...',
				selectOnFocus:true,
				width:135
			});
			tb.addField(combo);

			tb.doLayout();
			
			

			// functions to display feedback
			function onButtonClick(btn){
				Ext.Msg.alert('Button Click','You clicked the "'+btn.text+'" button.');
			}

			function onItemClick(item){
				Ext.Msg.alert('Menu Click', 'You clicked the "'+item.text+'" menu item.');
			}

			function onItemCheck(item, checked){
				Ext.Msg.alert('Item Check', 'You '+(checked ? 'checked' : 'unchecked')+'the "'+item.text+'" menu item.' );
			}

			function onItemToggle(item, pressed){
				Ext.Msg.alert('Button Toggled', 'Button "'+item.text+'" was toggled to '+pressed+'.');
			}
		
	}
		
	}
});