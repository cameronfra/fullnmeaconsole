package nmea.ui.deviation;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;
import nmea.ctx.Utils;

import nmea.event.NMEAListener;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

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

import java.util.Set;

import javax.swing.JOptionPane;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Angle360;

public class DeviationPanel 
     extends JPanel 
  implements MouseListener, 
             MouseMotionListener
{
  private final static DecimalFormat DF22 = new DecimalFormat("#0.00");
  
  private int draggedFromX = -1;
  private int draggedFromY = -1;
  private boolean dragged  = false;
  private boolean mouseDraggedEnabled = true;
  private double[] draggedPoint = null;

  private Color bgColor    = Color.black;
  private Color gridColor  = Color.green;
  private Color lineColor1 = Color.green;
  private Color lineColor2 = Color.red;
  
  private Hashtable<Double, Double> htDeviationCurve = new Hashtable<Double, Double>(); 
  private ArrayList<double[]> dataPoint = null;
  private boolean showData = true;
  
  private double widthFactor = 1d;  
  private double currentHDM = 0d;
  
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

  public void paintComponent(Graphics g)
  {
    int w = this.getSize().width;
    int h = this.getSize().height;
    GradientPaint gradient = new GradientPaint(0, 0, 
                                               Color.black, 
                                               this.getWidth(), this.getHeight(), 
                                               new Color(80, 80, 80));
    ((Graphics2D)g).setPaint(gradient);
    
 // g.setColor(bgColor);
    g.fillRect(0, 0, w, h);
    g.setColor(gridColor);
    
    // Find boundaries
    double min = Double.MAX_VALUE;
    double max = -min;
    for (Double d : htDeviationCurve.keySet())
    {
      double val = htDeviationCurve.get(d);
      if (val > max) max = val;
      if (val < min) min = val;
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
          if (val > max) max = val;
          if (val < min) min = val;
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
        g.setColor(Color.red);
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
    }
    // Current Heading
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
    if (dataPoint != null && showData)
    {
      g.setColor(Color.yellow);
      for (double[] da : dataPoint)
      {
        double hdg = da[0], 
               cog = da[1];
        double val = (hdg - cog);
        val += hdgOffset;  
        while (val > 180) val -= 360;
        while (val < -180) val += 360;
        
//      System.out.println("For hdg:" + hdg + ", dev=" + val);        
        // Rounding might look weird, because HDG and COG are int values. Thus dev is also an int.
        int _x = (int)(((val * widthFactor) + halfWidth) * xDataScale);
        int _y = (int)((extraVerticalOverlap + hdg) * yDataScale);
        g.fillOval(_x-1, _y-1, 2, 2);
      }
    }
    
    // Paint original deviation curve
    g.setColor(lineColor2);
    Point previousPoint = null;
    Set<Double> set = htDeviationCurve.keySet();
    ArrayList<Double> list = new ArrayList<Double>(set.size());
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
      g.drawOval(p.x - 2, p.y - 2, 4, 4);
      if (previousPoint != null)
        g.drawLine(previousPoint.x, previousPoint.y, p.x, p.y);
      previousPoint = p;
      if (displayTwice)
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
      Double dbl = new Double(value / (double)nbval);
      if (dbl.equals(Double.NaN))
        dbl = new Double(0d);
      suggestedCurve.put(new Double(i), dbl);
    }
    htDeviationCurve = suggestedCurve;
    ArrayList<double[]> ald = Utils.loadDeviationCurve(htDeviationCurve);
    NMEAContext.getInstance().setDeviation(ald);
    repaint();
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
    // TASK Add point, delete point?
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

  private static boolean showDuringDrag = false;
  
  public void mouseDragged(MouseEvent e)
  {
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
      dragged = false;
//    System.out.println("Moved from " + draggedFromX + "/" + draggedFromY + 
//                       " to " + e.getX() + "/" + e.getY() +
//                       " -> " + pp.getTwa() + "/" + pp.getBsp());
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      ArrayList<double[]> ald = Utils.loadDeviationCurve(htDeviationCurve);
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
  
  public void setDataPoint(ArrayList<double[]> dataPoint)
  {
    this.dataPoint = dataPoint;
  }
  public ArrayList<double[]> getDataPoint()
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
    ArrayList<Double> list = new ArrayList<Double>(set.size());
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
}
