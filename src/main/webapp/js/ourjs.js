var TRIVIA = {
		
		
		showDesc : function(desc){
			
			var detailsPopup = $('<div class="roundCorners" id="assignPopUp"></div>');
			detailsPopup.css('border','2px solid #8cb8c3');
			//detailsPopup.css('-moz-border-radius','12px');
			detailsPopup.css('position','absolute');
			detailsPopup.css('z-index','9999999');
			detailsPopup.css('display','none');
			detailsPopup.css('color','#000');
			detailsPopup.css('opacity','0.85');
			detailsPopup.css('background','white');
			detailsPopup.css('padding','5px');
			detailsPopup.css('font-size','10px');
			detailsPopup.css('font-family','Verdana, Tahoma, Arial, "Bitstream Vera Sans", helvetica,"sans serif"');
			detailsPopup.css('font-style','italic');
			detailsPopup.appendTo('body');
			detailsPopup.fadeIn('500');
			detailsPopup.html("<u><b>"+desc+"<b></u>");
			
			positionAsSource('assignPopUp');
			
		},
		
		
		getLogsFor : function(){
			
			var date = $('#date').val();
			var msisdn = $('#msisdn').val();
			
			TRIVIA.statsdiv = $('#stat_div');
			
			
			TRIVIA.statsdiv.html("<center><img src='images/loading.gif' alt='loading' /></center>");
			
			$.ajax({
				
				type : "POST",
				
				url: 'msisdnController',
				
				data: "{\"command\": \"getLogsFor\", \"msisdn\" : \""+msisdn+"\", \"date\": \""+date+"\"}",
				
				success: function(data){
					
					$('#mytable').detach();
					var table = $("<TABLE class='tiny_font' id='mytable' cellspacing='0'><TR id='header'>" +
							"<th scope='col'>Id</th>" +
							"<th scope='col'>Time" +
							"</th><th scope='col'>Tx No.</th>" +
							"<th scope='col'>First Name</th>" +
							"<th scope='col' style='width:150px'>Middle Name</th>" +
							"<th scope='col'>Last Name</th>" +
							"<th scope='col'>Paid in</th>" +
							"<th scope='col'>Agg. Balance</th>" +
							"<th scope='col'>Bussiness Code</th>" +
							"<th scope='col'>Trans. Type</th></TR></TABLE>");
					
					TRIVIA.statsdiv.html("");
					TRIVIA.statsdiv.css('width','90%');
					TRIVIA.statsdiv.append(table);
					
					//TRIVIA.statsdiv.html("");
					
					
					if(data.success=='true'){
						
						var y = data.CMP_Txid.length;
						
						
						for(var b = 0; b<y; b++){
							
							var id = unescape(data.id[b]);
							var billRefNumber = unescape(data.billRefNumber[b]);
							var businessShortcode = unescape(data.businessShortcode[b]);
							var first_name = unescape(data.first_name[b]);
							var last_name = unescape(data.last_name[b]);
							var middle_name = unescape(data.middle_name[b]);
							var orgAccountBalance = unescape(data.orgAccountBalance[b]);
							var raw_xml_id = unescape(data.raw_xml_id[b]);
							var sourceip  = unescape(data.sourceip[b]);
							var timeStamp  = unescape(data.timeStamp[b]);
							var transAmount  = unescape(data.transAmount[b]);
							var transId  = unescape(data.transId[b]);
							var transType  = unescape(data.transType[b]);
							var status  = unescape(data.status[b]);
							
							var spanC = (transAmount <50) ? "red" : "green";
							
							table.append($("<TR>" +
							"<TD>"+id+"</TD>" +
							"<TD>"+timeStamp+"</TD>" +
							"<TD>"+transId+"</TD>" +
							"<TD>"+first_name+"</TD>" +
							"<TD>"+middle_name+"</TD>" +
							"<TD>"+last_name+"</TD>" +
							"<TD><span class='"+spanC+"'>"+transAmount+"</span></TD>" +
							"<TD>"+businessShortcode+"</TD>" +
							"<TD>"+transType+"</TD></TR>"));
							
						}
						
					}else{
						
						if(data.message)
							alert(data.message);
						else
							alert('ERROR:: There was a problem fetching the subs\'s data. Try again later or contact tech.');
					}
					
				},
				
				dataType: 'json'
			
			});
			
			
			
		},
		
		
		getTxProfile : function(){
			
			var date = $('#date').val();
			var msisdn = $('#msisdn').val();
			
			TRIVIA.statsdiv = $('#stat_div');
			
			
			TRIVIA.statsdiv.html("<center><img src='images/loading.gif' alt='loading' /></center>");
			
			$.ajax({
				
				type : "POST",
				
				url: 'msisdnController',
				
				data: '{\'command\': \'getMsisdnProfile\', \'msisdn\' : \''+msisdn+'\', \'date\': \''+date+'\'}',
				
				success: function(data){
					
					TRIVIA.statsdiv.html("");
					TRIVIA.statsdiv.css('width','310px');
					//loader.detach();
					
					if(data.success=='true'){
						
						var y = data.statusCode.length;
						
						if(y>0)
						var tabl = $("<table class='tiny_font'><TR><th scope='col'>StatusCode</th><th scope='col'>count</th><th scope='col'>chargeable</th></TR></table>");
						
						for(var b = 0; b<y; b++){
							
							var statusCode = data.statusCode[b];
							var count = data.count[b];
							var price = data.price[b];
							var chargeable = price>0 ? "true" : "false";
							var cl = price>0 ? "green" : "norm";
							
							tabl.append("<TR><TD>"+statusCode+"</TD><TD>"+count+"</TD><TD> <span class='"+cl+"'> "+chargeable+"</span></TD></TR>");
							//TRIVIA.statsdiv.append("<span class='lefters'>"+statusCode+" = "+count+" </span> <span class='"+cl+"'> "+chargeable+"</span><br/>");
							
						}
						
						TRIVIA.statsdiv.append(tabl);
						
					}else{
						
						if(data.message)
							alert(data.message);
						else
							alert('ERROR:: There was a problem fetching the subs\'s data. Try again later or contact tech.');
					}
					
				},
				
				dataType: 'json'
			
			});
			
			
			
		},
		
		
	
		loadText : function(event){
			
			//var selectObj = $(event.data.data[0]);
			
			var key = $GSO('KEY');
			var language_id = $GSO('LANGUAGE');
			
			TRIVIA.table = $('#mytable');
			
			TRIVIA.table.find('tr').each(
					
					function(){
						var id = $(this).attr('id');
						if(id!='header'){
							$(this).detach();
						}
					}
			);
			
			var loader = $("<TR id='loader'><TD colspan='5' align='center'><img src='images/loading.gif' alt='loading' /></TR>");
			TRIVIA.table.append(loader);
			$.ajax({
				
				type : "POST",
				
				url: 'controller',
				
				data: '{\'command\': \'getRespTexts\', \'key\' : \''+key+'\', \'language_id\': \''+language_id+'\'}',
				
				success: function(data){
					
					loader.detach();
					
					if(data.success=='true'){
						
						var y = data.id.length;
						for(var b = 0; b<y; b++){
							var id = data.id[b];
							var size =  parseInt(data.size[b]);
							var lang =  data.lang[b];
							var message = data.message[b];
							var description =  unescape(data.description[b]);
							var dbKey =  data.key[b];
							
							var spanC = (size > parseInt(80)) ? "red" : "green";
							
							var row = $("<TR id='resp_rec_"+id+"'> <TD id='msg_msg_"+id+"' width='250'> <textarea class=\"tastyled\"" +
									"onfocus=\"setbg('#e5fff3');\" onblur=\"setbg('white')\"  id='msg_txt_"+id+"' rows=\"4\" cols= \"80\" > "+message+" </textarea> " +
											"<img class='pntz lefters' src='images/save.png' alt='Save' onclick=\"saveText('"+id+"')\"/> <span class='clearer'>&nbsp;</span></TD> " +
									"<TD id='resp_lang_"+id+"' class='tiny_font "+spanC+"'> <span id='"+id+"_span' class='"+spanC+"'/> "+size+" </TD> <TD id='resp_lang_"+id+"' class='tiny_font'> "+lang+" </TD> <TD width='50' id='resp_desc_"+id+"' class='medium_font'> "+description
									+" </TD> <TD id='resp_key_"+id+"' class='tiny_font'> "+dbKey+" </TD>  </TR>");
							TRIVIA.table.append(row);
							$('#'+id+'_span').css(spanC);
							
						}
						
						
					}else{
						alert('ERROR:: There was a problem fetching the response text in seletion');
					}
					
				},
				
				dataType: 'json'
			
			});
	
		}
}




$(function () {
	
	$('select').each(function() {
		var obj = $(this);
		
		//alert(obj.attr('id'));
		$(this).change({data: obj},TRIVIA.loadText);
		
	});

	/*$('select').find('option').each(function() {
	
		$(this).bind('click', function(event){
			alert('can load text');
		});
               // alert($(this).val());
 	});*/


});





function saveText(id){
	
	var daObj = $('#msg_txt_'+id);
	
	if (confirm("Change text to :::  \n\n"+daObj.val()+ " \n\n\n? By clicking \"OK\" the return text will be changed immediately. \n\nPlease bear" +
	" in mind that these texts are live. ")) { 
	
			var respText = daObj.val();
			
			//Make Ajax request to save response text..
			$.ajax({
				
				type : "POST",
				
				url: 'controller',
				
				data: '{\'command\': \'changeRespTxt\', \'text\' : \''+escape(respText)+'\', \'id\': \''+id+'\'}',
				
				success: function(data){
					
					if(data.success=='true'){
						alert('Response text successfully changed to:\n\n'+respText+'\n\n\n');
					}else{
						alert('ERROR:: There was a problem when changing the response text');
					}
					
				},
				
				dataType: 'json'
			
			});


	}
	
}