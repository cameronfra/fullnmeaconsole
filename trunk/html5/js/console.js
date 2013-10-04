var displayBSP, displayTWD, displayTWS, thermometer, displayHDG, displayOverview;

var init = function() 
{
  displayBSP      = new AnalogDisplay('bspCanvas', 100,   15,  5,  1);
  displayHDG      = new Direction('hdgCanvas', 100, 45, 5);
  displayTWD      = new Direction('twdCanvas', 100, 45, 5);
//displayTWD      = new Direction('twdCanvas', 100, 1060,  10,  1, false, 60, 960);
  displayTWS      = new AnalogDisplay('twsCanvas', 100,   50,  10,  1, true, 40);
  thermometer     = new Thermometer('tmpCanvas', 200);
  displayOverview = new BoatOverview('overviewCanvas');
  
  var interval = setInterval(function() { pingNMEAConsole(); }, 1000);
}

var TOTAL_WIDTH = 1200;

var resizeDisplays = function(width)
{
  if (displayBSP !== undefined && displayTWS !== undefined)
  {
    displayBSP.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWS.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayHDG.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWD.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    thermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayOverview.drawGraph();
  }
};
  
var reloadMap = function() 
{
  var start = new Date().getTime();
  var iframe = document.getElementById("map-frame");
  iframe.onload = function() 
  {
    var end = new Date().getTime();
    var time = end - start;
    document.getElementById("elapsed").innerHTML = "Reload (iframe & map) took <b>" + time + "</b> ms.";
  };
  iframe.contentWindow.location.reload();
}

/**
 * Sample data:
<data>
  <D>15</D>
  <gps-date-time>1278695979949</gps-date-time>
  <gps-date-time-fmt>09 Jul 2010 10:19:39 UTC</gps-date-time-fmt>
  <aws>11.25</aws>
  <wtemp>17.00</wtemp>
  <cdr>119</cdr>
  <gps-time>1278695979949</gps-time>
  <gps-time-fmt>10:19:39 UTC</gps-time-fmt>
  <tws>13.03</tws>
  <dbt>6.90</dbt>
  <cog>192</cog>
  <awa>46</awa>
  <hdg>237</hdg>
  <cmg>228</cmg>
  <twd>312</twd>
  <leeway>-10</leeway>
  <d>-5</d>
  <csp>3.89</csp>
  <twa>76</twa>
  <sog>6.51</sog>
  <lat>37.67263333333333</lat>
  <lng>-122.35033333333334</lng>
  <pos>N  37&deg;40.36' / W 122&deg;21.02'</pos>
  <bsp>6.56</bsp>
</data>
 */
var pingNMEAConsole = function()
{
  try
  {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/", false);
    xhr.send();
    doc = xhr.responseXML; 
    var errMess = "";
    
    try
    {
      var latitude  = parseFloat(doc.getElementsByTagName("lat")[0].childNodes[0].nodeValue);
//    console.log("latitude:" + latitude)
      var longitude = parseFloat(doc.getElementsByTagName("lng")[0].childNodes[0].nodeValue);
//    console.log("Pt:" + latitude + ", " + longitude);
      var label = "Your position";
      // Plot the station on the map
      var mapFrame = document.getElementById("map-frame");
      var canvas = "mapCanvas";
      mapFrame.contentWindow.plotPosToCanvas(canvas, latitude, longitude, label);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with position...");
    }
    // Displays
    try
    {
      var bsp = parseFloat(parseFloat(doc.getElementsByTagName("bsp")[0].childNodes[0].nodeValue));
      displayBSP.animate(bsp);
      displayOverview.setBSP(bsp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with boat speed...");
      displayBSP.animate(0.0);
    }
    try
    {
      var hdg = parseFloat(parseFloat(doc.getElementsByTagName("hdg")[0].childNodes[0].nodeValue));
      displayHDG.animate(hdg);
      displayOverview.setHDG(hdg);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with heading...");
      displayHDG.animate(0.0);
    }
    try
    {
      var twd = parseFloat(parseFloat(doc.getElementsByTagName("twd")[0].childNodes[0].nodeValue));
      displayTWD.animate(twd);
      displayOverview.setTWD(twd);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
      displayTWD.animate(0.0);
    }
    try
    {
      var tws = parseFloat(parseFloat(doc.getElementsByTagName("tws")[0].childNodes[0].nodeValue));
      displayTWS.animate(tws);
      displayOverview.setTWS(tws);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWS...");
      displayTWS.animate(0.0);
    }
    try
    {
      var waterTemp = parseFloat(parseFloat(doc.getElementsByTagName("wtemp")[0].childNodes[0].nodeValue));
      thermometer.animate(waterTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with water temperature...");
      thermometer.animate(0.0);
    }
    if (errMess.length > 0)
      document.getElementById("err-mess").innerHTML = errMess;
  }
  catch (err)
  {
    document.getElementById("err-mess").innerHTML = err;
  }
};

