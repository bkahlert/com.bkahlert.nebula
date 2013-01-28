// http://stv.whtly.com/2009/02/27/simple-jquery-string-padding-function/
$.strPad = function(i,l,s) {
	var o = i.toString();
	if (!s) { s = '0'; }
	while (o.length < l) {
		o = s + o;
	}
	return o;
};

/**
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */
var dateFormat=function(){var token=/d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,timezone=/\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,timezoneClip=/[^-+\dA-Z]/g,pad=function(val,len){val=String(val);len=len||2;while(val.length<len)val="0"+val;return val};return function(date,mask,utc){var dF=dateFormat;if(arguments.length==1&&Object.prototype.toString.call(date)=="[object String]"&&!/\d/.test(date)){mask=date;date=undefined}date=date?new Date(date):new Date;if(isNaN(date))throw SyntaxError("invalid date");mask=String(dF.masks[mask]||mask||dF.masks["default"]);if(mask.slice(0,4)=="UTC:"){mask=mask.slice(4);utc=true}var _=utc?"getUTC":"get",d=date[_+"Date"](),D=date[_+"Day"](),m=date[_+"Month"](),y=date[_+"FullYear"](),H=date[_+"Hours"](),M=date[_+"Minutes"](),s=date[_+"Seconds"](),L=date[_+"Milliseconds"](),o=utc?0:date.getTimezoneOffset(),flags={d:d,dd:pad(d),ddd:dF.i18n.dayNames[D],dddd:dF.i18n.dayNames[D+7],m:m+1,mm:pad(m+1),mmm:dF.i18n.monthNames[m],mmmm:dF.i18n.monthNames[m+12],yy:String(y).slice(2),yyyy:y,h:H%12||12,hh:pad(H%12||12),H:H,HH:pad(H),M:M,MM:pad(M),s:s,ss:pad(s),l:pad(L,3),L:pad(L>99?Math.round(L/10):L),t:H<12?"a":"p",tt:H<12?"am":"pm",T:H<12?"A":"P",TT:H<12?"AM":"PM",Z:utc?"UTC":(String(date).match(timezone)||[""]).pop().replace(timezoneClip,""),o:(o>0?"-":"+")+pad(Math.floor(Math.abs(o)/60)*100+Math.abs(o)%60,4),S:["th","st","nd","rd"][d%10>3?0:(d%100-d%10!=10)*d%10]};return mask.replace(token,function($0){return $0 in flags?flags[$0]:$0.slice(1,$0.length-1)})}}();dateFormat.masks={"default":"ddd mmm dd yyyy HH:MM:ss",shortDate:"m/d/yy",mediumDate:"mmm d, yyyy",longDate:"mmmm d, yyyy",fullDate:"dddd, mmmm d, yyyy",shortTime:"h:MM TT",mediumTime:"h:MM:ss TT",longTime:"h:MM:ss TT Z",isoDate:"yyyy-mm-dd",isoTime:"HH:MM:ss",isoDateTime:"yyyy-mm-dd'T'HH:MM:ss",isoUtcDateTime:"UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"};dateFormat.i18n={dayNames:["Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],monthNames:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","January","February","March","April","May","June","July","August","September","October","November","December"]};Date.prototype.format=function(mask,utc){return dateFormat(this,mask,utc)};

/**
 * A simple querystring parser.
 * Example usage: var q = $.parseQuery(); q.fooreturns  "bar" if query contains "?foo=bar"; multiple values are added to an array. 
 * Values are unescaped by default and plus signs replaced with spaces, or an alternate processing function can be passed in the params object .
 * http://actingthemaggot.com/jquery
 *
 * Copyright (c) 2008 Michael Manning (http://actingthemaggot.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 **/
(function($) {
	$.parseQuery = function(qs,options) {
		var q = (typeof qs === 'string'?qs:window.location.search), o = {'f':function(v){return unescape(v).replace(/\+/g,' ');}}, options = (typeof qs === 'object' && typeof options === 'undefined')?qs:options, o = jQuery.extend({}, o, options), params = {};
		jQuery.each(q.match(/^\??(.*)$/)[1].split('&'),function(i,p){
			p = p.split('=');
			p[1] = o.f(p[1]);
			params[p[0]] = params[p[0]]?((params[p[0]] instanceof Array)?(params[p[0]].push(p[1]),params[p[0]]):[params[p[0]],p[1]]):p[1];
		});
		return params;
	}
})(jQuery);

// source: http://www.techrepublic.com/article/convert-the-local-time-to-another-time-zone-with-this-javascript/6016329
function convertTimeZone(date, timeZoneOffset) {
    // convert to msec
    // add local time zone offset 
    // get UTC time in msec
    utc = date.getTime() + (date.getTimezoneOffset() * 60000);
    
    // create new Date object for different city
    // using supplied offset
    newDate = new Date(utc + (3600000*timeZoneOffset));
    
    // return time as a string
    return newDate;
}

// formats a date ignoring it's time zone and using the provided one for display
// differing time zones have no influence of the printed date
function formatDate(date, timeZoneOffset) {
	var s = date.format("yyyy-mm-dd'T'HH:MM:ss");
	var iso8601date = s + formatTimeZone(timeZoneOffset);
	return iso8601date;
}

// input: rational number like 2.0 or -6.5
function formatTimeZone(timeZoneOffset) {
	var sign = timeZoneOffset < 0 ? "-" : "+";
	timeZoneOffset = Math.abs(timeZoneOffset);
	var hours = $.strPad(Math.floor(timeZoneOffset), 2, 0);
	var minutes = $.strPad((timeZoneOffset-hours)*60, 2, 0);
	return sign + '' + hours + ':' + minutes;
}