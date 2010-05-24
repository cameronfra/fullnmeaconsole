package nmea.ui.viewer.elements;

import java.awt.AlphaComposite;

import nmea.ctx.NMEAContext;

import nmea.ctx.NMEADataCache;

import nmea.ctx.Utils;

import nmea.event.NMEAListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import java.text.DecimalFormat;

import javax.swing.JPanel;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Speed;

import ocss.nmea.parser.Temperature;

/**
 * All computing done in this class
 */
public class DrawingBoard
  extends JPanel
{
  private boolean debug = System.getProperty("debug", "off").equals("on");
  
  private DecimalFormat df3 = new DecimalFormat("###");
  private DecimalFormat df22 = new DecimalFormat("##0.00");
  private DecimalFormat df21 = new DecimalFormat("##0.0");

  private int boatPosX = 0, boatPosY = 0;

  private double aws = 10f, awa = -30f;

  private double bsp = 8D;
  private double hdg = 53D;

  private double sog = 7D;
  private double cog = 94D;

  private double bspCoeff  = 1D;
  private double hdgOffset = 0D;
  private double awsCoeff  = 1D;
  private double awaOffset = 0D;
  private double maxLeeway = 10d;
  
  private double leeway = 0d;

  private double tws = 0, twa = 0, twd = 0;
  private double wt  = -Double.MAX_VALUE;
  
  private double windScale       = 3d; // 10 knots
  private boolean displayCurrent = true;
  private boolean freeze         = false;
  private boolean showTemperature = false;
  
  public DrawingBoard()
  {
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
    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try { setHDG(((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue()); } catch (Exception ex) {}
            try { setBSP(((Speed)cache.get(NMEADataCache.BSP)).getValue()); } catch (Exception ex) {}
            try { setSOG(((Speed)cache.get(NMEADataCache.SOG)).getValue()); } catch (Exception ex) {}
            try { setCOG(((Angle360)cache.get(NMEADataCache.COG)).getValue()); } catch (Exception ex) {}
            try { setAWA(((Angle180)cache.get(NMEADataCache.AWA)).getValue()); } catch (Exception ex) {}
            try { setAWS(((Speed)cache.get(NMEADataCache.AWS)).getValue()); } catch (Exception ex) {}

            try { setBSPCoeff(((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue()); } catch (Exception ex) {}
            try { setHDGOffset(((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue()); } catch (Exception ex) {}
            try { setAWSCoeff(((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue()); } catch (Exception ex) {}
            try { setAWAOffset(((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue()); } catch (Exception ex) {}
            try { setMaxLeeway(((Double)cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue()); } catch (Exception ex) {}
            try { setWaterTemperature(((Temperature)cache.get(NMEADataCache.WATER_TEMP)).getValue()); } catch (Exception ex) {}
            
            repaint();
          }
        }
      });
    this.setLayout(null);
    this.setBackground(Color.white);
  }

  public void paintComponent(Graphics gr)
  {    
    // The boat position, in the middle
    boatPosX = this.getWidth() / 2;
    boatPosY = this.getHeight() / 2;

    GradientPaint gradient = new GradientPaint(0, 0, 
                                               Color.white, 
                                               this.getWidth(), this.getHeight(), 
                                               Color.lightGray);
    ((Graphics2D)gr).setPaint(gradient);

//  gr.setColor(Color.white);  // Aha!
    
    gr.fillRect(0, 0, this.getWidth(), this.getHeight());

    double bspLengthAt10 = 1d * this.getWidth() / windScale; // 3d;
    
    // Draw speed circles
    for (int w=1; w<=60; w++)
    {
      double radius = (w * (bspLengthAt10 / 10D));
      if (w % 5 == 0)
        gr.setColor(Color.gray);
      else
        gr.setColor(Color.lightGray);
        
      gr.drawOval((int)(boatPosX - radius),
                  (int)(boatPosY - radius),
                  (int)(2 * radius),
                  (int)(2 * radius));
    }
    // Draw axis
    gr.setColor(Color.lightGray);
    gr.drawLine(0, boatPosY, this.getWidth(), boatPosY);    
    gr.drawLine(boatPosX, 0, boatPosX, this.getHeight());    
    
    // CMG - Route Vraie
    double rvX = boatPosX + ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset)));
    double rvY = boatPosY - ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset)));

    // Thickness
    Stroke stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    ((Graphics2D) gr).setStroke(stroke);

    /*
     * Boat Speed and Heading
     */    
    gr.setColor(Color.red);
    if (debug || bsp > 0)
      Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rvX, (int) rvY), Color.red);
    gr.drawString("BSP:" + df22.format(bsp * bspCoeff) + " kts, HDG:" + df3.format((hdg + hdgOffset)) + "\272", (int) rvX + 5,
                  (int) rvY + 5);
    /*
     * Leeway
     */
    if (freeze)
      leeway = Utils.getLeeway(awa + awaOffset, maxLeeway);
    else
      leeway = ((Angle180LR) NMEAContext.getInstance().getCache().get(NMEADataCache.LEEWAY)).getValue();
    
    double rsX = boatPosX + ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset + leeway)));
    double rsY = boatPosY - ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset + leeway)));
    if (debug || (Math.abs(leeway) > 0.1 && bsp > 0))
    {
      gr.setColor(Color.cyan);
      Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rsX, (int) rsY), Color.pink);
      gr.drawString("LWY:" + df22.format(leeway) + "\272", (int) rsX + 5, (int) rsY + 5);
    }
    /*
     * SOG, COG
     * Route fond
     */
    double rfX = boatPosX + (sog * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(cog)));
    double rfY = boatPosY - (sog * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(cog)));
    if (debug || sog > 0)
    {
      gr.setColor(Color.magenta);
      Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rfX, (int) rfY), Color.magenta);
      gr.drawString("SOG:" + df22.format(sog) + " kts, COG:" + df3.format(cog) + "\272", (int) rfX + 5, (int) rfY + 5);
    }
    /*
     * AWA, AWS
     */
    double awX = boatPosX + ((aws * awsCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset + (awa + awaOffset))));
    double awY = boatPosY - ((aws * awsCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset + (awa + awaOffset))));
    if (debug || (sog > 0 && bsp > 0))
    {
      gr.setColor(Color.blue);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point(boatPosX, boatPosY), Color.blue);
      gr.drawString("AWS:" + df22.format(aws * awsCoeff) + " kts, AWA:" + df3.format((awa + awaOffset)) + "\272", (int) awX + 5, (int) awY + 5);
    }
    /*
     * TWA, TWS
     */
    double vvX = awX + ((sog) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(180 + cog)));
    double vvY = awY - ((sog) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(180 + cog)));
    // Vent vitesse
    if (false && (debug || (bsp > 0 && cog > 0)))
    {
      gr.setColor(Color.pink);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point((int) vvX, (int) vvY), Color.pink);
    }
    // Calculate TW with GPS
    if (freeze)
    {
      double[] twData = Utils.calculateTWwithGPS(aws, awsCoeff, awa, awaOffset, hdg, hdgOffset, sog, cog);
      twa = twData[0];
      tws = twData[1];
      twd = twData[2];
    }
    else
    {
      twa = ((Angle180) NMEAContext.getInstance().getCache().get(NMEADataCache.TWA)).getValue();      
      tws = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.TWS)).getValue();      
      twd = ((Angle360) NMEAContext.getInstance().getCache().get(NMEADataCache.TWD)).getValue();      
    }
    double twX = boatPosX + (tws * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(twd)));
    double twY = boatPosY - (tws * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(twd)));
    
    // Vent vitesse
    if (true && (debug || (bsp > 0 && cog > 0)))
    {
      gr.setColor(Color.pink);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point((int) twX, (int) twY), Color.pink);
    }
    
    gr.setColor(Color.black);
    Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) twX, (int) twY), new Point(boatPosX, boatPosY), Color.black);
    gr.drawString("TWS:" + df22.format(tws) + " kts, TWA:" + df3.format(twa) + "\272", (int) twX + 5, (int) twY + 5);

    /*
     * Current
     */
    double a = 10 * (rsX - rfX) / bspLengthAt10;
    double b = 10 * (rfY - rsY) / bspLengthAt10;
    double csp = Math.sqrt((a * a) + (b * b));
    double cdr = Utils.getDir((float) a, (float) b);
    if (debug || (displayCurrent && csp > 0.2))
    {
      gr.setColor(Color.green);
      Utils.drawArrow((Graphics2D) gr, new Point((int) rsX, (int) rsY), new Point((int) rfX, (int) rfY), Color.green);
    }
    /*
     * Boat itself
     * 
     * TODO An image (mono, cata, tri)
     */
    stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    ((Graphics2D) gr).setStroke(stroke);
    Utils.drawBoat((Graphics2D) gr, 
                   Color.blue, 
                   Color.gray,
                   Color.black,
                   new Point(boatPosX, boatPosY), 
                   50, 
                   (int) (hdg + hdgOffset),
                   0.75f);

    // Display All Data Values:
    gr.setFont(new Font("Courier New", Font.PLAIN, 12)); // TODO 12 as a parameter
    gr.setColor(new Color(0, 142, 0));
    int y = 12;
    gr.drawString("BSP (corrected) :" + df22.format(bsp * bspCoeff) + " kts", 10, y);
    y += 12;
    Angle180EW dec = (Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DECLINATION);
    if (dec.getValue() == -Double.MAX_VALUE)
      dec = (Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION);
    gr.drawString("HDG (corrected) :" + df3.format(hdg + hdgOffset) + "\272 (D=" + 
                  dec.toFormattedString() +
                  ", d=" + 
                  ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION)).toFormattedString() +
                  ")", 10, y);
    y += 12;
    gr.drawString("AWS (corrected) :" + df22.format(aws * awsCoeff) + " kts", 10, y);
    y += 12;
    gr.drawString("AWA (corrected) :" + df3.format(awa + awaOffset) + "\272", 10, y);
    gr.setColor(Color.red);
    y += 12;
    gr.drawString("BSP Coeff       :" + df22.format(bspCoeff), 10, y);
    y += 12;
    gr.drawString("HDG Offset      :" + df3.format(hdgOffset) + "\272", 10, y);
    y += 12;
    gr.drawString("AWS Coeff       :" + df22.format(awsCoeff), 10, y);
    y += 12;
    gr.drawString("AWA Offset      :" + df3.format(awaOffset) + "\272", 10, y);
    gr.setColor(Color.blue);
    y += 12;
    gr.drawString("TWS             :" + df22.format(tws) + " kts", 10, y);
    y += 12;
    gr.drawString("TWA             :" + df3.format(twa) + "\272", 10, y);
    y += 12;
    gr.drawString("TWD             :" + df3.format(twd) + "\272", 10, y);
    y += 12;
    gr.drawString("CDR             :" + df3.format(cdr) + "\272", 10, y);
    y += 12;
    gr.drawString("CSP             :" + df22.format(csp) + " kts", 10, y);
    
    if (showTemperature)
    {
      gr.setColor(Color.black);
      int midThermometer = this.getWidth() - (6 * tubeWidth);
      if (wt != -Double.MAX_VALUE)
      {
        stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
        int fontSize = gr.getFont().getSize();
        // Graduation 
        for (int i=bottomTemperature; i<=topTemperature; i+=5)
        {
          int _y = getTemperatureYValue(i);
          gr.drawLine(midThermometer - tubeWidth, 
                     _y, 
                     midThermometer + tubeWidth, 
                     _y);
          gr.drawString(Integer.toString(i) + "\272C", (int)(midThermometer + (1.5 * tubeWidth)), _y + (fontSize / 2));
        }
        // Tube
        int tubeTop    = topTemperature + 2;
        int tubeBottom = bottomTemperature - 2;
        gr.drawLine(midThermometer - (tubeWidth / 2),
                   getTemperatureYValue(tubeTop),
                   midThermometer - (tubeWidth / 2),
                   getTemperatureYValue(tubeBottom));
        gr.drawLine(midThermometer + (tubeWidth / 2),
                   getTemperatureYValue(tubeTop),
                   midThermometer + (tubeWidth / 2),
                   getTemperatureYValue(tubeBottom));
        gr.drawArc(midThermometer - (tubeWidth / 2),
                  getTemperatureYValue(tubeTop) - (tubeWidth / 2),
                  tubeWidth, 
                  tubeWidth, 
                  180, 
                  -180);
        gr.drawOval(midThermometer - tubeWidth, 
                   getTemperatureYValue(tubeBottom),
                   tubeWidth * 2,
                   tubeWidth * 2);
        ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        gr.setColor(Color.red);
        gr.fillOval(midThermometer - tubeWidth, 
                   getTemperatureYValue(tubeBottom),
                   tubeWidth * 2,
                   tubeWidth * 2);
        // Value
        gr.fillRect(midThermometer - (tubeWidth / 2),
                   getTemperatureYValue(wt),
                   tubeWidth,
                   getTemperatureYValue(tubeBottom - 1) - getTemperatureYValue(wt));
        ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        String value = df21.format(wt) + "\272C";
        Font f = gr.getFont();
        gr.setFont(f.deriveFont(Font.BOLD));
        int l = gr.getFontMetrics(gr.getFont()).stringWidth(value);
        gr.drawString(value, midThermometer - (l / 2), this.getHeight() - 10);
        gr.setFont(f);
      }
    }
  }

  private final static int tubeWidth         = 10;
  private final static int bottomTemperature = -5;
  private final static int topTemperature    = 30;
  private final static int margin            = 60;

  private int getTemperatureYValue(double d)
  {
    int y = 0;
    int height = this.getHeight() - (2 * margin);
    int ampl = topTemperature - bottomTemperature;
    double absY = (double)(d - bottomTemperature) / (double)ampl;
    absY = absY * (double)height;
    y = margin + (height - (int)absY);
    
    return y;
  }

  private void setBSP(double bsp)
  {
    if (!freeze)
    {
      this.bsp = bsp;
//    this.repaint();
    }
  }

  private void setHDG(double hdg)
  {
    if (!freeze)
    {
      this.hdg = hdg;
//    this.repaint();
    }
  }

  private void setSOG(double sog)
  {
    if (!freeze)
    {
      this.sog = sog;
//    this.repaint();
    }
  }

  private void setCOG(double hdg)
  {
    if (!freeze)
    {
      this.cog = hdg;
//    this.repaint();
    }
  }

  private void setAWS(double aws)
  {
    if (!freeze)
    {
      this.aws = aws;
//    this.repaint();
    }
  }

  private void setAWA(double awa)
  {
    if (!freeze)
    {    
      this.awa = awa;
//    this.repaint();
    }
  }

  private void setBSPCoeff(double d)
  {
    this.bspCoeff = d;
//  this.repaint();
  }

  private void setHDGOffset(double d)
  {
    this.hdgOffset = d;
//  this.repaint();
  }

  private void setAWSCoeff(double d)
  {
    this.awsCoeff = d;
//  this.repaint();
  }

  private void setAWAOffset(double d)
  {
    this.awaOffset = d;
//  this.repaint();
  }

  private void setMaxLeeway(double d)
  {
    this.maxLeeway = d;
//  this.repaint();
  }

  private void setWaterTemperature(double t)
  {
    this.wt = t;
  }
  
  public void setWindScale(double windScale)
  {
    this.windScale = windScale;
//  repaint();
  }

  public void setDisplayCurrent(boolean displayCurrent)
  {
    this.displayCurrent = displayCurrent;
//  repaint();
  }

  public boolean isDisplayCurrent()
  {
    return displayCurrent;  
  }

  public void setFreeze(boolean freeze)
  {
    this.freeze = freeze;
  }

  public void setShowTemperature(boolean showTemperature)
  {
    this.showTemperature = showTemperature;
  }
}
