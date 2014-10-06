
<script type="text/javascript">	
			$(function(){
				
				  $('#phone_voucher_num').daterangepicker({arrows: true, dateFormat: 'yy-mm-dd'}); 
			 });
</script>
<div id="search">

<table width="100%" border="0">
	<tr>
		
		<th width="15%" align="left" scope="col">Date Range</th>
		
		<th>
		
			<input id="phone_voucher_num" type="text" value="Choose a Date" />
		
		</th>
		
		<th width="15%" align="left" scope="col">Phone/Voucher #</th>
		
		<th>
		
			<input type="text" value="Enter Phone/Voucher #" id="phone_voucher_num2" />
		
		</th>
		
		<th>
		
			<input type="button" value="Search" id="search" onclick="GIANT.search()"/>
		
		</th>
		
		
		
		
	</tr>
</table>
</div>

<div id="search_results" style="clear: both;">
</div>