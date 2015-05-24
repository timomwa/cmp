var GLOBAL_VARS = {};

(function getCurrentUserPermissions(){
if (window.XMLHttpRequest) {
AJAX = new XMLHttpRequest();
} else {
AJAX = new ActiveXObject("Microsoft.XMLHTTP");
}
if (AJAX) {
AJAX.open("GET", '/Permission.action', false);
AJAX.send(null);
var res = AJAX.responseText;
GLOBAL_VARS.currentRoles = eval('(' + res + ')');
} else {
return false;
}
})();

console.log(GLOBAL_VARS.currentRoles[0].permission);



var roles = new Ext.data.JsonStore({
	url: '/Permission.action',
    fields: [
		{name: 'permission'}
	],
	listeners : {
		load : function() {
			//GLOBAL_VARS.currentRoles = [];
			//this.count = this.getCount();
			//var records = this.getRange();
			//for(var i = 0; i<records.length; i++)
			//	GLOBAL_VARS.currentRoles.push(records[i].get("permission"));
			//console.log(GLOBAL_VARS.currentRoles);
			//return this.loaded;
			
		}
	}
});


function hasRole(role){
	
	for(var i = 0; i< GLOBAL_VARS.currentRoles.length; i++){
		console.log("has role "+role+" ? "+(GLOBAL_VARS.currentRoles[i].permission == role));
		if(GLOBAL_VARS.currentRoles[i].permission == role)
			return true;
	}
	return false;
}
