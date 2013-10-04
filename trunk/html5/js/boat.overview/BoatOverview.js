function BoatOverview(cName)     // Canvas name
{
  var instance = this;
  var cWidth, cHeight;
  
  var canvas;
  var context;
  
  // NMEA Data
  var  bsp = 0, hdg = 0, tws = 0, twa = 0, twd = 0, aws = 0, awa = 0, perf = 0, leeway = 0, vmg = 0, cog = 0, sog = 0, cmg = 0;
  
  this.setBSP = function(d)
  {
    bsp = d;
    instance.drawGraph();
  };
  this.setTWA = function(d)
  {
    twa = d;
    instance.drawGraph();
  };
  this.setTWS = function(d)
  {
    tws = d;
    instance.drawGraph();
  };
  this.setTWD = function(d)
  {
    twd = d;
    instance.drawGraph();
  };
  this.setHDG = function(d)
  {
    hdg = d;
    instance.drawGraph();
  };
    
  // Line with arrow head
  function Line(x1, y1, x2, y2)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  };
  Line.prototype.drawWithArrowhead = function(ctx)
  {
    this.drawWithArrowheads(ctx, false);
  };
  
  Line.prototype.drawWithArrowheads = function(ctx, both)
  {
    if (both === undefined)
      both = true;
    // arbitrary styling
//  ctx.strokeStyle = "blue";
//  ctx.fillStyle   = "blue";
//  ctx.lineWidth   = 1;
  
    // draw the line
    ctx.beginPath();
    ctx.moveTo(this.x1, this.y1);
    ctx.lineTo(this.x2, this.y2);
    ctx.stroke();
  
    if (both)
    {
      // draw the starting arrowhead
      var startRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
      startRadians += ((this.x2>this.x1)?-90:90) * Math.PI/180;
      this.drawArrowhead(ctx, this.x1, this.y1, startRadians);
    }
    // draw the ending arrowhead
    var endRadians=Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
    endRadians += ((this.x2>this.x1)?90:-90) * Math.PI/180;
    this.drawArrowhead(ctx, this.x2, this.y2, endRadians);
  };
  
  var HEAD_LENGTH = 20;
  var HEAD_WIDTH  = 6;
  Line.prototype.drawArrowhead = function(ctx, x, y, radians)
  {
    ctx.save();
    ctx.beginPath();
    ctx.translate(x, y);
    ctx.rotate(radians);
    ctx.moveTo(0, 0);
    ctx.lineTo( HEAD_WIDTH, HEAD_LENGTH);
    ctx.lineTo(-HEAD_WIDTH, HEAD_LENGTH);
    ctx.closePath();
    ctx.restore();
    ctx.fill();
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
  
  this.drawGraph = function()
  {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    context = canvas.getContext('2d');
    
 // context.fillStyle = "LightGray";
    var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
    grd.addColorStop(0, 'gray');    // 0  Beginning
    grd.addColorStop(1, 'LightGray');// 1  End
    context.fillStyle = grd;

    context.fillRect(0, 0, canvas.width, canvas.height);    

    context.beginPath();
    context.fillStyle = 'red';
    context.fillText("Overview", 10, 20);
    context.lineWidth = 1;
//  context.strokeStyle = 'black';
//  context.strokeText("Overview", 10, 20); // Outlined  
    context.closePath();
    // Circles
    var x = cWidth / 2;
    var y = cHeight / 2;
    context.strokeStyle = 'gray';
    for (var circ=1; circ<=10; circ++)
    {
      var radius = Math.round(circ * ((Math.min(cHeight, cWidth) / 2) / 10));
      context.beginPath();
      context.arc(x, y, radius, 0, 2 * Math.PI);        
      context.closePath();
      context.stroke();
    }
    
    instance.drawBoat(hdg);
    instance.drawTrueWind();
    instance.drawBSP();
  };
  
  var ARROW_LEN = 40;
  
  this.drawTrueWind = function()
  {    
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var wd = twd + 180; // Direction the wind is blowing TO
    while (wd > 360)
      wd -= 360;
    var _twd = toRadians(wd); 
    context.beginPath();
    var x = cWidth / 2;
    var y = cHeight / 2;
    
    var windLength = tws * ((Math.min(cHeight, cWidth) / 2) / 10);
    var dX = windLength * Math.sin(_twd);
    var dY = - windLength * Math.cos(_twd);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "black";
    context.fillStyle   = "black";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
  };
  
  this.drawBSP = function()
  {    
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var _hdg = toRadians(hdg); 
    context.beginPath();
    var x = cWidth / 2;
    var y = cHeight / 2;
    
    var bspLength = bsp * ((Math.min(cHeight, cWidth) / 2) / 10);
    var dX = bspLength * Math.sin(_hdg);
    var dY = - bspLength * Math.cos(_hdg);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "red";
    context.fillStyle   = "red";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
  };
  
  var WL_RATIO_COEFF = 0.75; // Ratio to apply to (3.5 * Width / Length)
  var BOAT_LENGTH = 50;
  this.drawBoat = function(trueHeading)
  {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var x = new Array();
    x.push(WL_RATIO_COEFF * 0); 
    x.push(WL_RATIO_COEFF * BOAT_LENGTH / 7);
    x.push(WL_RATIO_COEFF * (2 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * (2 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * (1.5 * BOAT_LENGTH) / 7); 
    x.push(WL_RATIO_COEFF * -(1.5 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * -(2 * BOAT_LENGTH) / 7);
    x.push( WL_RATIO_COEFF * -(2 * BOAT_LENGTH) / 7); 
    x.push(WL_RATIO_COEFF * -BOAT_LENGTH / 7);
    var y = new Array();// Half, length
    y.push(-(4 * BOAT_LENGTH) / 7);
    y.push(-(3 * BOAT_LENGTH) / 7);
    y.push(-(BOAT_LENGTH) / 7);
    y.push(BOAT_LENGTH / 7);
    y.push((3 * BOAT_LENGTH) / 7);
    y.push((3 * BOAT_LENGTH) / 7);
    y.push(BOAT_LENGTH / 7);
    y.push(-(BOAT_LENGTH) / 7);
    y.push(-(3 * BOAT_LENGTH) / 7);
    
    var xpoints = new Array();
    var ypoints = new Array();

    // Rotation matrix:
    // | cos(alpha)  -sin(alpha) |
    // | sin(alpha)   cos(alpha) |
    
    var ptX = cWidth / 2;
    var ptY = cHeight / 2;
    for (var i=0; i<x.length; i++)
    {
      var dx = x[i] * Math.cos(toRadians(trueHeading)) + (y[i] * (-Math.sin(toRadians(trueHeading))));
      var dy = x[i] * Math.sin(toRadians(trueHeading)) + (y[i] * Math.cos(toRadians(trueHeading)));
      xpoints.push(Math.round(ptX + dx));
      ypoints.push(Math.round(ptY + dy));
    }
    context.fillStyle = '#f00';
    context.beginPath();
    context.moveTo(xpoints[0], ypoints[0]);
    for (var i=1; i<xpoints.length; i++)   
    {
      context.lineTo(xpoints[i], ypoints[i]);
    }
    context.closePath();
    context.fill();
    context.lineWidth = 1;
    context.stroke();
  };

  var toRadians = function(deg)
  {
    return deg * (Math.PI / 180);
  };

  (function()
   { 
     canvas = document.getElementById(cName);
     instance.drawGraph();
   })(); // Invoked automatically when new is invoked.  
};
