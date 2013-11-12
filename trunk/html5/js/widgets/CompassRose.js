/*
 * @author Olivier Le Diouris
 */
function CompassRose(cName,                     // Canvas Name
                     title,
                     width,                     // Display width
                     height,                    // Display height
                     value, 
                     textColor)
{
  // base = w 200 h 50
  var scale = 1;   

  var canvasName = cName;
  
  var displayWidth  = width;
  var displayHeight = height;

  var valueToDisplay = 0;
  var totalViewAngle = 50; // must be even...
  
  if (value !== undefined)
    valueToDisplay = value;
  if (textColor === undefined)
    textColor = '#ffffd0';   // ivory
  
  var instance = this;
  
  (function(){ drawDisplay(canvasName, displayWidth, displayHeight); })(); // Invoked automatically
  
  this.setValue = function(val)
  {
    valueToDisplay = val;
    drawDisplay(canvasName, displayWidth, displayHeight);
  };
  
  this.setDisplaySize = function(dw, dh)
  {
 // scale = ds / 100;
    displayWidth  = dw;
    displayHeight = dh;
    drawDisplay(canvasName, displayWidth, displayHeight);
  };
  
  function drawDisplay(displayCanvasName, displayW, displayH)
  {
    if (displayW !== undefined && displayH !== undefined)
    {
      scale = Math.min(displayW / 200, displayH / 50);
    }
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
    grd.addColorStop(0, 'black'); // 0  Beginning
    grd.addColorStop(1, 'gray');  // 1  End
    context.fillStyle = grd;
  
    // Background
    roundRect(context, 0, 0, canvas.width, canvas.height, 10, true, false);    
    // Ticks
    context.strokeStyle = 'white'; // 'rgba(255, 255, 255, 0.7)';
    context.lineWidth   = 0.5;
    
    var startValue = valueToDisplay - (totalViewAngle / 2);
    var endValue   = valueToDisplay + (totalViewAngle / 2);
    for (var tick=startValue; tick<=endValue; tick++)
    {
      var tickHeight = canvas.height / 4;
      if (tick % 5 === 0)
        tickHeight = canvas.height / 2;
      var x = (tick - startValue) * (canvas.width / totalViewAngle);
      context.strokeStyle = 'white'; // 'rgba(255, 255, 255, 0.7)';
      context.beginPath();
      context.moveTo(x, 0);
      context.lineTo(x, tickHeight);
      context.closePath();
      context.stroke();    
      if (tick % 15 === 0)
      {
        var tk = tick;
        while (tk < 0) tk += 360;
        var txt = tk.toString();
        if (tick % 90 === 0)
        {
          if (tick === 0)   txt = "N";
          if (tick === 90)  txt = "E";
          if (tick === 180) txt = "S";
          if (tick === 270) txt = "W";
          if (tick === 360) txt = "N";
        }
        context.font = "bold " + Math.round(scale * 20) + "px Arial"; // "bold 16px Arial"
        var metrics = context.measureText(txt);
        len = metrics.width;    
        context.fillStyle = textColor;
        context.fillText(txt, x - (len / 2), canvas.height - 5);
      }
    }

    context.fillStyle = 'green';
    // Value, top left corner
    context.font = "bold " + Math.round(scale * 16) + "px Courier New"; // "bold 16px Arial"
    context.fillText(valueToDisplay.toString() + "\272", 5, 14);
    
    context.strokeStyle = 'red';
    context.beginPath();
    context.moveTo(canvas.width / 2, 0);
    context.lineTo(canvas.width / 2, canvas.height);
    context.closePath();
    context.stroke();    
  };

  function roundRect(ctx, x, y, width, height, radius, fill, stroke) 
  {
    if (fill === undefined) 
    {
      fill = true;
    }
    if (stroke === undefined) 
    {
      stroke = true;
    }
    if (radius === undefined) 
    {
      radius = 5;
    }
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + width - radius, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    ctx.lineTo(x + width, y + height - radius);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    ctx.lineTo(x + radius, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
    if (stroke) 
    {
      ctx.stroke();
    }
    if (fill) 
    {
      ctx.fill();
    }        
  };
}