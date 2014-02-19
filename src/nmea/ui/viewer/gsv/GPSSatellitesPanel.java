package nmea.ui.viewer.gsv;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

import java.awt.Stroke;

import java.awt.font.TextAttribute;

import java.text.AttributedString;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;

import nmea.server.ctx.NMEADataCache;

import nmea.server.utils.ChartColor;

import ocss.nmea.parser.SVData;

public class GPSSatellitesPanel
  extends javax.swing.JPanel
{
  private final static DecimalFormat DF2 = new DecimalFormat("00");
  
  /** Creates new form GPSSatellitesPanel */
  public GPSSatellitesPanel()
  {
    jbInit();
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener()
    {
      public void manageNMEAString(String str)
      {
//      System.out.println("NMEA:" + str);
        if (str.trim().length() > 6 && str.startsWith("$"))
        {
          if (str.substring(3, 6).equals("GSV"))
          {
            try
            {
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
  }

  private void jbInit()
  {
    this.setLayout(null);
    this.setBackground(Color.white);
  }
  
  public void paintComponent(Graphics gr)
  {    
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);  
    int width  = this.getWidth();
    int height = this.getHeight();
    int radius = (int)(0.9 * Math.min(width, height) / 2);
    Point center = new Point(width / 2, height / 2);
//  gr.setColor(Color.black);
//  gr.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);
    Graphics2D g2d = (Graphics2D)gr;
    if (true) // With shaded bevel
    {
      RadialGradientPaint rgp = new RadialGradientPaint(center, 
                                                        (int)(radius * 1.15), 
                                                        new float[] {0f, 0.9f, 1f}, 
                                                        new Color[] {this.getBackground(), Color.gray, this.getBackground()});
      g2d.setPaint(rgp);
      g2d.fillRect(0, 0, width, height);
    }
    drawGlossyCircularDisplay((Graphics2D)gr, center, radius, Color.lightGray, Color.black, 1f);
    // Grid
    gr.setColor(Color.gray);
    Stroke origin = g2d.getStroke();
    Stroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5 }, 0);
    g2d.setStroke(dotted);
    g2d.drawLine(center.x, center.y - radius, center.x, center.y + radius); // N-S
    g2d.drawLine(center.x - radius, center.y, center.x + radius, center.y); // E-W
    g2d.drawLine(center.x - (int)((Math.cos(Math.PI / 4d) * radius)), // NW-SE
                 center.y - (int)((Math.sin(Math.PI / 4d) * radius)), 
                 center.x + (int)((Math.cos(Math.PI / 4d) * radius)), 
                 center.y + (int)((Math.sin(Math.PI / 4d) * radius))); 
    g2d.drawLine(center.x + (int)((Math.cos(Math.PI / 4d) * radius)), // NE-SW
                 center.y - (int)((Math.sin(Math.PI / 4d) * radius)), 
                 center.x - (int)((Math.cos(Math.PI / 4d) * radius)), 
                 center.y + (int)((Math.sin(Math.PI / 4d) * radius))); 
    g2d.drawOval(center.x - (int)(radius * (1d/3d)), 
                 center.y - (int)(radius * (1d/3d)), 
                 2 * (int)(radius * (1d/3d)), 
                 2 * (int)(radius * (1d/3d)));
    g2d.drawOval(center.x - (int)(radius * (2d/3d)), 
                 center.y - (int)(radius * (2d/3d)), 
                 2 * (int)(radius * (2d/3d)), 
                 2 * (int)(radius * (2d/3d)));    
    g2d.setStroke(origin);
    Font origFont = g2d.getFont();
    g2d.setFont(origFont.deriveFont(Font.BOLD, 40f));
    String north = "N";
    int strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(north);
    g2d.drawString(north, center.x - (strWidth / 2), center.y - radius + g2d.getFont().getSize());
    g2d.setFont(origFont);
    try
    {
      if (NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW) != null)
      {
        Map<Integer, SVData> hm = ( Map<Integer, SVData>)NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW);
        
        gr.setColor(Color.red);
        Font f = gr.getFont();
        gr.setFont(f.deriveFont(Font.BOLD));
        for (Integer sn : hm.keySet())
        {
          SVData svd = hm.get(sn);
          int sID = svd.getSvID();
          int elev = svd.getElevation();
          int z = svd.getAzimuth();
          int snr = svd.getSnr();
          int dz = (90 - elev);
          int x = center.x + (int)((dz / 90d) * radius * Math.sin(Math.toRadians(z)));
          int y = center.y - (int)((dz / 90d) * radius * Math.cos(Math.toRadians(z)));
          // Get the color from SNR
          gr.setColor(getSNRColor(snr));
          gr.fillOval(x - 10, y - 10, 20, 20);
          gr.drawString(Integer.toString(sID), x + 12, y + 12);
        }
        gr.setFont(f);
      }
    }
    catch (Exception ex)
    {
      // No cache yet
    }
  } 

  private static void drawGlossyCircularDisplay(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency)
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
  
  private static Color getSNRColor(int snr)
  {
    Color c = Color.lightGray;
    if (snr > 0)
      c = Color.red;
    if (snr > 10)
      c = Color.orange;
    if (snr > 20)
      c = Color.yellow;
    if (snr > 30)
      c = ChartColor.LIGHT_GREEN;
    if (snr > 40)
      c = Color.green;
    return c;
  }
}
