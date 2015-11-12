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
							"<th scope='col'>CMP_Txid</th>" +
							"<th scope='col'>timeStamp" +
							"</th><th scope='col'>SUB_Mobtel</th>" +
							"<th scope='col'>MO_Received</th>" +
							"<th scope='col'>MT_Sent</th>" +
							"<th scope='col'>MT_STATUS</th>" +
							"<th scope='col'>dlrArrive</th>" +
							"<th scope='col'>source</th></TR></TABLE>");
					
					TRIVIA.statsdiv.html("");
					TRIVIA.statsdiv.css('width','90%');
					TRIVIA.statsdiv.append(table);
					
					//TRIVIA.statsdiv.html("");
					
					
					if(data.success=='true'){
						
						var y = data.CMP_Txid.length;
						
						
						for(var b = 0; b<y; b++){
							
							var CMP_Txid = unescape(data.CMP_Txid[b]);
							var timeStamp =  unescape(data.timeStamp[b]);
							var SUB_Mobtel =  unescape(data.SUB_Mobtel[b]);
							var MO_Received =  unescape(data.MO_Received[b]);
							var MT_Sent =  unescape(data.MT_Sent[b]);
							var MT_STATUS =  unescape(data.MT_STATUS[b]);
							var dlrArrive =  unescape(data.dlrArrive[b]);
							var source = unescape(data.source[b]);
							
							var spanC = (source == 'ussd') ? "red" : "green";
							table.append($("<TR>" +
							"<TD>"+CMP_Txid+"</TD>" +
							"<TD>"+timeStamp+"</TD>" +
							"<TD>"+SUB_Mobtel+"</TD>" +
							"<TD>"+MO_Received+"</TD>" +
							"<TD>"+MT_Sent+"</TD>" +
							"<TD>"+MT_STATUS+"</TD>" +
							"<TD>"+dlrArrive+"</TD>" +
							"<TD><span class='"+spanC+"'>"+source+"</span></TD></TR>"));
							
							//TRIVIA.statsdiv.append("<span class='lefters'>"+statusCode+" = "+count+" </span><br/>");
							
						}
						
						//TRIVIA.statsdiv.append("<span class='clearer'></span>");
						
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