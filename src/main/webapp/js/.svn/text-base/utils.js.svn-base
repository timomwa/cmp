/**
 * @author Timothy Mwangi
 * Date created, sometime between Jan 2010 and October 2010
 * Really cool scripts here.. take a closer look :)
 */

var progressBar;

var IE = document.all?true:false;

if (!IE){ document.captureEvents(Event.MOUSEMOVE)
	document.onmousemove = getMouseXY;
}

var tempX = 0;
var tempY = 0;
var theDiv;

function positionAsSource(theDivId){
	
	
	theDiv = document.getElementById(theDivId);
	
	if (IE){
		theDiv.style.minWidth = '250px';
		theDiv.style.minHeight = '90px';
	}
	var height = theDiv.offsetHeight;
	var width = theDiv.offsetWidth;
	theDiv.style.position = 'absolute';
	theDiv.style.top = (tempY-height); //+ 'px';
	theDiv.style.left = (tempX-width); //+ 'px';
	
	//if(IE){
		//setTimeout('positionIE()',500);
	//}
	
}


function positionIE(){
	var height = theDiv.offsetHeight;
	var width = theDiv.offsetWidth;
	theDiv.style.position = 'absolute';
	theDiv.style.top = (tempY-(height*0.5)); //+ 'px';
	theDiv.style.left = (tempX-(width*0.5)); //+ 'px';
}


function getMouseXY(e) {
	
	if (IE) { // grab the x-y pos.s if browser is IE
		
		tempX = event.clientX + document.body.scrollLeft;
		
		tempY = event.clientY + document.body.scrollTop;
	
	}else {  // grab the x-y pos.s if browser is NS
		
		tempX = e.pageX;
		tempY = e.pageY;
	}
	
	if (tempX < 0){tempX = 0;}
	if (tempY < 0){tempY = 0;}  
	
	//alert('tempX: '+tempX+'tempY: '+tempY);
	//document.Show.MouseX.value = tempX;
	//document.Show.MouseY.value = tempY;
	return true;
}


function positinToCenter(theLight){
	var windowWidth = 0;
	var windowHeight = 0;
		
	if (typeof(window.innerWidth) == 'number') {
            //Non-IE
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        }
        else 
            if (document.documentElement &&
            (document.documentElement.clientWidth ||
            document.documentElement.clientHeight)) {
                //IE 6+ in 'standards compliant mode'
                windowWidth = document.documentElement.clientWidth;
                windowHeight = document.documentElement.clientHeight;
            }
            else 
                if (document.body && (document.body.clientWidth || 
					document.body.clientHeight)) {
                    //IE 4 compatible
                    windowWidth = document.body.clientWidth;
                    windowHeight = document.body.clientHeight;
                }
		var topPos = ((windowHeight/2) - (theLight.offsetHeight*2));
		var botPos = ((windowWidth/2) - (theLight.offsetWidth/2));	
		var percTop = (topPos/windowHeight) * 100;
		var percLeft = (botPos/windowWidth) * 100;
		theLight.style.position = 'absolute';
		theLight.style.top = (percTop) + '%';	
		theLight.style.left = (percLeft) + '%';
		var BROWSERDIMENSIONS =  {"browser":{"left":percLeft, "top":percTop}};	
}

function $SP(dDiv){
	$HP();
	var idOfWin = dDiv;
	progressBar = document.createElement('div');
	var light = document.getElementById(idOfWin);
	if(light!=null)
		light.appendChild(progressBar);
	else
		document.getElementById('light2').appendChild(progressBar);
	progressBar.innerHTML = '<center><div style="background: #FFF6E6;"><img src="images/loginLoader.gif"></div></center>';
	progressBar.id = 'progressBar';
	progressBar.zIndex = 99999999;
}

function $HP(){
	$D('progressBar');
}

function $G(id){
	return document.getElementById(id);
}

function $GV(id){
	return $G(id).value;
}


function $RED(id){
	$G(id).style.color = 'red';
}

function $D(id){
	var unwanted = $G(id);
	if(unwanted)
		unwanted.parentNode.removeChild(unwanted);
}


/**
 * Appends content of a given url
 * to the parent node of the source.
 * @param url the url
 * @param source the source Div
 * 
 */
function appendToPage(url, source){
	var holder = source.parentNode;
	$(holder).load(url);
}


/**
 * Appends content of a given url
 * to the given div
 * @param url the url
 * @param theDv the div you want
 * to append to.
 * 
 */
function appendToDiv(url, theDv){
	$(theDv).load(url);
}

/**
 * 
 * @param a first value
 * @param b second value
 * @return true if they are identical
 */
function theyMatch(a,b){
	return (a==b);
}


/**
 * Toggles visibility
 * @param thatDiv
 * @return
 */
function toggleVisibility(thatDiv){
	var display = thatDiv.style.display;
	if(display=='block'){
		thatDiv.style.display = 'none';
	}else{
		thatDiv.style.display = 'block';
	}
}


function toggleVisibilityId(id){
	
	var thatDiv = $G(id);
	var display = thatDiv.style.visibility;
	alert(display);
	if(display=='block'){
		thatDiv.style.display = 'none';
	}else{
		thatDiv.style.display = 'none';
	}
}


/**
 * Shows the error message on a
 * pop up window
 * @param message the message to be shown
 * @param error the error
 * @param stay boolean whether to remove the note
 * after some time
 * @return false
 */
function notify(message, error, stay){
	$D('error');
	var light = $('#light');//.append("<center><div id='error' class='popupwindowContent'>"+message+"</div><center>");
	if(light.html()==null)
		$('#light2').append("<center><div id='error' class='popupwindowContent'>"+message+"</div><center>");
	else
		$('#light').append("<center><div id='error' class='popupwindowContent'>"+message+"</div><center>");
	if(error)
		$RED('error');
	
	if(stay){
		//N/A
	}
	else{
		setTimeout("$D('error')", 7000);
	}
}

/**
 * pass ID's only
 * @param element id of element to be moved
 * @param dest id of destination element 
 * @return
 */
function $M(element, dest){
	var el = $G(element);
	var d = $G(dest);
	var or = el;
	if(el.onclick.toString()==function onclick(event) {CUEA.setCourse(this);}){
		el.onclick=function onclick(event) {CUEA.deselectCourse(this);}
	}else{
		el.onclick=function onclick(event) {CUEA.setCourse(this);}
	}
	//$D(element);
	$A(d, or);
	
}

function $A(a, b){
	try{
		a.appendChild(b);
	}catch(e){
		alert(e);
	}	
}

function $GN(name){
	return document.getElementsByName(name);
}

function $GSV(name){
	var els = $GN(name);
	var val;
	for(var x = 0; x<els.length; x++){
		val = getCheckedValue(els[x]);
		if(val != "")
			return val;
	}
}


/**
 * get selected value of radio
 * @param radioObj
 * @return
 */
function getCheckedValue(radioObj) {
	if(!radioObj)
		return "";
	var radioLength = radioObj.length;
	if(radioLength == undefined)
		if(radioObj.checked)
			return radioObj.value;
		else
			return "";
	for(var i = 0; i < radioLength; i++) {
		if(radioObj[i].checked) {
			return radioObj[i].value;
		}
	}
	return "";
}

/**
 * 
 * @param id id of the <SELECT> element
 * @return the value if the selected option
 */
function $GSO(id){
	var si = $G(id);
	var val;
	if(si==undefined){
		return null;
	}else{	
		var x = si.selectedIndex;
		val = si[x].value;
		return val;
	}
	
}
