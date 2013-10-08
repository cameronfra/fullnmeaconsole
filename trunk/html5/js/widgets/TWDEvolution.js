function TWDEvolution(cName)     // Canvas name
{
  var instance = this;
  var cWidth, cHeight;
  
  var canvas;
  var context;
  
  var twdBuffer = [];
  var lastTWD = 0;
  
  this.addTWD = function(d)
  {
    twdBuffer.push(d);
    lastTWD = d;
    instance.drawGraph();
  };

  this.resetTWD = function()
  {
    twdBuffer = [];
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
    var x = canvas.width / 2;
    
    context.strokeStyle = 'LightGreen';
    context.beginPath();
    context.lineWidth = 3;
    context.moveTo(x, 0);
    context.lineTo(x, canvas.height);
    context.closePath();
    context.stroke();
    // Data here    
    // Calculate average
    if (false && twdBuffer.length > 0)
    {
      var sum = 0;
      for (var i=0; i<twdBuffer.length; i++)
      {
        sum += twdBuffer[i];
      }
      var avg = sum / twdBuffer.length;
    }
    
    var yScale = canvas.height / (twdBuffer.length - 1);
    var xScale = canvas.width / 360;
    context.strokeStyle = 'red';
    context.beginPath();
    for (var i=0; i<twdBuffer.length; i++)
    {
      var xPt = twdBuffer[i] * xScale;
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
    context.fillText("TWD", col1, txtY);
    context.fillText(lastTWD + "º", col2, txtY);
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
        var str1 = "TWD " + Math.round(360 * x / canvas.width) + "º";
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
