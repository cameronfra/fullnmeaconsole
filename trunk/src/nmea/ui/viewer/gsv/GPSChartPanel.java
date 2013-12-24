
package nmea.ui.viewer.gsv;

import astro.calc.GeoPoint;

import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JPanel;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.server.utils.ChartColor;

import ocss.nmea.parser.GeoPos;

public class GPSChartPanel
     extends JPanel
  implements ChartPanelParentInterface
{
  public final static int CHART_WIDTH = 400;
  private BorderLayout borderLayout1;
  private ChartPanel chartPanel;
  
  ArrayList<GeoPoint> track = new ArrayList<GeoPoint>();

  public GPSChartPanel()
  {
    borderLayout1 = new BorderLayout();
    chartPanel = new ChartPanel(this);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
        throws Exception
  {
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener()
    {
      public void manageNMEAString(String str)
      {
//      System.out.println("NMEA:" + str);
        if (str.trim().length() > 6 && str.startsWith("$"))
        {
          if (str.substring(3, 6).equals("RMC") || str.substring(3, 6).equals("GLL"))
          {
            try
            {
              GeoPos pos = (GeoPos)NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
              plot = new GeoPoint(pos.lat, pos.lng);
              repaint();
            }
            catch (Exception ex)
            {
              // No cache yet
            }
          }
        }
      }
    });
    setLayout(borderLayout1);
    chartPanel.setProjection(ChartPanel.ANAXIMANDRE);
    add(chartPanel, BorderLayout.CENTER);

    double nLat = 83D;
    double sLat = -65D;
    double wLong = -180D;
    double eLong = 180D; // chartPanel.calculateEastG(nLat, sLat, wLong);
    chartPanel.setEastG(eLong);
    chartPanel.setWestG(wLong);
    chartPanel.setNorthL(nLat);
    chartPanel.setSouthL(sLat);
    
    chartPanel.setWithGrid(false);
//  chartPanel.setPreferredSize(new Dimension(300, 200));
    int chartWidth = CHART_WIDTH;
    chartPanel.setW(chartWidth);
    chartPanel.setH((int)(chartWidth * (nLat - sLat) / 360d));
//  chartPanel.setWidthFromChart(nLat, sLat, wLong, eLong);
    chartPanel.setHorizontalGridInterval(10D);
    chartPanel.setVerticalGridInterval(10D);
    chartPanel.setWithScale(false);

    chartPanel.setMouseDraggedEnabled(false);
    
    chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    chartPanel.setPositionToolTipEnabled(true);
  }

  GeoPoint plot = null;
  
  public void chartPanelPaintComponent(Graphics gr)
  {
    int w = chartPanel.getW();
    int h = chartPanel.getH();
    gr.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
    Graphics2D g2d = null;
    if (gr instanceof Graphics2D)
      g2d = (Graphics2D)gr;
    World.paintChart(null, chartPanel, g2d, Color.orange);
    World.drawChart(chartPanel, gr);
    // Pos
    gr.setColor(Color.red);
    if (plot != null)
    {
      Point pt = chartPanel.getPanelPoint(plot);
//    gr.fillOval(pt.x - 5, pt.y - 5, 10, 10);
      Color lightColor = ChartColor.YELLOW;
      Color darkColor  = ChartColor.DARK_BLUE; 
      drawGlossyCircularBall(g2d, pt, 5, lightColor, darkColor, 0.9f);
    }
  }

  public boolean onEvent(EventObject e, int type)
  {
    return true;
  }

  public String getMessForTooltip()
  {
    return null;
  }

  public boolean replaceMessForTooltip()
  {
    return false;
  }

  public void videoCompleted() {}
  public void videoFrameCompleted(Graphics g, Point p) {}

  public void zoomFactorHasChanged(double d)
  {
  }

  public void chartDDZ(double top, double bottom, double left, double right)
  {
  }

  private static void drawGlossyCircularBall(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
    g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);

    Point gradientOrigin = new Point(center.x - radius,
                                     center.y - radius);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (2 * radius / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    g2d.fillOval((int)(center.x - (radius * 0.90)), 
                 (int)(center.y - (radius * 0.95)), 
                 (int)(2 * radius * 0.9), 
                 (int)(2 * radius * 0.95));
  }
}
