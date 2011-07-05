package nmea.ui.viewer;


import astro.calc.GeoPoint;
import astro.calc.GreatCircle;

import chart.components.ui.ChartPanelParentInterface;
import chart.components.ui.PlottingSheet;
import chart.components.util.MercatorUtil;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import javax.swing.border.BevelBorder;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import nmea.event.NMEAListener;

import nmea.ui.viewer.elements.CurrentDisplay;

import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.UTCDate;

import user.util.GeomUtil;


public class DeadReckoningPlottingSheet
  extends JPanel
  implements ChartPanelParentInterface
{
  private PlottingSheet plottingSheet;
  private JPanel        topPanel;
  private JComboBox     timeComboBox;
  private JButton       resetButton;
  private JPanel centerPanel = new JPanel(new BorderLayout());
  private JPanel bottomPanel = new JPanel();
  private CurrentDisplay currentDisplay = new CurrentDisplay("Current", "00.00", "Current", 30);
  
  private GeoPoint[] groundData = null;
  private GeoPoint[] drData     = null;

  private int width  = 400;
  private int height = 400;
  private double centerLat = 0d;
  private double centerLng = 0d;
  private double latSpan   = 1d;
  
  private long bufferLength = 60000L;
  
  // Time, Position, CMG, BSP.
  private ArrayList<UTCDate> timeBuffer    = new ArrayList<UTCDate>();
  private ArrayList<GeoPos> positionBuffer = new ArrayList<GeoPos>();
  private ArrayList<Angle360> cmgBuffer    = new ArrayList<Angle360>();
  private ArrayList<Angle360> hdgBuffer    = new ArrayList<Angle360>();
  private ArrayList<Speed> bspBuffer       = new ArrayList<Speed>();

  public DeadReckoningPlottingSheet(int w, 
                                    int h, 
                                    double cL, 
                                    double cG, 
                                    double ls)
  {
    super();
    width = w;
    height = h;
    centerLat = cL;
    centerLng = cG;
    latSpan = ls;
    
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    plottingSheet = new PlottingSheet(this, width, height, centerLat, centerLng, latSpan);    
    plottingSheet.setWithDistanceScale(true);

    centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    topPanel = new JPanel();
    this.setLayout(new BorderLayout());
    this.setSize(new Dimension(400, 400));
    centerPanel.add(plottingSheet, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);
    timeComboBox = new JComboBox();
    timeComboBox.removeAllItems();
    timeComboBox.addItem(new TimeObject(  60000L,  "1 minute"));
    timeComboBox.addItem(new TimeObject( 120000L,  "2 minutes"));
    timeComboBox.addItem(new TimeObject( 300000L,  "5 minutes"));
    timeComboBox.addItem(new TimeObject( 600000L, "10 minutes"));
    timeComboBox.addItem(new TimeObject(1200000L, "20 minutes"));
    timeComboBox.addItem(new TimeObject(1800000L, "30 minutes"));
    timeComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          setBufferLength(((TimeObject)timeComboBox.getSelectedItem()).getTime());
        }
      });    
    resetButton = new JButton("Reset");
    resetButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent e)
        {
          timeBuffer     = new ArrayList<UTCDate>();
          positionBuffer = new ArrayList<GeoPos>();
          cmgBuffer      = new ArrayList<Angle360>();
          hdgBuffer    = new ArrayList<Angle360>();
          bspBuffer      = new ArrayList<Speed>();          
        }
      });
    currentDisplay.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    topPanel.add(timeComboBox, null);
    topPanel.add(resetButton, null);
    
    this.add(topPanel, BorderLayout.NORTH);

    currentDisplay.setDisplayColor(Color.cyan);
    bottomPanel.add(currentDisplay, null);
    this.add(bottomPanel, BorderLayout.SOUTH);
    NMEAContext.getInstance().addNMEAListener(new NMEAListener()
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try 
            { 
//            long time       = new Date().getTime();
              UTCDate utcDate = (UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME);
              Angle360 cmg    = (Angle360)cache.get(NMEADataCache.CMG); 
              GeoPos position = (GeoPos)cache.get(NMEADataCache.POSITION);
              Speed bsp       = (Speed)cache.get(NMEADataCache.BSP); 
              Angle360 hdg    = (Angle360)cache.get(NMEADataCache.HDG_TRUE); 
              if (timeBuffer.size() == 0 || (timeBuffer.size() > 0 && (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime())))
              {
                if (utcDate != null && cmg != null && position != null && bsp != null && hdg != null)
                {
                  if (timeBuffer.size() > 0)
                  {
                    UTCDate oldest = timeBuffer.get(0);
                    boolean keepGoing = true;
                    while (keepGoing && oldest.getValue().getTime() < (utcDate.getValue().getTime() - bufferLength))
                    {
                      timeBuffer.remove(0);
                      positionBuffer.remove(0);
                      cmgBuffer.remove(0);
                      bspBuffer.remove(0);
                      hdgBuffer.remove(0);

                      if (timeBuffer.size() > 0)
                        oldest = timeBuffer.get(0);
                      else
                        keepGoing = false;
                    }
                  }
                  timeBuffer.add(utcDate);
                  positionBuffer.add(position);
//                System.out.println("Adding position:" + position.toString());
                  cmgBuffer.add(cmg);
                  bspBuffer.add(bsp);
                  hdgBuffer.add(hdg);
                  groundData = new GeoPoint[positionBuffer.size()];
                  int index = 0;
                  for (GeoPos gp : positionBuffer)
                  {
                    groundData[index++] = new GeoPoint(gp.lat, gp.lng);
                  }
                  index = 0;
                  drData     = new GeoPoint[positionBuffer.size()];
                  GeoPos drPos = positionBuffer.get(0);
                  int size = positionBuffer.size();
                  for (int i=0; i<size; i++)
                  {
                    if (i > 0)
                    {
                      long timeInterval = timeBuffer.get(i).getValue().getTime() - timeBuffer.get(i-1).getValue().getTime();
                      double bspeed = bspBuffer.get(i).getDoubleValue();
//                    System.out.println("-- TimeInterval:" + timeInterval + ", bsp:" + bspeed);
                      if (bspeed > 0)
                      {
                        double dist = bspeed * ((double)timeInterval / (double)3600000L); // in minutes (miles)
                        double rv   = cmgBuffer.get(i - 1).getValue();
//                      System.out.println("** In " + timeInterval + " ms, at " + bspeed + " kts, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + "... ");
                        if (dist > 0)
                        {
                          GeoPoint pt = MercatorUtil.deadReckoning(drPos.lat, drPos.lng, dist, rv);                      
//                        System.out.println("In " + timeInterval + " ms, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + ", ends up " + pt.toString());
                          drPos = new GeoPos(pt.getL(), pt.getG());
                        }
//                      else
//                        System.out.println("** dist : 0");
                      }
//                    else
//                      System.out.println("-- speed : 0");
                    } 
                    if (false)
                    {
                      if (i == 0)
                        System.out.println("-----------------------");
                      System.out.println("Adding to drData (" + i + ") :" + drPos.toString());
                    }
                    drData[i] = new GeoPoint(drPos.lat, drPos.lng);
                  }
                  setBoundaries();
                  repaint();
                }
              }
            }
            catch (Exception ex) 
            {
              ex.printStackTrace();
            }
          }
        }        
      });
  }
  
  public void chartPanelPaintComponent(Graphics gr)
  {
    plottingSheet.chartPanelPaintComponent(gr);
    /*
    double saveZoom = plottingSheet.getZoomFactor();
    double z = Math.min((double) centerPanel.getWidth() / (double) plottingSheet.getWidth(),
                        (double) centerPanel.getHeight() / (double) plottingSheet.getHeight());
    plottingSheet.setZoomFactor(z);
    plottingSheet.zoomIn();
    plottingSheet.setZoomFactor(saveZoom);    
    */
    Stroke stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    ((Graphics2D) gr).setStroke(stroke);

    // Over Ground
    gr.setColor(Color.black);
    Point ppt = null;
//  System.out.println("GroundData.length = " + groundData.length);
    for (int i=0; groundData != null && i<groundData.length; i++)
    {
      Point pt = null;
      try { pt = plottingSheet.getPanelPoint(groundData[i]); } catch (Exception nep) { System.err.println("Pt:" + nep.toString()); } 
      if (pt != null && i == 0)
      {
        String[] sa =  groundData[i].toString().split("/");
        String one = sa[0].trim();
        String two = sa[1].trim();
        int len = Math.max(one.length(), two.length());
        one = Utils.lpad(one, " ", len);
        two = Utils.lpad(two, " ", len);
        Font f = gr.getFont();
        gr.setFont(new Font("courier new", Font.PLAIN, 11));
        plottingSheet.postit(gr, one + "\n" + two, pt.x, pt.y, Color.yellow, Color.blue, 0.55f);
        gr.setFont(f);
      }

      if (pt != null && i == (groundData.length - 1))
      {
        String[] sa =  groundData[i].toString().split("/");
        String one = sa[0].trim();
        String two = sa[1].trim();
        int len = Math.max(one.length(), two.length());
        one = Utils.lpad(one, " ", len);
        two = Utils.lpad(two, " ", len);
        Font f = gr.getFont();
        gr.setFont(new Font("courier new", Font.PLAIN, 11));
        plottingSheet.postit(gr, one + "\n" + two, pt.x, pt.y, Color.yellow, Color.blue, 0.55f);
        gr.setFont(f);
      }
      
      if (ppt != null && pt != null)
      {        
        gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
        ppt = pt;
      }
      ppt = pt;
    }
    // Thru water
    gr.setColor(Color.red);
    ppt = null;
//  System.out.println("DRData.length = " + drData.length);
    for (int i=0; drData != null && i<drData.length; i++)
    {
      Point pt = null;
      try
      {
        if (drData[i] != null)
          pt = plottingSheet.getPanelPoint(drData[i]);
      }
      catch (Exception ex)
      {
        System.err.println("drData[" + i + "]:");
        ex.printStackTrace();
      }
      if (ppt != null && pt != null)
      {
        gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
//      ppt = pt;
      }
      ppt = pt;
    }
    // Boat
    if (groundData != null && groundData.length > 0 && hdgBuffer.size() > 0)
    {
      Point pt = null;
      try { pt = plottingSheet.getPanelPoint(groundData[groundData.length - 1]); } catch (Exception nep) { System.err.println("Drawing boat:" + nep.toString()); } 
      if (pt != null)
      {
        stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
        Utils.drawBoat((Graphics2D) gr, 
                       Color.blue, 
                       Color.gray,
                       Color.black,
                       pt, 
                       50, 
                       (int) hdgBuffer.get(hdgBuffer.size() - 1).getDoubleValue(),
                       0.75f);
      }
    }
    // Current
    try
    {
      if (groundData != null && groundData.length > 0 && drData != null && drData.length > 0)
      {
        gr.setColor(Color.green);
        Point p1 = null;
        try { p1 = plottingSheet.getPanelPoint(groundData[groundData.length - 1]); } catch (Exception npe) { System.err.println("P1:" + npe.toString()); }
        Point p2 = null;
        try { p2 = plottingSheet.getPanelPoint(drData[drData.length - 1]); } catch (Exception npe) { System.err.println("P2:" + npe.toString()); }
        if (p1 != null && p2 != null)
        {
          GeoPoint geoFrom = new GeoPoint(Math.toRadians(drData[drData.length - 1].getL()),         
                                          Math.toRadians(drData[drData.length - 1].getG())); 
          GeoPoint geoTo   = new GeoPoint(Math.toRadians(groundData[groundData.length - 1].getL()), 
                                          Math.toRadians(groundData[groundData.length - 1].getG())); 
          double dist = GreatCircle.calculateRhumLineDistance(geoFrom, geoTo);
          double dir  = Math.toDegrees(GreatCircle.calculateRhumLineRoute(geoFrom, geoTo));
          double hourRatio = (double)(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - timeBuffer.get(0).getValue().getTime()) / (double)3600000L;
          double speed = dist / hourRatio;
          
//        System.out.println("-- Speed:" + speed + " (" + dist + " nm in " + hourRatio + " hour(s).)");
  
          stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
          ((Graphics2D) gr).setStroke(stroke);
          Utils.drawCurrentArrow((Graphics2D) gr, p2, p1, Color.green);
          if (speed > 0.25)
            Utils.drawHollowArrow((Graphics2D) gr, p2, p1, Color.green);
  //      gr.drawLine(p1.x, p1.y, p2.x, p2.y);
          // Display values
          gr.setColor(Color.blue);
          Font f = gr.getFont();
          gr.setFont(new Font("courier new", Font.PLAIN, 12));
          gr.drawString("Current dir  :" + NMEAContext.DF3.format(dir) + "\272", 10, 10);
          gr.drawString("Current speed:" + NMEAContext.DF22.format(speed) + " kts", 10, 25);
          gr.setFont(f);
          currentDisplay.setDirection(dir);
          currentDisplay.setSpeed(speed);
          currentDisplay.repaint();
        }
      }
    }
    catch (Exception ex)
    {
      System.err.println("Displaying current in DeadReckoningPlottingSheet:");
//    System.err.println(ex.toString());
      ex.printStackTrace();
    }
  }

  public boolean onEvent(EventObject eventobject, int i)
  {
    return false;
  }

  public String getMessForTooltip()
  {
    return null;
  }

  public boolean replaceMessForTooltip()
  {
    return false;
  }

  public void videoCompleted()
  {
  }

  public void videoFrameCompleted(Graphics g, Point p)
  {
  }

  public void zoomFactorHasChanged(double d)
  {
  }

  public void chartDDZ(double top, double bottom, double left, double right)
  {
  }

  private void setBoundaries()
  {
    // Calculate center
    double top = -90d, bottom = 90d, right = -180d, left = 180d;
    for (int i=0; groundData != null && i<groundData.length; i++)
    {
      GeoPoint gp = groundData[i];
      if (gp.getL() < bottom) bottom = gp.getL();
      if (gp.getL() > top) top = gp.getL();
      if (gp.getG() > right) right = gp.getG();
      if (gp.getG() < left) left = gp.getG();
    }
    for (int i=0; drData != null && i<drData.length; i++)
    {
      GeoPoint gp = drData[i];
      if (gp.getL() < bottom) bottom = gp.getL();
      if (gp.getL() > top) top = gp.getL();
      if (gp.getG() > right) right = gp.getG();
      if (gp.getG() < left) left = gp.getG();
    }
//  System.out.println("Width:" + (right - left) + ", Height:" + (top - bottom));
    double max = Math.max((top - bottom), (right - left));
//  if (max < 0.001d)
//    max = 0.001d;
    
    plottingSheet.setChartLatitudeSpan(max /*(top - bottom)*/ * 1.1d);
    plottingSheet.setCenterLat(bottom + ((top - bottom) / 2d));
    plottingSheet.setCenterLong(left + ((right - left) / 2d));
    
    this.repaint();
  }
  
  public void setGroundData(GeoPoint[] groundData)
  {
    this.groundData = groundData;
    setBoundaries();
  }

  public void setDrData(GeoPoint[] drData)
  {
    this.drData = drData;
    setBoundaries();
  }

  public void setCenterLat(double centerLat)
  {
    plottingSheet.setCenterLat(centerLat);
    this.centerLat = centerLat;
  }

  public void setCenterLng(double centerLng)
  {
    plottingSheet.setCenterLong(centerLng);
    this.centerLng = centerLng;
  }

  public void setLatSpan(double latSpan)
  {
    plottingSheet.setChartLatitudeSpan(latSpan);
    this.latSpan = latSpan;
  }
  
  public long getTimeWindow()
  {
    return ((TimeObject)timeComboBox.getSelectedItem()).getTime();
  }

  public void setBufferLength(long bufferLength)
  {
    this.bufferLength = bufferLength;
  }

  public PlottingSheet getPlottingSheet()
  {
    return plottingSheet;
  }

  public static class TimeObject
  {
    private long time;
    private String label;
    public TimeObject(long time, String label)
    {
      this.time = time;
      this.label = label;
    }
    
    public String toString()
    {
      return label;
    }

    public void setTime(long time)
    {
      this.time = time;
    }

    public long getTime()
    {
      return time;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getLabel()
    {
      return label;
    }
  }
}
