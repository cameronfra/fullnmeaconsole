var getCSSClass = function(cssClassName)
{
  var classText;
  var ss = document.styleSheets;
  for (var i=0; i<ss.length; i++)
  {
    var classes = ss[i].rules;
    try
    {
      if ((classes === undefined || classes === null) && ss[i].cssRules !== undefined)
        classes = ss[i].cssRules;
    }
    catch (err)
    { console.log(err); }
    if (classes !== null)
    {
      for (var j=0; j<classes.length; j++)
      {
        var st = classes[j].selectorText;
        if (st === cssClassName)
        {
          var ct = classes[j].style.cssText;
          classText = ct;
        }
      }
    }
  }  
  return classText;
};
