function TWSEvolution(cName)     // Canvas name
{
  var instance = this;
  var cWidth, cHeight;
  
  var canvas;
  var context;
  
  var twsBuffer = [];
  var lastTWS = 0;
  
  this.addTWS = function(d)
  {
    twsBuffer.push(d);
    lastTWS = d;
    instance.drawGraph();
  };

  this.resetTWS = function()
  {
    twsBuffer = [];
    instance.drawGraph();
  };

  this.drawGraph = function()
  {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    context = canvas.getContext('2d');
    
 // context.fillStyle = "LightGray";
    var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
    grd.addColorStop(0, 'LightGray') ; // 'gray');    // 0  Beginning
    grd.addColorStop(1, 'black'); // 'LightGray');    // 1  End
    context.fillStyle = grd;

    context.fillRect(0, 0, canvas.width, canvas.height);    

//  context.beginPath();
//  context.lineWidth = 1;
//  context.strokeStyle = 'black';
//  context.strokeText("Overview", 10, 20); // Outlined  
//  context.closePath();
    // Grid
    context.strokeStyle = 'LightGreen';
    for (var i=0; i<60; i+=5) // every 5 knots
    {
      var x = i * (canvas.width / 60);
      context.beginPath();
      context.lineWidth = (i % 10 === 0) ? 3 : 1;
      context.moveTo(x, 0);
      context.lineTo(x, canvas.height);
      context.closePath();
      context.stroke();
    }        
    context.lineWidth = 1;
    // Beaufort scale
    var beaufort = [1, 4, 7, 11, 16, 22, 28, 34, 41, 48, 56, 64];
    context.strokeStyle = 'rgba(255, 0, 0, 0.7)'; // 'red';
    for (var i=0; i<beaufort.length; i++)
    {
      var x = beaufort[i] * (canvas.width / 60);
      context.beginPath();
      context.moveTo(x, 0);
      context.lineTo(x, canvas.height);
      context.closePath();
      context.stroke();
      var txt = (i + 1).toString();
      context.font = "bold 16px Arial"; // "bold 16px Arial"
      var metrics = context.measureText(txt);
      len = metrics.width;    
      context.fillStyle = 'white';
      context.fillText(txt, x - (len / 2), canvas.height - 5);
    }
    context.lineWidth = 3;
    
    // Data here    
    // Calculate average
    if (false && twsBuffer.length > 0)
    {
      var sum = 0;
      for (var i=0; i<twsBuffer.length; i++)
      {
        sum += twsBuffer[i];
      }
      var avg = sum / twsBuffer.length;
    }
    
    var yScale = canvas.height / (twsBuffer.length - 1);
    var xScale = canvas.width / 60;
    context.strokeStyle = 'cyan';
    context.beginPath();
    for (var i=0; i<twsBuffer.length; i++)
    {
      var xPt = twsBuffer[i] * xScale;
      var yPt = canvas.height - (i * yScale);
//    console.log("i:" + i + ", " + xPt + "/" + yPt);
      if (i === 0)
        context.moveTo(xPt, yPt);
      else
        context.lineTo(xPt, yPt);
    }
//  context.closePath();
    context.stroke();
    
    // Display values
    context.fillStyle = 'green';
    context.font="bold 16px Courier New";
    var txtY = 20;
    var space = 18;
    var col1 = 10, col2 = 90;
    context.fillText("TWS", col1, txtY);
    context.fillText(lastTWS + "kts", col2, txtY);
    txtY += space;    
  };

  var relativeMouseCoords = function (event, element)
  {
    var totalOffsetX = 0;
    var totalOffsetY = 0;
    var canvasX = 0;
    var canvasY = 0;
    var currentElement = element;

    do
    {
      totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
      totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
    }
    while (currentElement = currentElement.offsetParent)

    canvasX = event.pageX - totalOffsetX;
    canvasY = event.pageY - totalOffsetY;

    return {x:canvasX, y:canvasY};
  };
    
  var toRadians = function(deg)
  {
    return deg * (Math.PI / 180);
  };

  function toDegrees(rad)
  {
    return rad * (180 / Math.PI);
  };
  
  (function()
   { 
     canvas = document.getElementById(cName);
     canvas.addEventListener('mousemove', function(evt)
     {
        var x = evt.pageX - canvas.offsetLeft;
        var y = evt.pageY - canvas.offsetTop;
        
        var coords = relativeMouseCoords(evt, canvas);
        x = coords.x;
        y = coords.y;
        var str1 = "TWS " + Math.round(60 * x / canvas.width) + "kts";
        instance.drawGraph();
        context.fillStyle = "rgba(250, 250, 210, .6)"; 
//      context.fillStyle = 'yellow';
        context.fillRect(x + 10, y + 10, 70, 20); // Background
        context.fillStyle = 'black';
        context.font = 'bold 12px verdana';
        context.fillText(str1, x + 15, y + 25, 60); 
      }, 0);
     instance.drawGraph();
   })(); // Invoked automatically when new is invoked.  
};
