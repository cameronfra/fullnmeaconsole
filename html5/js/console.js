var displayBSP, displayTWD, displayTWS, thermometer, displayHDG, displayOverview,
    jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG,
    displayAW,
    twdEvolution;
    
var jumboList = [];

var init = function() 
{
  displayBSP      = new AnalogDisplay('bspCanvas', 100,   15,  5,  1);
  displayHDG      = new Direction('hdgCanvas', 100, 45, 5);
  displayTWD      = new Direction('twdCanvas', 100, 45, 5);
//displayTWD      = new Direction('twdCanvas', 100, 1060,  10,  1, false, 60, 960);
  displayTWS      = new AnalogDisplay('twsCanvas', 100,   50,  10,  1, true, 40);
  thermometer     = new Thermometer('tmpCanvas', 200);
  displayOverview = new BoatOverview('overviewCanvas');
  
  jumboBSP        = new JumboDisplay('jumboBSPCanvas', 'BSP', 120, 60, "0.00");
  jumboHDG        = new JumboDisplay('jumboHDGCanvas', 'HDG', 120, 60, "000");
  jumboTWD        = new JumboDisplay('jumboTWDCanvas', 'TWD', 120, 60, "000", 'cyan');
  jumboLWY        = new JumboDisplay('jumboLWYCanvas', 'LWY', 120, 60, "000", 'red');
  jumboAWA        = new JumboDisplay('jumboAWACanvas', 'AWA', 120, 60, "000");
  jumboTWA        = new JumboDisplay('jumboTWACanvas', 'TWA', 120, 60, "000", 'cyan');
  jumboAWS        = new JumboDisplay('jumboAWSCanvas', 'AWS', 120, 60, "00.0");
  jumboTWS        = new JumboDisplay('jumboTWSCanvas', 'TWS', 120, 60, "00.0", 'cyan');
  jumboCOG        = new JumboDisplay('jumboCOGCanvas', 'COG', 120, 60, "000");
  jumboCDR        = new JumboDisplay('jumboCDRCanvas', 'CDR', 120, 60, "000", 'cyan');
  jumboSOG        = new JumboDisplay('jumboSOGCanvas', 'SOG', 120, 60, "0.00");
  jumboCSP        = new JumboDisplay('jumboCSPCanvas', 'CSP', 120, 60, "00.0", 'cyan');
  jumboVMG        = new JumboDisplay('jumboVMGCanvas', 'VMG', 120, 60, "0.00", 'yellow');
  
  jumboList = [jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG];
  
  displayAW       = new AWDisplay('awDisplayCanvas', 80, 45, 5);
  twdEvolution    = new TWDEvolution('twdEvolutionCanvas');
  
  var interval = setInterval(function() { pingNMEAConsole(); }, 1000);
};

var TOTAL_WIDTH = 1200;

var resizeDisplays = function(width)
{
  if (displayBSP !== undefined && displayTWS !== undefined) // TODO Other displays
  {
    displayBSP.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWS.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayHDG.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWD.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    thermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayOverview.drawGraph();
    twdEvolution.drawGraph();

    var jumboFactor = width / TOTAL_WIDTH;
    for (var i=0; i<jumboList.length; i++)
    {
      if (jumboList[i] !== undefined)
        jumboList[i].setDisplaySize(120 * jumboFactor, 60 * jumboFactor);
    }
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
};

/**
 * Sample data:
<?xml version='1.0' encoding='UTF-8'?>
<?xml-stylesheet href="nmea-xml-html.xsl" type="text/xsl"?>
<!DOCTYPE data [
 <!ENTITY deg     "&#176;">
]>
<data>
  <D>10</D>
  <gps-date-time>1290377264165</gps-date-time>
  <gps-date-time-fmt>21 Nov 2010 14:07:44 UTC</gps-date-time-fmt>
  <aws>16.46</aws>
  <wtemp>26.50</wtemp>
  <to-wp>RANGI   </to-wp>
  <cdr>311</cdr>
  <gps-time>1290377264165</gps-time>
  <gps-time-fmt>14:07:44 UTC</gps-time-fmt>
  <tws>21.20</tws>
  <dbt>2.00</dbt>
  <cog>223</cog>
  <awa>-115</awa>
  <hdg>214</hdg>
  <cmg>218</cmg>
  <twd>82</twd>
  <leeway>0</leeway>
  <d>-2</d>
  <csp>1.05</csp>
  <twa>-132</twa>
  <sog>6.72</sog>
  <lat>-9.10825</lat>
  <lng>-140.20926666666668</lng>
  <pos>S  09&deg;06.49' / W 140&deg;12.56'</pos>
  <bsp>6.79</bsp>
  <b2wp>230</b2wp>
  <gps-solar-date>1290343613941</gps-solar-date>
  <vmg-wind>-5.21</vmg-wind>
  <vmg-wp>6.66</vmg-wp>
  <perf>0.96</perf>
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
      var bsp = parseFloat(doc.getElementsByTagName("bsp")[0].childNodes[0].nodeValue);
//    displayBSP.animate(bsp);
      displayBSP.setValue(bsp);
      displayOverview.setBSP(bsp);
      jumboBSP.setValue(bsp.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with boat speed...");
//    displayBSP.animate(0.0);
      displayBSP.setValue(0.0);
    }
    try
    {
      var hdg = parseFloat(doc.getElementsByTagName("hdg")[0].childNodes[0].nodeValue);
//    displayHDG.animate(hdg);
      displayHDG.setValue(hdg);
      displayOverview.setHDG(hdg);
      jumboHDG.setValue(lpad(Math.round(hdg).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with heading...");
//    displayHDG.animate(0.0);
      displayHDG.setValue(0.0);
    }
    try
    {
      var twd = parseFloat(doc.getElementsByTagName("twd")[0].childNodes[0].nodeValue);
//    displayTWD.animate(twd);
      displayTWD.setValue(twd);
      displayOverview.setTWD(twd);
      jumboTWD.setValue(lpad(Math.round(twd).toString(), '0', 3));
      twdEvolution.addTWD(twd);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
//    displayTWD.animate(0.0);
      displayTWD.setValue(0.0);
    }
    try
    {
      var twa = parseFloat(doc.getElementsByTagName("twa")[0].childNodes[0].nodeValue);
      displayOverview.setTWA(twa);
      var twaStr = lpad(Math.round(Math.abs(twa)).toString(), '0', 3);
      if (twa < 0)
        twaStr = '-' + twaStr;
      else
        twaStr += '-';
      jumboTWA.setValue(twaStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
//    displayTWD.animate(0.0);
      displayTWD.setValue(0.0);
    }
    try
    {
      var tws = parseFloat(doc.getElementsByTagName("tws")[0].childNodes[0].nodeValue);
//    displayTWS.animate(tws);
      displayTWS.setValue(tws);
      displayOverview.setTWS(tws);
      jumboTWS.setValue(tws.toFixed(1));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWS...");
//    displayTWS.animate(0.0);
      displayTWS.setValue(0.0);
    }
    try
    {
      var waterTemp = parseFloat(doc.getElementsByTagName("wtemp")[0].childNodes[0].nodeValue);
//    thermometer.animate(waterTemp);
      thermometer.setValue(waterTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with water temperature...");
//    thermometer.animate(0.0);
      thermometer.setValue(0.0);
    }
    try
    {
      var aws = parseFloat(doc.getElementsByTagName("aws")[0].childNodes[0].nodeValue);
      displayAW.setAWS(aws);
      displayOverview.setAWS(aws);
      jumboAWS.setValue(aws.toFixed(1));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWS...");
    }    
    try
    {
      var awa = parseFloat(doc.getElementsByTagName("awa")[0].childNodes[0].nodeValue);
//    displayAW.animate(awa);
      displayAW.setValue(awa);
      displayOverview.setAWA(awa);
      var awaStr = lpad(Math.round(Math.abs(awa)).toString(), '0', 3);
      if (awa < 0)
        awaStr = '-' + awaStr;
      else
        awaStr += '-';
      jumboAWA.setValue(awaStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWA...");
    }    
    try
    {
      var cdr = parseFloat(doc.getElementsByTagName("cdr")[0].childNodes[0].nodeValue);
      displayOverview.setCDR(cdr);
      jumboCDR.setValue(lpad(Math.round(cdr).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CDR...");
    }
      
    try
    {
      var cog = parseFloat(doc.getElementsByTagName("cog")[0].childNodes[0].nodeValue);
      displayOverview.setCOG(cog);
      jumboCOG.setValue(lpad(Math.round(cog).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with COG...");
    }
    try
    {
      var cmg = parseFloat(doc.getElementsByTagName("cmg")[0].childNodes[0].nodeValue);
      displayOverview.setCMG(cmg);
//    jumboCMG.setValue(lpad(Math.round(cmg).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CMG...");
    }      
    try
    {
      var leeway = parseFloat(doc.getElementsByTagName("leeway")[0].childNodes[0].nodeValue);
      displayOverview.setLeeway(leeway);
      var lwyStr = lpad(Math.round(Math.abs(leeway)).toString(), '0', 2);
      if (leeway < 0)
        lwyStr = '-' + lwyStr;
      else
        lwyStr += '-';
      jumboLWY.setValue(lwyStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Leway...");
    }      
    try
    {
      var csp = parseFloat(doc.getElementsByTagName("csp")[0].childNodes[0].nodeValue);
      displayOverview.setCSP(csp);
      jumboCSP.setValue(csp.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CSP...");
    }    
    try
    {
      var sog = parseFloat(doc.getElementsByTagName("sog")[0].childNodes[0].nodeValue);
      displayOverview.setSOG(sog);
      jumboSOG.setValue(sog.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with SOG...");
    }
    // to-wp, vmg-wind, vmg-wp, b2wp
    try
    {
      var to_wp = doc.getElementsByTagName("to-wp")[0].childNodes[0].nodeValue;
      var b2wp = parseFloat(doc.getElementsByTagName("b2wp")[0].childNodes[0].nodeValue);
      displayOverview.setB2WP(b2wp);
      document.getElementById("display.vmg.waypoint").disabled = false;
      document.getElementById("display.vmg.waypoint").value = to_wp;
      document.getElementById("display.vmg.waypoint").nextSibling.textContent = "VMG to " + to_wp;
    }
    catch (err)
    {
      document.getElementById("display.vmg.waypoint").disabled = true;
      document.getElementById("display.vmg.wind").checked = true;
    }
    
    try
    {
      var vmg = 0;
      if (document.getElementById("display.vmg.wind").checked)
        vmg = parseFloat(doc.getElementsByTagName("vmg-wind")[0].childNodes[0].nodeValue);
      else
        vmg = parseFloat(doc.getElementsByTagName("vmg-wp")[0].childNodes[0].nodeValue);
      displayOverview.setVMG(vmg);
      jumboVMG.setValue(vmg.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with VMG...");
    }
    
    // perf
    try
    {
      var perf = parseFloat(doc.getElementsByTagName("perf")[0].childNodes[0].nodeValue);
      perf *= 100;
      displayOverview.setPerf(perf);
    }
    catch (err)
    {
   // errMess += ((errMess.length > 0?"\n":"") + "Problem with Perf...");
    }
    
    if (errMess.length > 0)
      document.getElementById("err-mess").innerHTML = errMess;
  }
  catch (err)
  {
    document.getElementById("err-mess").innerHTML = err;
  }
};

var lpad = function(str, pad, len)
{
  while (str.length < len)
    str = pad + str;
  return str;
};
