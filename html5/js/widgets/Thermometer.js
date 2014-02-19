/*
 * @author Olivier Le Diouris
 */
var thermometerColorConfigWhite = 
{
  withShadow:        true,
  shadowColor:       'LightGrey',
  scaleColor:        'black',
  bgColor:           'white',
  majorTickColor:    'LightGrey',
  minorTickColor:    'DarkGrey',
  valueOutlineColor: 'black',
  valueColor:        'DarkGrey',
  tubeOutlineColor:  'pink',
  hgOutlineColor:    'DarkGrey',
  font:              'Arial'
};

var thermometerColorConfigBlack = 
{
  withShadow:        true,
  shadowColor:       'black',
  scaleColor:        'LightGrey',
  bgColor:           'black',
  majorTickColor:    'LightGrey',
  minorTickColor:    'DarkGrey',
  valueOutlineColor: 'black',
  valueColor:        'LightGrey',
  tubeOutlineColor:  'pink',
  hgOutlineColor:    'DarkGrey',
  font:              'Arial'
};

var thermometerColorConfig = thermometerColorConfigBlack; // Black is the default

function Thermometer(cName, dSize, minValue, maxValue, majorTicks, minorTicks)
{
  if (minValue === undefined)
    minValue = -20;
  if (maxValue === undefined)
    maxValue = 50;
  if (majorTicks === undefined)
    majorTicks = 10;
  if (minorTicks === undefined)
    minorTicks = 1;

  var canvasName = cName;
  var displaySize = dSize;

  var running = false;
  var previousValue = 0.0;
  var intervalID;
  var valueToDisplay = 0;
  
  var instance = this;
  
//try { console.log('in the Thermometer constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}
  
  (function(){ drawDisplay(canvasName, displaySize, previousValue); })(); // Invoked automatically
  
  this.repaint = function()
  {
    drawDisplay(canvasName, displaySize, previousValue);
  };
  
  this.setDisplaySize = function(ds)
  {
    displaySize = ds;
    drawDisplay(canvasName, displaySize, previousValue);
  };
  
  this.startStop = function (buttonName) 
  {
//  console.log('StartStop requested on ' + buttonName);
    var button = document.getElementById(buttonName);
    running = !running;
    button.value = (running ? "Stop" : "Start");
    if (running)
      this.animate();
    else 
    {
      window.clearInterval(intervalID);
      previousValue = valueToDisplay;
    }
  };

  this.animate = function()
  {    
    var value;
    if (arguments.length === 1)
      value = arguments[0];
    else
    {
//    console.log("Generating random value");
      value = minValue + ((maxValue - minValue) * Math.random());
    }
//  console.log("Reaching Value :" + value + " from " + previousValue);
    diff = value - previousValue;
    valueToDisplay = previousValue;
    
//  console.log(canvasName + " going from " + previousValue + " to " + value);
    
    if (diff > 0)
      incr = 1; // 0.1 * maxValue; // 0.01 is nocer, but too slow...
    else 
      incr = -1; // -0.1 * maxValue;
    intervalID = window.setInterval(function () { displayAndIncrement(incr, value); }, 50);
  };

  var displayAndIncrement = function(inc, finalValue)
  {
    //console.log('Tic ' + inc + ', ' + finalValue);
    drawDisplay(canvasName, displaySize, valueToDisplay);
    valueToDisplay += inc;
    if ((inc > 0 && valueToDisplay > finalValue) || (inc < 0 && valueToDisplay < finalValue))
    {
      //  console.log('Stop!')
      window.clearInterval(intervalID);
      previousValue = finalValue;
      if (running)
        instance.animate();
      else
        drawDisplay(canvasName, displaySize, finalValue);
    }
  };

  function drawDisplay(displayCanvasName, displayRadius, displayValue)
  {
    var schemeColor = getCSSClass(".display-scheme");
    if (schemeColor !== undefined && schemeColor !== null)
    {
      var styleElements = schemeColor.split(";");
      for (var i=0; i<styleElements.length; i++)
      {
        var nv = styleElements[i].split(":");
        if ("color" === nv[0])
        {
//        console.log("Scheme Color:[" + nv[1].trim() + "]");
          if (nv[1].trim() === 'black')
            thermometerColorConfig = thermometerColorConfigBlack;
          else if (nv[1].trim() === 'white')
            thermometerColorConfig = thermometerColorConfigWhite;
        }
      }
    }

    var digitColor = thermometerColorConfig.scaleColor;
    
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var radius = 10; // The ball at the bottom. The tube is (radius / 2) wide.
  
    // Cleanup
    context.fillStyle = thermometerColorConfig.bgColor;
  //context.fillStyle = "#ffffff";
  //context.fillStyle = "LightBlue";
  //context.fillStyle = "transparent";
    context.fillRect(0, 0, canvas.width, canvas.height);    
  //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
  //context.fillRect(0, 0, canvas.width, canvas.height);    
  
  //context.fillStyle = "transparent";
     // Bottom of the tube at (canvas.height - 10 - radius)
    var bottomTube = (canvas.height - 10 - radius);
    var topTube = 40;// Top of the tube at y = 20
    
    var tubeLength = bottomTube - topTube;

    // Major Ticks
    context.beginPath();
    for (i = 0;i <= (maxValue - minValue) ;i+=majorTicks)
    {
      xFrom = (canvas.width / 2) - 20;
      yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
      xTo = (canvas.width / 2) + 20;
      yTo = yFrom;
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
    }
    context.lineWidth = 1;
    context.strokeStyle = thermometerColorConfig.majorTickColor;
    context.stroke();
    context.closePath();

    // Minor Ticks
    if (minorTicks > 0)
    {
      context.beginPath();
      for (i = 0;i <= (maxValue - minValue) ;i+=minorTicks)
      {
        xFrom = (canvas.width / 2) - 15;
        yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
        xTo = (canvas.width / 2) + 15;
        yTo = yFrom;
        context.moveTo(xFrom, yFrom);
        context.lineTo(xTo, yTo);
      }
      context.lineWidth = 1;
      context.strokeStyle = thermometerColorConfig.minorTickColor;
      context.stroke();
      context.closePath();
    }
    
    // Tube
    context.beginPath();
  //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);          
    context.arc(canvas.width / 2, canvas.height - 10 - radius, radius, 5 * Math.PI / 4, 7 * Math.PI / 4, true);    
    context.lineTo((canvas.width / 2) + (radius *Math.cos(Math.PI / 4)), topTube); // right side of the tube
    context.arc(canvas.width / 2, topTube, (radius / 2), 0, Math.PI, true); 
    context.lineWidth = 1;
  
    var grd = context.createLinearGradient(0, 5, 0, radius);
    grd.addColorStop(0, 'LightGrey'); // 0  Beginning. black
    grd.addColorStop(1, 'white');     // 1  End. LightGrey
    context.fillStyle = grd;

    if (thermometerColorConfig.withShadow)
    {    
      context.shadowOffsetX = 3;
      context.shadowOffsetY = 3;
      context.shadowBlur  = 3;
      context.shadowColor = thermometerColorConfig.shadowColor;
    }
  
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = thermometerColorConfig.tubeOutlineColor; // Tube outline color
    context.stroke();
    context.closePath();
        
    // Numbers
    context.beginPath();
    for (i = minValue;i <= maxValue ;i+=majorTicks)
    {
      xTo = (canvas.width / 2) + 20;
      yTo = bottomTube - ((tubeLength) * ((i - minValue) / (maxValue - minValue)));;
      context.font = "bold 10px " + thermometerColorConfig.font;
      context.fillStyle = digitColor;
      str = i.toString();
      len = context.measureText(str).width;
      context.fillText(str, xTo, yTo + 3); // 5: half font size
    }
    context.closePath();
    
    // Value
    text = displayValue.toFixed(2);
    len = 0;
    context.font = "bold 20px " + thermometerColorConfig.font;
    var metrics = context.measureText(text);
    len = metrics.width;
  
    context.beginPath();
    context.fillStyle = thermometerColorConfig.valueColor;
    context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
    context.lineWidth = 1;
    context.strokeStyle = thermometerColorConfig.valueOutlineColor;
    context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined  
    context.closePath();
  
    // Liquid in the tube
    context.beginPath();
  //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);          
    context.arc(canvas.width / 2, canvas.height - 10 - (radius * 0.75), (radius * 0.75), 5 * Math.PI / 4, 7 * Math.PI / 4, true);   
    var y = bottomTube - ((tubeLength) * ((displayValue - minValue) / (maxValue - minValue)));

    context.lineTo((canvas.width / 2) + ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // right side of the tube
    context.lineTo((canvas.width / 2) - ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // top of the liquid

    context.lineWidth = 1;
  
    var _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
    // Colors are hard-coded
    _grd.addColorStop(0,   'red');    // 0  Beginning, top
    _grd.addColorStop(0.6, 'orange');   
    _grd.addColorStop(0.8, 'blue'); 
    _grd.addColorStop(1,   'navy');   // 1  End, bottom
    context.fillStyle = _grd;
    
//  context.shadowBlur  = 20;
//  context.shadowColor = 'black';
  
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = thermometerColorConfig.hgOutlineColor;
    context.stroke();
    context.closePath();
  };
  
  this.setValue = function(val)
  {
    drawDisplay(canvasName, displaySize, val);
  };
}