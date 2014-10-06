var GIANT = {
		
		HOME_PAGE : 'home.jsp',
		
		CURRENT_PAGE : '',
		
		CASHIER_PAGE : 'cashier.jsp',
		
		STATS_PAGE : 'stats.jsp',
		
		LOGIN_PAGE : 'login.jsp',
		
		SUPER_USER : 0,//CAN READ,EDIT,DELETE ANYTHING EDITABLE
		
		ADMIN_USER : 1,//CONTENT IS READ ONLY ACCESS, CAN EDIT THEIR ACCOUNT AND THOSE FOR OTHER USERS.
		
		NORMAL_USER : 2,//CONTENT IS READ ONLY ACCESS, CAN EDIT THEIR ACCOUNT ONLY (change password only as well as first and last name)
		
		READ_ONLY : 3,//CONTENT IS READ ONLY ACCESS, CAN'T EDIT THEIR ACCOUNT

		LOG_IN : 0,
		
		LOG_OUT : 1,
		
		SEARCH : 2,
		
		REDEEM_VOUCHER : 3,
		
		VIEW_STATS : 4,
		
		current_voucher_id : -1,
		
		loading : '<div id="loading_gif_in"><img  src="images/loading.gif" alt="loading..."/>&nbsp;Loading... Please wait<br/></div>',
		
		loading1 : '<div id="loading_gif_in2"><img  src="images/loading.gif" alt="loading..."/><br/></div>',
		
		navigate: function(el){
			var name = null;
			if(el.name)
				name = el.name+'.jsp';
			else
				name = el;
			$('#content').load(name);
		},
		
		
		showLoading : function(){
			$('#search_results').first().before(GIANT.loading);
		},
		
		hideLoading : function(){
			$('#loading_gif_in').remove();
		},
		
		
		downloadStats : function(){
			
			var date_range = $('#phone_voucher_num').val();
			
			GIANT.showLoading();
			$.ajax({
				
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'downloadStats\', \'date_range\' : \''+date_range+'\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
			
		},
		
		
		viewStats : function(){
			
			var date_range = $('#phone_voucher_num').val();
			
			GIANT.showLoading();
			$.ajax({
				
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'viewStats\', \'date_range\' : \''+date_range+'\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
			
		},
		
		
		search : function(){
			var date_range = $('#phone_voucher_num').val();
			var phone_number = $('#phone_number').val();
			var voucher_number = $('#voucher_number').val();
			if(date_range==null){
				date_range = '';
			}
			
			
			GIANT.showLoading();
			$.ajax({
				
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'search\', \'date_range\' : \''+date_range+'\', \'voucher_number\': \''+voucher_number+'\', \'phone_number\': \''+phone_number+'\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
		},
		
		
		
		redeemVoucher : function(voucher_id){
			//var date_range = $('#phone_voucher_num').val();
			
			GIANT.current_voucher_id = voucher_id;
			var vnid = '#voucher_num_'+voucher_id;
			var msisdn_id = '#msisdn_'+voucher_id;
			var voucher_num = $(vnid).html();
			var msisdn = $(msisdn_id).html();
			
			GIANT.showLoading();
			
			$.ajax({
				
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'redeem_voucher\', \'voucher_id\' : \''+voucher_id+'\', \'voucher_num\': \''+voucher_num+'\', \'msisdn\': \''+msisdn+'\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
		},
		
		
		standardAjaxResponseAction : function(data){
			GIANT.hideLoading();
			
		
			
			if(data.success[0]==true){
				
				if(data.action[0]==GIANT.LOG_IN){
					
					var role_id = data.data[0].role_id;
					
					//alert('role_id  = '+role_id+' \n GIANT.READ_ONLY = ' +GIANT.READ_ONLY+' \n(role_id==GIANT.READ_ONLY) = '+(role_id==GIANT.READ_ONLY));
					
					if((role_id==GIANT.READ_ONLY)){
						GIANT.CURRENT_PAGE = GIANT.CASHIER_PAGE;
						$.cookie('current_page',GIANT.CASHIER_PAGE);
						$('#main_div').load(GIANT.CASHIER_PAGE);
					}else if((role_id==GIANT.ADMIN_USER)){
						GIANT.CURRENT_PAGE = GIANT.STATS_PAGE;
						$.cookie('current_page',GIANT.STATS_PAGE);
						//$('#main_div').load(GIANT.STATS_PAGE);
						$(location).attr('href',GIANT.STATS_PAGE);
					}else{
						GIANT.CURRENT_PAGE = GIANT.HOME_PAGE;
						$.cookie('current_page',GIANT.HOME_PAGE);
						$('#main_div').load(GIANT.HOME_PAGE);
					}
					
					if(data.data==null){
						$('#status_span').html(GIANT.loading1+'&nbsp;'+data.message[0]+'<br/>');
					}
					
					$('#loading_gif_in2').remove();
					GIANT.hideLoading();
					
				}else if(data.action[0]==GIANT.LOG_OUT){
					
					GIANT.CURRENT_PAGE = GIANT.LOGIN_PAGE;
					$.cookie('current_page',GIANT.LOGIN_PAGE);
					$('#main_div').load(GIANT.LOGIN_PAGE);
				
				}else if(data.action[0]==GIANT.REDEEM_VOUCHER){
					
					GIANT.hideLoading();
					//$('#main_div').load(GIANT.LOGIN_PAGE);
					var voucher_table = $('#voucher_row_'+GIANT.current_voucher_id);
					//alert(voucher_table.html());
					
					voucher_table.find("td").each(function(){
						$(this).attr('class', 'looser');
					});
					var button = $('#button_'+GIANT.current_voucher_id);
					button.remove();
					
				
				}else if(data.action[0]==GIANT.SEARCH){
				
					
					$('#search_results').html('');
					$('#search_results').html(data.message[0]);
					
					var length = data.data[0].msisdn.length;
					//$('#search_results').html(data.data[0].prize_description[0]+' size: '+length);
					
					var table = $('<table class="imagetable"></table>');
					table.append('<tr><th>Status</th><th>Voucher/Random#</th><th>Phone#</th> <th>Date Awarded</th> <th>Date Redeemed</th> <th>Prize Name</th> <th>Description</th><th>Redeemable</th></tr>');
					
					$('#search_results').append(table);
					
					if(length>0){
						
						for(var i = 0; i<length;i++){
							
							var redeemStr = '';
							var voucher_id = data.data[0].voucher_id[i];
							
						   var rwc = 'class="looser"';
							
							var used = '<span class="non_redeemable">Non-redeemable</span>';
							if((data.data[0].winning[i]==1) && (data.data[0].used[i]==0)){
								 redeemStr = '<button class="search_btn" id="button_'+voucher_id+'" onclick="GIANT.redeemVoucher(\''+voucher_id+'\')">Redeem Voucher</button>';
								 rwc = 'class="winner"';
								 used = '<img class="voucher_c" src="images/voucher.png" alt="VOUCHER_VALID"/><br/><span class="redeemable">Redeemable</span>';
							}
							
							if(data.data[0].used[i]==1){
								 used = '<img class="voucher_c" src="images/voucher.png" alt="VOUCHER_VALID"/><br/><span class="redeemablef">Already redeemed</span>';
							}
							
							
							
							
							table.append('<tr id="voucher_row_'+voucher_id+'"><td '+rwc+' id="status_'+voucher_id+'">'+used+'</td><td '+rwc+' id="voucher_num_'+voucher_id+'">'+data.data[0].voucherNumber[i]
							+'</td><td '+rwc+' id="msisdn_'+voucher_id+'">'+data.data[0].msisdn[i]+'</td> <td '+rwc+'>'+data.data[0].timeStamp_awarded[i]
							+'</td> <td '+rwc+'>'+data.data[0].timeStamp_used[i]+'</td> <td '+rwc+'>'+data.data[0].prize_name[i]
							+'</td> <td '+rwc+'>'+data.data[0].prize_description[i]+'</td><td  '+rwc+' id="voucher_row'+voucher_id+'">'+redeemStr+'</td></tr>');
							
							/*var table = $('<table></table>');
							
							
							alert('data.data[0].winning[i] = '+data.data[0].winning[i] + "data.data[0].winning[i]==1 : "+(data.data[0].winning[i]==1));
							
							
							
							if(data.data[0].winning[i]==1){
								$('#search_results').append(table);
								table.append('<tr id="voucher_row'+voucher_id+'"><td class="left_table_info">&nbsp;</td><td><button onclick="GIANT.redeemVoucher(\''+voucher_id+'\')">Redeem Voucher</button></td></tr>');
							}
							
							var table_inner = $('<table class="imagetable" id="voucher_table_'+voucher_id+'"></table>');
							
							$('.left_table_info').append(table_inner);
							
							table_inner.append('<tr><td>Ticket #</td><td id="voucher_num_'+voucher_id+'">'+data.data[0].voucherNumber[i]+'</td></tr>');
							table_inner.append('<tr><td>Msisdn</td><td id="msisdn_'+voucher_id+'">'+data.data[0].msisdn[i]+'</td></tr>');
							table_inner.append('<tr><td>Date Awarded:</td><td>'+data.data[0].timeStamp_awarded[i]+'</td></tr>');
							table_inner.append('<tr><td>Date Redeemed:</td><td>'+data.data[0].timeStamp_used[i]+'</td></tr>');
							table_inner.append('<tr><td>Prize Name:</td><td>'+data.data[0].prize_name[i]+'</td></tr>');
							table_inner.append('<tr><td>Prize Description: </td><td>'+data.data[0].prize_description[i]+'</td></tr>');*/
						}
					
					}
			
				}else if(data.action[0]==GIANT.VIEW_STATS){
					GIANT.hideLoading();
					$('#search_results').html('');
					$('#search_results').html(data.message[0]);
					//alert(data);
					var length = data.data[0].prize_value_claimed.length;
					
					if(length>0){
					$('#search_results').append('<div id="search_results_" ><br/><H1 style="float:left">CLAIMED<H1><br/></div>');
					var table = $('<table class="imagetable"  border="1"></table>');
					table.append('<tr><th>Date Awarded</th><th>Store Name</th><th>Claimed</th></tr>');
					
					$('#search_results_').append(table);
					for(var i = 0; i<length; i++){
					
						if(data.data[0].store_name[i]!='Unclaimed'){
						table.append('<tr><td>'+data.data[0].time_Awarded[i]+'</td><td>'+data.data[0].store_name[i]+'</td><td>'+data.data[0].count[i]+'</td></tr>');
						data.data[0].prize_value_claimed[i];
						data.data[0].count[i];
						data.data[0].claimed_count[i];
						data.data[0].time_Awarded[i];
						data.data[0].store_id_fk[i];
						data.data[0].prize_value_claimed[i];
						}
					}
					
					
					$('#search_results_').append('<br/><br/><H1 style="float:left">UNCLAIMED<H1><br/>');
					table = $('<table class="imagetable"  border="1"></table>');
					table.append('<tr><th>Date Awarded</th><th>Store Name</th><th>Unclaimed</th></tr>');
					$('#search_results_').append(table);
					
					for(var i = 0; i<length; i++){
						if(data.data[0].store_name[i]=='Unclaimed'){
						table.append('<tr><td>'+data.data[0].time_Awarded[i]+'</td><td>'+data.data[0].store_name[i]+'</td><td>'+data.data[0].count[i]+'</td></tr>');
						data.data[0].prize_value_claimed[i];
						data.data[0].count[i];
						data.data[0].claimed_count[i];
						data.data[0].time_Awarded[i];
						data.data[0].store_id_fk[i];
						data.data[0].prize_value_claimed[i];
						}
					}
					
					}
					
				}
				
			}
			
			
		},
		
		logout: function(){
			
			$.ajax({
				
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'logout\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
			
		},
		
		login : function(){
			
			var username = $('#login1').val();
			var password = $('#login2').val();
			$('#status_span').append(GIANT.loading);
			$.ajax({
			
				type : "POST",
				
				url: 'giant',
				
				data: '{\'command\': \'login\', \'username\' : \''+username+'\', \'password\': \''+password+'\'}',
				
				success: GIANT.standardAjaxResponseAction,
				
				dataType: 'json'
			
			});
			
		},
		
		
		
		end : function(){
			
		}
}