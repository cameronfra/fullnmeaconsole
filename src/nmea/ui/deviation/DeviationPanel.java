package nmea.ui.deviation;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import nmea.event.NMEAListener;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JPanel;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedReader;

import java.io.FileReader;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import nmea.server.constants.Constants;

import nmea.ui.deviation.deviationcurve.DeviationCurve;

import ocss.nmea.parser.Angle360;

public class DeviationPanel 
     extends JPanel 
  implements MouseListener, 
             MouseMotionListener
{
  private final static DecimalFormat DF22 = new DecimalFormat("#0.00");
  
  private int draggedFromX = -1;
  private int draggedFromY = -1;
  
  private int draggedToX = -1;
  private int draggedToY = -1;
  
  private boolean dragged  = false;
  private boolean mouseDraggedEnabled = true;
  private double[] draggedPoint = null;
  
  private boolean sprayPoints = false;
  private boolean deletePoints = false;
  private boolean deleting = false;

  private Color bgColor    = Color.black;
  private Color gridColor  = Color.green;
  private Color lineColor1 = Color.green;
  private Color lineColor2 = Color.red;
  private Color lineColor3 = Color.yellow;
  
  private Hashtable<Double, Double> htDeviationCurve = new Hashtable<Double, Double>(); 
  private List<double[]> alSprayedPoints = new ArrayList<double[]>(); 
  private List<double[]> dataPoint = null;
  private boolean showData = true;
  private boolean showCurvePoints = true;
  
  private double widthFactor = 1d;  
  private double currentHDM  = 0d;
  
  public DeviationPanel()
  {
    this(null, null, null, null);
  }
  
  public DeviationPanel(Color c1, 
                        Color c2, 
                        Color c3, 
                        Color c4)
  {
    if (c1 != null) bgColor    = c1;
    if (c2 != null) gridColor  = c2;
    if (c3 != null) lineColor1 = c3;
    if (c4 != null) lineColor2 = c4;
    
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.setLayout(null);
    addMouseMotionListener(this);
    addMouseListener(this);
    this.setPreferredSize(new Dimension(400, 500));
    
    setHtDeviationCurve(Utils.loadDeviationCurve(NMEAContext.getInstance().getDeviation()));
    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void dataUpdate() 
        {
          currentHDM = ((Angle360) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_COMPASS)).getValue();
          repaint();
        }

        public void deviationCurveChanged(Hashtable<Double, Double> devCurve) 
        {
          setHtDeviationCurve(devCurve);
          repaint();
        }
      });
  }

  double xDataScale = 0d;
  double yDataScale = 0d;
  double halfWidth = 0d;
  
  double extraVerticalOverlap = 30d;
  
  private boolean printVersion = false;

  public void paintComponent(Graphics g)
  {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);      
    int w = this.getSize().width;
    int h = this.getSize().height;
    if (!printVersion)
    {
      GradientPaint gradient = new GradientPaint(0, 0, 
                                                 Color.black, 
                                                 this.getWidth(), this.getHeight(), 
                                                 new Color(80, 80, 80));
      ((Graphics2D)g).setPaint(gradient);
    }
    else
      g.setColor(Color.white);
 // g.setColor(bgColor);
    g.fillRect(0, 0, w, h);
    if (!printVersion)
      g.setColor(gridColor);
    else
      g.setColor(Color.gray);
    
    // Find boundaries
    double min = Double.MAX_VALUE;
    double max = -min;
    for (Double d : htDeviationCurve.keySet())
    {
      double val = htDeviationCurve.get(d);
//    if (val > max) max = val;
//    if (val < min) min = val;
      max = Math.max(max, val);
      min = Math.min(min, val);
    }
    double hdgOffset = ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue();
//  System.out.println("Applying HDG Offset:" + hdgOffset);
    // DataPoints boundaries
    if (dataPoint != null && showData)
    {
      for (double[] da : dataPoint) // Apply HDG Offset
      {
        double hdg = da[0], 
               cog = da[1];
        double val = (hdg - cog);
        val += hdgOffset;  
        while (val > 180) val -= 360;
        while (val < -180) val += 360;
        if (Math.abs(val) < 90)
        {
    //    if (val > max) max = val;
    //    if (val < min) min = val;
          max = Math.max(max, val);
          min = Math.min(min, val);
        }
      }
    }

    halfWidth = Math.max(Math.abs(min), Math.abs(max)) + 2;
    xDataScale = (double)w / (2 * halfWidth);
    yDataScale = (double)h / (360d + (2 * extraVerticalOverlap));
    
    // Painting deviation grid, vertical
    int minMax = (int)Math.round(Math.ceil(halfWidth)) + 2;
    for (int i=(-minMax); i<minMax; i++)
    {
      int _x = (int)(((i * widthFactor)+ halfWidth) * xDataScale);
      Color c = null;
      if (i == 0)
      {
        c = g.getColor();
        if (!printVersion)
          g.setColor(Color.red);
        else
          g.setColor(Color.black);
      }
      g.drawLine(_x, 0, _x, h);
      g.drawString(Integer.toString(i), _x, 10);
      if (c != null)
        g.setColor(c);
    }
    // Painting horizontal lines
    for (double d=-extraVerticalOverlap; d<=(360.0 + extraVerticalOverlap); d+=30.0)
    {
      int _y = (int)((d + extraVerticalOverlap) * yDataScale);
      g.drawLine(0, _y, w, _y);
      double _d = d;
      while (_d < 0) _d += 360d;
      while (_d > 360) _d -= 360d;
      g.drawString(Integer.toString((int)_d), 2, _y);
      if (_d == 0 || _d == 90 || _d == 180 || _d == 270 || _d == 360)
      {
        String card = "N";
        switch ((int)_d)
        {
          case 0   :
            break;
          case 90  :
            card = "E";
            break;
          case 180 :
            card = "S";
            break;
          case 270 :
            card = "W";
            break;
          case 360 :
            break;
        }
        Font origFont = g.getFont();
        Color origColor = g.getColor();
        int fontSize = 20;
        g.setFont(g.getFont().deriveFont(Font.BOLD, fontSize));
        g.setColor(Color.lightGray);
        int strWidth  = g.getFontMetrics(g.getFont()).stringWidth(card);
        int _x = (int)(halfWidth * xDataScale);
        g.drawString(card, _x - (int)(strWidth / 2), _y + (int)(fontSize / 2));        
        g.setFont(origFont);
        g.setColor(origColor);
      }
    }
    // Current Heading
    if (!printVersion)
    {
      g.setColor(Color.cyan);
      int _y = (int)((currentHDM + extraVerticalOverlap) * yDataScale);
      if (g instanceof Graphics2D)
      {
        // Transparency
        Graphics2D g2 = (Graphics2D)g;
        float alpha = 0.3f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.fillRect(0, _y - 5, w, 10);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
      }
//    else
      g.drawLine(0, _y, w, _y);
    }
    // Painting data points if any
    Point previousPoint = null;
    if (dataPoint != null && showData)
    {
      // Plot curve calculated after the bulk data points
      if (false)  // DISABLED
      {
        try
        {        
          List<double[]> dp = new ArrayList<double[]>();
          for (double[] da : dataPoint)
          {
            double hdg = da[0], 
                   cog = da[1];
            double dev = (hdg - cog);
            dev += hdgOffset;  
            while (dev > 180) dev -= 360;
            while (dev < -180) dev += 360;
            dp.add(new double[] {hdg, dev});
          }
          if (alSprayedPoints != null)
          {
            for (double[] da : alSprayedPoints)
            {
              double hdg = da[0], 
                     cog = da[1];
              double dev = (hdg - cog);
              dev += hdgOffset;  
              while (dev > 180) dev -= 360;
              while (dev < -180) dev += 360;
              dp.add(new double[] {hdg, dev});
            }
          }
          double[] c = DeviationCurve.calculateCurve(dp);
          g.setColor(Color.cyan);
          for (int cm=-30; cm<=390; cm += 5)
          {
            double dev = DeviationCurve.devFunc(c, cm);
  //        System.out.println("For Cm:" + cm + " dev=" + dev);
            int _x = (int)((((dev - 180) * widthFactor) + halfWidth) * xDataScale);
            int _y = (int)((extraVerticalOverlap + cm) * yDataScale);
            Point p = new Point(_x, _y);
            if (previousPoint != null)
              g.drawLine(previousPoint.x, previousPoint.y, p.x, p.y);
            previousPoint = p;
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      g.setColor(Color.yellow);
      for (double[] da : dataPoint)
      {
        double hdg = da[0], 
               cog = da[1];
        double dev = (hdg - cog);
        dev += hdgOffset;  
        while (dev > 180) dev -= 360;
        while (dev < -180) dev += 360;
        
//      System.out.println("For hdg:" + hdg + ", dev=" + val);        
        // Rounding might look weird, because HDG and COG are int values. Thus dev is also an int.
        int _x = (int)(((dev * widthFactor) + halfWidth) * xDataScale);
        int _y = (int)((extraVerticalOverlap + hdg) * yDataScale);
        g.fillOval(_x-1, _y-1, 2, 2);
      }
      if (alSprayedPoints != null)
      {
        g.setColor(Color.cyan);
        for (double[] da : alSprayedPoints)
        {
          double hdg = da[0], 
                 cog = da[1];
          double dev = (hdg - cog);
          dev += hdgOffset; 
          while (dev > 180) dev -= 360;
          while (dev < -180) dev += 360;
          int _x = (int)(((dev * widthFactor) + halfWidth) * xDataScale);
          int _y = (int)((extraVerticalOverlap + hdg) * yDataScale);
          g.fillOval(_x-1, _y-1, 2, 2);
        }
      }      
    }
    
    // Paint original deviation curve
    previousPoint = null;
    if (htDeviationCurve != null)
    {
      // Smoothed one
      try
      {
        // Dislay smoothed one, based on (possibly) suggested one
        double[] f = DeviationCurve.calculateCurve(htDeviationCurve); // TODO Dont re-smooth everytime...
        if (!printVersion)
        {
          g.setColor(lineColor3);
          // Display coefficients
          Font font = g.getFont();
          int fSize = 10;
          g.setFont(font.deriveFont(fSize));
          for (int i=0; i<f.length; i++)
            g.drawString("coef[" + i + "]=" + Double.toString(f[i]), 5, this.getHeight() - ((f.length - i - 1) * (fSize + 2)) - 2);
          g.setFont(font);
        }
        else
          g.setColor(Color.darkGray);
        for (int cm=-30; cm<=390; cm += 5)
        {
          double dev = DeviationCurve.devFunc(f, cm);
          int _x = (int)(((dev * widthFactor) + halfWidth) * xDataScale);
          int _y = (int)((extraVerticalOverlap + cm) * yDataScale);
          Point p = new Point(_x, _y);
          if (previousPoint != null)
            g.drawLine(previousPoint.x, previousPoint.y, p.x, p.y);
          previousPoint = p;
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
        
    if (!printVersion)
      g.setColor(lineColor2);
    else
      g.setColor(Color.black);
    previousPoint = null;
    Set<Double> set = htDeviationCurve.keySet();
    List<Double> list = new ArrayList<Double>(set.size());
    for (Double d: set)
      list.add(d);
    Collections.sort(list);
    Stroke origStroke = ((Graphics2D)g).getStroke();
    Stroke stroke =  new BasicStroke(2, 
                                     BasicStroke.CAP_BUTT,
                                     BasicStroke.JOIN_BEVEL);
    ((Graphics2D)g).setStroke(stroke);  
    boolean displayTwice = false;
    for (Double d : list)
    {
      double deviation = htDeviationCurve.get(d);
      double cm        = d.doubleValue();
      displayTwice = (cm < extraVerticalOverlap || cm > (360 - extraVerticalOverlap));
        
//    System.out.println("For:" + cm + ", d:" + deviation);
      int _x = (int)(((deviation * widthFactor) + halfWidth) * xDataScale);
      int _y = (int)((extraVerticalOverlap + cm) * yDataScale);
      Point p = new Point(_x, _y);
      if (showCurvePoints)
        g.drawOval(p.x - 2, p.y - 2, 4, 4);
      if (previousPoint != null)
        g.drawLine(previousPoint.x, previousPoint.y, p.x, p.y);
      previousPoint = p;
      if (displayTwice && showCurvePoints)
      {
        Color c = g.getColor();
        g.setColor(Color.white);
        if (cm < extraVerticalOverlap)
          _y = (int)((extraVerticalOverlap + cm + 360) * yDataScale);
        else
          _y = (int)((extraVerticalOverlap + cm - 360) * yDataScale);
        p = new Point(_x, _y);
        g.drawOval(p.x - 2, p.y - 2, 4, 4);
        g.setColor(c);
      }
    }    
    ((Graphics2D)g).setStroke(origStroke);  
    
    if (deleting)
    {
      if (draggedFromX != -1 && draggedFromY != -1 &&
          draggedToX != -1 && draggedToY != -1)
      {
        g.setColor(Color.green);
        int topLeftX = Math.min(draggedFromX, draggedToX);
        int topLeftY = Math.min(draggedFromY, draggedToY);
        int width  = Math.abs(draggedFromX - draggedToX);
        int height = Math.abs(draggedFromY - draggedToY);
        g.drawRect(topLeftX, topLeftY, width, height);
      }
    }
  }
  
  public void suggestCurve()
  {
    double hdgOffset = ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue();
    Hashtable<Double, Double> suggestedCurve = new Hashtable<Double, Double>(); 
    for (int i=5; i<=355; i+=10)
    {
      double value = 0d;
      int nbval = 0;
      for (double[] da : dataPoint)
      {
        if (da[0] < (i+5) && da[0] > (i-5))
        {
          double hdg = da[0], cog = da[1];
          double val = (hdg - cog);
          val += hdgOffset;  
          while (val > 180) val -= 360;
          while (val < -180) val += 360;
          value += val;
          nbval++;
        }
      }
      if (alSprayedPoints != null)
      {
        for (double[] da : alSprayedPoints)
        {
          if (da[0] < (i+5) && da[0] > (i-5))
          {
            double hdg = da[0], cog = da[1];
            double val = (hdg - cog);
            val += hdgOffset;  
            while (val > 180) val -= 360;
            while (val < -180) val += 360;
            value += val;
            nbval++;
          }
        }
      }
      Double dbl = new Double(value / (double)nbval);
      if (dbl.equals(Double.NaN))
        dbl = new Double(0d);
      suggestedCurve.put(new Double(i), dbl);
    }
    htDeviationCurve = suggestedCurve;
    List<double[]> ald = Utils.loadDeviationCurve(htDeviationCurve);
    NMEAContext.getInstance().setDeviation(ald);
    repaint();
  }
  
  public void resetSprayedPoints()
  {
    alSprayedPoints = null;
  }
  
  public void stickPointsToCurve()
  {
    try
    {
      double[] f = DeviationCurve.calculateCurve(htDeviationCurve); 
      Hashtable<Double, Double> ht = new Hashtable<Double, Double>(); 
      for (int cm=0; cm<=360; cm += 5)
      {
        double dev = DeviationCurve.devFunc(f, cm);
        ht.put(new Double(cm), new Double(dev));
      }
      htDeviationCurve = ht;
      // TODO Ask question 
      List<double[]> ald = Utils.loadDeviationCurve(htDeviationCurve);
      NMEAContext.getInstance().setDeviation(ald);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  // Tooltip
  public void mouseMoved(MouseEvent e)
  {
    Point mouse = e.getPoint();
    double value = (((double)mouse.x / xDataScale) - halfWidth) / widthFactor;
    int cm       = (int)Math.round(((double)mouse.y / yDataScale) - extraVerticalOverlap);
    while (cm < 0)   cm += 360;
    while (cm > 360) cm -= 360;
    this.setToolTipText("<html>d=" + 
                          DF22.format(value) + "\272<br>Z=" + 
                          Integer.toString(cm) + "\272" +
                        "</html>");
    if (findClosest(mouse) != null)
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    else    
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseClicked(MouseEvent e)
  {
    e.consume();
    // TASK delete point?
//  int x = e.getX();
//  int y = e.getY();
//  String mess = "Pos:" + Integer.toString(x) + ", " + Integer.toString(y);
//  System.out.println(mess);
  }

  public void mousePressed(MouseEvent e)
  {
    if (mouseDraggedEnabled)
    {
      draggedFromX = e.getX();
      draggedFromY = e.getY();
      if (!dragged)
        draggedPoint = findClosest(e.getPoint());
      if (draggedPoint != null)
      {
//      System.out.println("Found! " + draggedPoint[0] + ":" + draggedPoint[1]);
        // Change the component's cursor to another shape
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      else if (deletePoints)
      {
//      System.out.println("Starting Delete Points process...");
        deleting = true;
      }
      else if (sprayPoints && !deletePoints)
      {
        Point mouse = e.getPoint();
        sprayPoints(mouse);
        suggestCurve();
        this.repaint();
      }
    }
  }
  
  private double[] findClosest(Point p)
  {
    double[] ret = null;
    double value = (((double)p.x / xDataScale) - halfWidth) / widthFactor;
    int cm       = (int)Math.round(((double)p.y / yDataScale) - extraVerticalOverlap);
    
    Double found = htDeviationCurve.get(cm);
    if (found != null && found.doubleValue() == value)
    {
      ret = new double[2];
      ret[0] = cm;
      ret[1] = value;
    }
    else
    {
      for (Double d : htDeviationCurve.keySet())
      {
        double val = htDeviationCurve.get(d);
        if ((Math.abs(value - val) < 0.5) &&
            (Math.abs(cm - d.doubleValue()) < 2.0))
        {
          ret = new double[2];
          ret[0] = d.doubleValue();
          ret[1] = val;
        }
      }      
    }    
    return ret;
  }

  public void setMouseDraggedEnabled(boolean b)
  {
    mouseDraggedEnabled = b;
  }

  public boolean getMouseDraggedEnabled()
  {
    return mouseDraggedEnabled;
  }

  private static boolean showDuringDrag = true;
  
  private void sprayPoints(Point mouse)
  {
    if (alSprayedPoints == null)
      alSprayedPoints = new ArrayList<double[]>();
    final int NB_SPAYED_POINTS = 50; // TODO Parameter
    final int SPRAY_RADIUS     = 10; // TODO Parameter
    for (int i=0; i<NB_SPAYED_POINTS; i++)
    {
      double rnd = (Math.random() * SPRAY_RADIUS) * (Math.random() > 0.5?1:-1);
      double value = ((((double)mouse.x + rnd)/ xDataScale) - halfWidth) / widthFactor;  // Dev
      rnd = (Math.random() * SPRAY_RADIUS) * (Math.random() > 0.5?1:-1);
      double cm    = (((double)mouse.y + rnd)/ yDataScale) - extraVerticalOverlap;       // HDM
      cm += ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue();
      while (cm < 0)   cm += 360;
      while (cm > 360) cm -= 360;
      alSprayedPoints.add(new double[] { cm, cm - value });
    }
  }
  
  public void mouseDragged(MouseEvent e)
  {
    Point mouse = e.getPoint();
    if (sprayPoints && !deletePoints)
    {
      sprayPoints(mouse);
      suggestCurve();
      this.repaint();
    }

    if (mouseDraggedEnabled)
    {
      dragged = true;
      if (showDuringDrag)
      {
        if (draggedPoint != null)
        {
          // Show New data, even during drag
          double value = (((double)e.getPoint().x / xDataScale) - halfWidth) / widthFactor;
          int cm       =  (int)Math.round((double)e.getPoint().y / yDataScale);
  //      int cm       =  (int)draggedPoint[0]; // Horizontal only
          // Remove previous one
          htDeviationCurve.remove(draggedPoint[0]);
          // Add new one
          htDeviationCurve.put((double)cm, value);  // Only if moved.
        }  
        else if (deleting)
        {
          draggedToX = mouse.x;
          draggedToY = mouse.y;
        }
        this.repaint(); 
      }
    }
  }

  public void mouseReleased(MouseEvent e)
  {
//  System.out.println("released");
    if (dragged)
    {
      // New data
      double value = (((double)e.getPoint().x / xDataScale) - halfWidth) / widthFactor;
      int cm       = (int)Math.round(((double)e.getPoint().y / yDataScale) - extraVerticalOverlap);      
      if (draggedPoint != null)
      {
        // Remove previous one
        htDeviationCurve.remove(draggedPoint[0]);
        // Add new one
        htDeviationCurve.put((double)cm, value);  // Only if moved.
      } 
      else if (deleting)
      {
        deleting = false;              
//      System.out.println("Deleting points !");
        int minX = Math.min(draggedFromX, draggedToX);
        int maxX = Math.max(draggedFromX, draggedToX);
        int minY = Math.min(draggedFromY, draggedToY);
        int maxY = Math.max(draggedFromY, draggedToY);
        double minHdg = ((double)minY / yDataScale) - extraVerticalOverlap;
        double maxHdg = ((double)maxY / yDataScale) - extraVerticalOverlap;
        double minDev = (((double)minX / xDataScale) - halfWidth) / widthFactor;
        double maxDev = (((double)maxX / xDataScale) - halfWidth) / widthFactor;
        
        String mess = "Deleting from\nHDG [" +
                       DF22.format(minHdg) + ", " + DF22.format(maxHdg) + "]\nDEV [" + 
                       DF22.format(minDev) + ", " + DF22.format(maxDev) + "]";
//      System.out.println(mess);
        // Count the points to delete
        double hdgOffset = ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue();
        int nbPtDeleted = 0;
        for (double[] d : dataPoint)
        {
          double hdg = d[0], 
                 cog = d[1];
          double dev = (hdg - cog);
          dev += hdgOffset;  
          while (dev > 180) dev -= 360;
          while (dev < -180) dev += 360;
        //          System.out.println("Is " + DF22.format(hdg) + " in [" + DF22.format(minHdg) + ", " + DF22.format(maxHdg) + "] and\n" +
        //                             "   " + DF22.format(dev) + " in [" +  DF22.format(minDev) + ", " + DF22.format(maxDev) + "] ?");
          if (hdg >= minHdg && hdg <= maxHdg && dev >= minDev && dev <= maxDev)
            nbPtDeleted++;
        }
        mess += ("\n\nWould delete " + Integer.toString(nbPtDeleted) + " point(s)");
        // Prompt the user
        int resp = JOptionPane.showConfirmDialog(this, mess, "Deleting points", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resp == JOptionPane.OK_OPTION)
        {
       /* int */ nbPtDeleted = 0;
//        double hdgOffset = ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue();
          List<double[]> newData = new ArrayList<double[]>();
          for (double[] d : dataPoint)
          {
            double hdg = d[0], 
                   cog = d[1];
            double dev = (hdg - cog);
            dev += hdgOffset;  
            while (dev > 180) dev -= 360;
            while (dev < -180) dev += 360;
//          System.out.println("Is " + DF22.format(hdg) + " in [" + DF22.format(minHdg) + ", " + DF22.format(maxHdg) + "] and\n" +
//                             "   " + DF22.format(dev) + " in [" +  DF22.format(minDev) + ", " + DF22.format(maxDev) + "] ?");
            if (hdg >= minHdg && hdg <= maxHdg && dev >= minDev && dev <= maxDev)
              nbPtDeleted++;
            else
              newData.add(d);
          }
          synchronized (dataPoint) { dataPoint = newData; }
          /*
          Set<Double> set = htDeviationCurve.keySet();
          for (Double hdg : set)
          {
            double dev = htDeviationCurve.get(hdg).doubleValue();
            if (hdg.doubleValue() >= minHdg && hdg.doubleValue() <= maxHdg && dev >= minDev && dev <= maxDev)
            {
              htDeviationCurve.remove(hdg);
              nbPtDeleted++;
            }
          }
          */
          newData = new ArrayList<double[]>();
          for (double[] da : alSprayedPoints)
          {
            double hdg = da[0], 
                   cog = da[1];
            double dev = (hdg - cog);
            dev += hdgOffset;  
            while (dev > 180) dev -= 360;
            while (dev < -180) dev += 360;
            if (hdg >= minHdg && hdg <= maxHdg && dev >= minDev && dev <= maxDev)
              nbPtDeleted++;
            else
              newData.add(da);
          }
          synchronized (alSprayedPoints) { alSprayedPoints = newData; }
          suggestCurve();
//        System.out.println("Deleted " + nbPtDeleted + " Pt(s).");
        }        
        draggedToX = -1;
        draggedToY = -1;
      }
      dragged = false;
//    System.out.println("Moved from " + draggedFromX + "/" + draggedFromY + 
//                       " to " + e.getX() + "/" + e.getY() +
//                       " -> " + pp.getTwa() + "/" + pp.getBsp());
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      List<double[]> ald = Utils.loadDeviationCurve(htDeviationCurve);
      NMEAContext.getInstance().setDeviation(ald);
      repaint();
    }
  }
  
  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }
  
  public void setHtDeviationCurve(Hashtable<Double, Double> ht)
  {
    this.htDeviationCurve = ht;
  }

  public Hashtable<Double, Double> getHtDeviationCurve()
  {
    return htDeviationCurve;
  }
  
  public void setDataPoint(List<double[]> dataPoint)
  {
    this.dataPoint = dataPoint;
  }
  public List<double[]> getDataPoint()
  {
    return dataPoint;
  }


  public static void main2(String[] args) throws Exception
  {
    DeviationPanel dp = new DeviationPanel();
    Hashtable<Double, Double> data = new Hashtable<Double, Double>();  
    
    // Read a CSV file
    FileReader fr = new FileReader("C:\\_myWork\\_ForExport\\dev-corner\\logisail\\DashBoard\\deviation.csv");
    BufferedReader br = new BufferedReader(fr);
    String line = "";
    while ((line = br.readLine()) != null)
    {
      String[] sa = line.split(",");
      double cm = Double.parseDouble(sa[0]);
      double d  = Double.parseDouble(sa[1]);
      data.put(cm, d);
    }
    br.close();
    fr.close();
    
    dp.setHtDeviationCurve(data);
    
    JOptionPane.showMessageDialog(null, dp, "Deviation", JOptionPane.PLAIN_MESSAGE);
    data = dp.getHtDeviationCurve();
    
    Set<Double> set = data.keySet();
    List<Double> list = new ArrayList<Double>(set.size());
    for (Double d: set)
      list.add(d);
    Collections.sort(list);
    for (Double d : list)
    {
      double deviation = data.get(d);
      double cm        = d.doubleValue();
      
      System.out.println(cm + "," + deviation);
    }
  }

  public void setWidthFactor(double widthFactor)
  {
    this.widthFactor = widthFactor;
  }

  public double getWidthFactor()
  {
    return widthFactor;
  }

  public void setShowData(boolean showData)
  {
    this.showData = showData;
  }

  public boolean isShowData()
  {
    return showData;
  }
  
  public void setShowCurveData(boolean b)
  {
    this.showCurvePoints = b;
  }

  public boolean isShowCurvePoints()
  {
    return showCurvePoints;
  }

  public void setPrintVersion(boolean printVersion)
  {
    this.printVersion = printVersion;
  }

  public void setSprayPoints(boolean sprayPoints)
  {
    this.sprayPoints = sprayPoints;
  }
  
  public void setDeletePoints(boolean deletePoints)
  {
    this.deletePoints = deletePoints;
    setMouseDraggedEnabled(deletePoints);
  }
}
