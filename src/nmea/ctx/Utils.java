package nmea.ctx;

import nmea.ui.widgets.DeclinationPanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.PrintWriter;

import java.net.InetAddress;

import java.net.URL;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javax.swing.JOptionPane;

import nmea.ui.viewer.minimaxi.wind.WindSpeed;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.Distance;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.OverGround;
import ocss.nmea.parser.RMB;
import ocss.nmea.parser.RMC;

import ocss.nmea.parser.Speed;
import ocss.nmea.parser.StringParsers;

import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.parser.UTCTime;
import ocss.nmea.parser.Wind;

import oracle.xml.parser.v2.NSResolver;

public class Utils
{
  public final static String PROPERTIES_FILE = "nmea-config.properties";
  public final static String USER_CONFIG     = "user-nmea-config.xml";
  public final static String LOGISAIL_NS     = "urn:logisail-nmea";
  public final static String PARAMETER_FILE  = "nmea-prms.properties";
    
  public final static NSResolver LgSlResolver = new NSResolver()
    {
      public String resolveNamespacePrefix(String string)
      {
        return LOGISAIL_NS;
      }
    };
  
  public final static void displayHelpAbout(Component parent)
  {
    String helpContent = "NMEA Console.\n";
    helpContent += "Displays NMEA Data read from some stream (Serial Port, TCP Port, Simualtion Data File)\n";
    helpContent += "Can log NMEA Data.\n";
    helpContent += "Can rebroadcast NMEA data to an HTTP port in XML format.\n";
    String hostname = "localhost";
    String ip       = "127.0.0.1";
    try 
    { 
      InetAddress addr = InetAddress.getLocalHost(); // Get IP Address 
      byte[] ipAddr = addr.getAddress();             // Get hostname 
      ip = "";
      for (int i=0; i<ipAddr.length; i++) 
      { 
        if (i > 0)
          ip += "."; 
        ip += ipAddr[i]&0xFF;      
      }
      hostname = addr.getHostName(); 
    } 
    catch (UnknownHostException e) { }           
    
    helpContent += ("Default URL would be http://" + hostname + ":6666/ or http://" + ip + ":6666/\n");
    
    JOptionPane.showMessageDialog(parent, helpContent, "NMEA Console", JOptionPane.INFORMATION_MESSAGE);
  }
  
  private final static long SEC  = 1000L;
  private final static long MIN  = 60 * SEC;
  private final static long HOUR = 60 * MIN;
  private final static long DAY  = 24 * HOUR;
  
  public static String setRange(long sec)
  {
    String str = Long.toString(sec) + " ms = ";
    long nbSec = sec;
    long day = nbSec / DAY;
    nbSec = nbSec - (day * DAY);
    long hour = nbSec / HOUR;
    nbSec = nbSec - (hour * HOUR);
    long min  = nbSec / MIN;        
    nbSec = nbSec - (min * MIN);    
    long s    = nbSec / SEC;
    
    if (day > 0)
      str += (Long.toString(day) + " day(s) ");
    if (hour > 0)
      str += (Long.toString(hour) + " hour(s) ");
    if (min > 0)
      str += (Long.toString(min) + " min(s) ");
    if (s > 0)
      str += (Long.toString(s) + " sec(s) ");
    
    return str;
  }  

  public static boolean isInArray(String s, String[] sa)
  {
    boolean b = false;
    
    for (int i=0; sa != null && i<sa.length; i++)
    {
      if (s.equals(sa[i]))
      {
        b = true;
        break;
      }
    }
    return b;
  }
  
  public static void writeNMEAParameters()
  {
    Properties props = new Properties();
    props.put("deviation.file.name",  (String) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION_FILE));
    props.put("max.leeway",          ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.MAX_LEEWAY)).toString());
    props.put("hdg.offset",          ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).toString());
    props.put("awa.offset",          ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.AWA_OFFSET)).toString());
    props.put("bsp.coeff",           ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.BSP_FACTOR)).toString());
    props.put("aws.coeff",           ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.AWS_FACTOR)).toString());
    props.put("default.declination", Double.toString(((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue()));
    props.put("damping.value",       Integer.toString(((Integer) NMEAContext.getInstance().getCache().get(NMEADataCache.DAMPING)).intValue()));
    
    try
    {
      PrintWriter pw = new PrintWriter(new FileOutputStream(PARAMETER_FILE));
      props.store(pw, "NMEA Calibration Parameters");
      pw.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static void readNMEAParameters()
  {
    try
    {
      Properties props = new Properties();
      FileReader fr = new FileReader(PARAMETER_FILE);
      props.load(fr);
      fr.close();
      
      String dfName = props.getProperty("deviation.file.name", "zero-deviation.csv");
      NMEAContext.getInstance().getCache().put(NMEADataCache.DEVIATION_FILE, dfName);

      String lwStr  = props.getProperty("max.leeway", "10.0");
      NMEAContext.getInstance().getCache().put(NMEADataCache.MAX_LEEWAY, Double.parseDouble(lwStr));
      
      String hdgStr = props.getProperty("hdg.offset", "0");
      NMEAContext.getInstance().getCache().put(NMEADataCache.HDG_OFFSET, Double.parseDouble(hdgStr));

      String awaStr = props.getProperty("awa.offset", "0");
      NMEAContext.getInstance().getCache().put(NMEADataCache.AWA_OFFSET, Double.parseDouble(awaStr));

      String bspStr = props.getProperty("bsp.coeff", "1");
      NMEAContext.getInstance().getCache().put(NMEADataCache.BSP_FACTOR, Double.parseDouble(bspStr));

      String awsStr = props.getProperty("aws.coeff", "1");
      NMEAContext.getInstance().getCache().put(NMEADataCache.AWS_FACTOR, Double.parseDouble(awsStr));

      String ddStr = props.getProperty("default.declination", "0");
      NMEAContext.getInstance().getCache().put(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(Double.parseDouble(ddStr)));

      String damp = props.getProperty("damping.value", "1");
      NMEAContext.getInstance().getCache().put(NMEADataCache.DAMPING, Integer.parseInt(damp));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static void parseAndCalculate(String key, String value)
  {
    String sentenceId = key.substring(2);
    
    if ("RMC".equals(sentenceId))
    {
//    System.out.println("RMC'ing... [" + key + "] - [" + value + "]");
      RMC rmc = StringParsers.parseRMC(value);
      if (rmc != null)
      {
        HashMap<String, Object> rmcMap = new HashMap<String, Object>(5);
        rmcMap.put(NMEADataCache.SOG,         new Speed(rmc.getSog()));
        rmcMap.put(NMEADataCache.POSITION,    rmc.getGp());
        rmcMap.put(NMEADataCache.GPS_DATE_TIME, new UTCDate(rmc.getRmcDate()));
        rmcMap.put(NMEADataCache.COG,         new Angle360(rmc.getCog()));
        rmcMap.put(NMEADataCache.DECLINATION, new Angle180EW(rmc.getDeclination()));

        NMEAContext.getInstance().putDataCache(rmcMap);
      }
    }
    else if ("VHW".equals(sentenceId)) // Water Speed and Heading
    {
      double[] vhw = StringParsers.parseVHW(value);
      double bsp = vhw[StringParsers.BSP_in_VHW];
      double hdm = vhw[StringParsers.HDM_in_VHW]             ;
      NMEAContext.getInstance().putDataCache(NMEADataCache.BSP, new Speed(bsp));
      // Question for NMEA, HDG is TRUE when there is a Dec in HDG, or RMC
      double dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DECLINATION)).getValue();
      if (dec == -Double.MAX_VALUE)
        dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
      NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdm - dec));        
    }
    else if ("VLW".equals(sentenceId)) // Log
    {
      double[] d = StringParsers.parseVLW(value);
      HashMap<String, Object> map = new HashMap<String, Object>(2);
      map.put(NMEADataCache.LOG      , new Distance(d[0]));
      map.put(NMEADataCache.DAILY_LOG, new Distance(d[1]));

      NMEAContext.getInstance().putDataCache(map);
    }
    else if ("MTW".equals(sentenceId)) // Water Temperature
    {
      double t = StringParsers.parseMTW(value);
      NMEAContext.getInstance().putDataCache(NMEADataCache.WATER_TEMP, new Temperature(t));
    }
    else if ("MWV".equals(sentenceId)) // Apparent Wind Speed and Direction
    {
      Wind wind = StringParsers.parseMWV(value);
      if (wind != null)
      {
        HashMap<String, Object> map = new HashMap<String, Object>(2);
        map.put(NMEADataCache.AWS, new Speed(wind.speed));
        int awa = wind.angle;
        if (awa > 180)
          awa -= 360;
        map.put(NMEADataCache.AWA, new Angle180(awa));

        NMEAContext.getInstance().putDataCache(map);
      }
    }
    else if ("VWR".equals(sentenceId)) // Apparent Wind Speed and Direction (2)
    {
      Wind wind = StringParsers.parseVWR(value);
      if (wind != null)
      {
        HashMap<String, Object> map = new HashMap<String, Object>(2);
        map.put(NMEADataCache.AWS, new Speed(wind.speed));
        int awa = wind.angle;
        if (awa > 180)
          awa -= 360;
        map.put(NMEADataCache.AWA, new Angle180(awa));

        NMEAContext.getInstance().putDataCache(map);
      }
    }
    else if ("VTG".equals(sentenceId)) // Speed and Course over Ground
    {
      OverGround og = StringParsers.parseVTG(value);
      if (og != null)
      {
        HashMap<String, Object> map = new HashMap<String, Object>(2);
        map.put(NMEADataCache.COG, new Angle360(og.getCourse()));
        map.put(NMEADataCache.SOG, new Speed(og.getSpeed()));

        NMEAContext.getInstance().putDataCache(map);
      }
    }
    else if ("GLL".equals(sentenceId)) // Lat & Long
    {
      Object[] obj = StringParsers.parseGLL(value);
      if (obj != null)
      {
        GeoPos pos = (GeoPos)obj[StringParsers.GP_in_GLL];
        if (pos != null)
          NMEAContext.getInstance().putDataCache(NMEADataCache.POSITION, pos);
        Date date = (Date)obj[StringParsers.DATE_in_GLL];
        if (date != null)
          NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_TIME, new UTCTime(date));
      }
    }
    else if ("HDM".equals(sentenceId)) // Heading
    {
      int hdg = StringParsers.parseHDM(value);
      NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
    }
    else if ("HDG".equals(sentenceId)) // Heading
    {
      int hdg = (int)StringParsers.parseHDG(value)[StringParsers.HDG_in_HDG];
      double dev = StringParsers.parseHDG(value)[StringParsers.DEV_in_HDG];
      double var = StringParsers.parseHDG(value)[StringParsers.VAR_in_HDG];
      if (dev == -Double.MAX_VALUE && var == -Double.MAX_VALUE)
        NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
      else
      {
        double dec = 0d;
        if (dev != -Double.MAX_VALUE)
          dec = dev;
        else
          dec = var;
        NMEAContext.getInstance().putDataCache(NMEADataCache.DECLINATION, new Angle180EW(dec));
        NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg - dec));
      }
    }
    else if ("RMB".equals(sentenceId))
    {
      RMB rmb = StringParsers.parseRMB(value);
      if (rmb != null)
      {
        HashMap<String, Object> map = new HashMap<String, Object>(2);
        map.put(NMEADataCache.XTE,     new Distance(rmb.getXte()));
        map.put(NMEADataCache.WP_POS,  rmb.getDest());
        map.put(NMEADataCache.FROM_WP, rmb.getOwpid());
        map.put(NMEADataCache.TO_WP,   rmb.getDwpid());
        map.put(NMEADataCache.D2WP,    new Distance(rmb.getRtd()));
        map.put(NMEADataCache.B2WP,    new Angle360(rmb.getBtd()));
        map.put(NMEADataCache.S2WP,    new Speed(rmb.getDcv()));
        map.put(NMEADataCache.S2STEER, rmb.getDts());

        NMEAContext.getInstance().putDataCache(map);
      }
    }
    else if ("DBT".equals(sentenceId)) // Depth
    {
      float f = StringParsers.parseDBT(value, StringParsers.DEPTH_IN_METERS);
      NMEAContext.getInstance().putDataCache(NMEADataCache.DBT, new Depth(f));
    }
    
    computeAndSendValues(NMEAContext.getInstance().getCache());
    NMEAContext.getInstance().fireDataChanged();
  }

  /**
   * Calculated Data
   * 
   * TWS, TWA, TWD
   * HDG, true
   * CSP, CDR
   * Leeway
   */
  public static void computeAndSendValues(NMEADataCache cache)
  {
    double heading = 0d;
    double hdc = 0d;
    double dec = 0d;
    try { hdc = ((Angle360)cache.get(NMEADataCache.HDG_COMPASS)).getValue() + ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue(); } catch (Exception ex) {}
    try { dec = ((Angle180EW)cache.get(NMEADataCache.DECLINATION)).getValue(); } catch (Exception ex) {}
    if (dec == -Double.MAX_VALUE)
      dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue();

    heading = hdc + dec; // Magnetic
    cache.put(NMEADataCache.HDG_MAG, new Angle360(heading));
      
    double dev = Utils.getDeviation(heading);
    cache.put(NMEADataCache.DEVIATION, new Angle180EW(dev));
    
    double w = dec + dev;
    cache.put(NMEADataCache.VARIATION, new Angle180EW(w));    
    heading = hdc + w; // true    
    cache.put(NMEADataCache.HDG_TRUE, new Angle360(heading));

    double twa = 0d, 
           tws = 0d; 
    int twd = 0;
    
    double sog = 0d,
           cog = 0d,
           aws = 0d;
    int awa = 0;
    try { sog = ((Speed)cache.get(NMEADataCache.SOG)).getValue(); } catch (Exception ex) {}
    try { cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue(); } catch (Exception ex) {}
    try { aws = ((Speed)cache.get(NMEADataCache.AWS)).getValue() * ((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue(); } catch (Exception ex) {}
    try { awa = (int)(((Angle180)cache.get(NMEADataCache.AWA)).getValue() + ((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue()); } catch (Exception ex) {}
    
    double[] tw = calculateTWwithGPS(aws, 
                                     1.0,
                                     awa,
                                     0.0,
                                     heading,
                                     0.0,
                                     sog, 
                                     cog);
    twa = tw[0]; 
    tws = tw[1];    
    twd = (int)tw[2];
    cache.put(NMEADataCache.TWA, new Angle180(twa));
    cache.put(NMEADataCache.TWS, new Speed(tws));
    cache.put(NMEADataCache.TWD, new Angle360(twd));
    
    //  System.out.println("AWS:" + aws + ", TWS:" + tws + ", AWA:" + awa + ", TWA:" + twa);
    
    double bsp = 0d;
    double maxLeeway = 0d;
    try { maxLeeway = ((Double)cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue(); } catch (Exception ex) { System.out.println("MaxLeeway not available:" + ex.toString());}
    double leeway = getLeeway(awa, maxLeeway);
    cache.put(NMEADataCache.LEEWAY, new Angle180LR(leeway));
    double cmg = heading + leeway;
    cache.put(NMEADataCache.CMG, new Angle360(cmg));
    
    try { bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue() * ((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue(); } catch (Exception ex) {}
    double[] cr = calculateCurrent(bsp,
                                   1.0,
                                   heading,
                                   0.0,
                                   leeway,
                                   sog,
                                   cog);
    cache.put(NMEADataCache.CDR, new Angle360(cr[0]));
    cache.put(NMEADataCache.CSP, new Speed(cr[1]));    
  }

  public static double[] calculateTWwithGPS(double aws, double awsCoeff, 
                                            double awa, double awaOffset,
                                            double hdg, double hdgOffset,
                                            double sog,
                                            double cog)
  {
    double twa = 0d, tws = 0d, twd = 0d;
    try
    {
      // Warning, the MHU is carried by the boat, that has the HDG...
      // Only if the boat is moving (ie SOG > 0)
      double diffCogHdg = 0;
      if (sog > 0d)
      {
        diffCogHdg = (cog - (hdg + hdgOffset));
        if (diffCogHdg > 180)
          diffCogHdg -= 360;
      }
      double awaOnCOG = (awa + awaOffset) - diffCogHdg;      
      double d = ((aws * awsCoeff) * Math.cos(Math.toRadians(awaOnCOG))) - (sog);
      double h = ((aws * awsCoeff) * Math.sin(Math.toRadians(awaOnCOG)));
      tws = Math.sqrt((d*d) + (h*h));
      double twaOnCOG = Math.toDegrees(Math.acos(d/tws));
      if (Double.compare(Double.NaN, twaOnCOG) == 0)
        twaOnCOG = 0d;
      if (Math.abs(awaOnCOG) > 180 || awaOnCOG < 0)
        twaOnCOG = 360 - twaOnCOG;
      if (sog > 0)
        twd = (int)(cog) + (int)twaOnCOG;
      else
        twd = (int)(hdg) + (int)twaOnCOG;
      while (twd > 360) twd -= 360;
      while (twd < 0) twd += 360;
      
      twa = twaOnCOG + diffCogHdg;
      if (twa > 180)
        twa -= 360;
  //    System.out.println("DiffCOG-HDG:" + diffCogHdg + ", AWA on COG:" + awaOnCOG + ", TWAonCOG:" + twaOnCOG);
    }
    catch (Exception oops)
    {
      oops.printStackTrace();
    }
    return new double[] { twa, tws, twd };
  }

  public static double[] calculateCurrent(double bsp, double bspCoeff,
                                          double hdg, double hdgOffset,
                                          double leeway,
                                          double sog, double cog)
  {
    double cdr = 0d, csp = 0d;

  //  double rvX = ((bsp * bspCoeff) * Math.sin(Math.toRadians(hdg + hdgOffset)));
  //  double rvY = -((bsp * bspCoeff) * Math.cos(Math.toRadians(hdg + hdgOffset)));

    double rsX = ((bsp * bspCoeff) * Math.sin(Math.toRadians((hdg + hdgOffset) + leeway)));
    double rsY = -((bsp * bspCoeff) * Math.cos(Math.toRadians((hdg + hdgOffset) + leeway)));

    double rfX = (sog * Math.sin(Math.toRadians(cog)));
    double rfY = -(sog * Math.cos(Math.toRadians(cog)));
    double a = (rsX - rfX);
    double b = (rfY - rsY);
    csp = Math.sqrt((a * a) + (b * b));
    cdr = getDir((float) a, (float) b);
    
    return new double[] { cdr, csp };
  }
  
  public static double getDir(float x, float y)
  {
    double dir = 0.0D;
    if (y != 0)
      dir = Math.toDegrees(Math.atan((double) x / (double) y));
    if (x <= 0 || y <= 0)
    {
      if (x > 0 && y < 0)
        dir += 180D;
      else if (x < 0 && y > 0)
        dir += 360D;
      else if (x < 0 && y < 0)
        dir += 180D;
      else if (x == 0)
      {
        if (y > 0)
          dir = 0.0D;
        else
          dir = 180D;
      }
      else if (y == 0)
      {
        if (x > 0)
          dir = 90D;
        else
          dir = 270D;
      }
    }
    dir += 180D;
    while (dir >= 360D)
      dir -= 360D;
    return dir;
  }

  public static String lpad(String str, String with, int len)
  {
    while (str.length() < len)
      str = with + str;
    return str;
  }
  
  public static void drawBoat(Graphics2D g2, 
                              Color c,
                              Color start, 
                              Color end, 
                              Point pt, 
                              int boatLength, 
                              int trueHeading, 
                              float alpha)
  {
    GradientPaint gradient = new GradientPaint(pt.x - boatLength, 
                                               pt.y - boatLength, 
                                               start, 
                                               pt.x + boatLength, 
                                               pt.y + boatLength, 
                                               end); // vertical, upside down
    g2.setPaint(gradient);
    drawBoat(g2, c, pt, boatLength, trueHeading, alpha);
  }
  
  public static void drawBoat(Graphics2D g2, 
                              Color c, 
                              Point pt, 
                              int boatLength, 
                              int trueHeading, 
                              float alpha)
  {
    // Transparency
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Color before = g2.getColor();
//  g2.setColor(c);
    double[] x = // Half
      new double[] { 0, boatLength / 7, 
                    (2 * boatLength) / 7, 
                    (2 * boatLength) / 7, 
                    (1.5 * boatLength) / 7, 
                    -(1.5 * boatLength) / 7,
                    -(2 * boatLength) / 7, 
                    -(2 * boatLength) / 7, 
                    -boatLength / 7 };
    double[] y = // Half
      new double[] { -(4 * boatLength) / 7, 
                     -(3 * boatLength) / 7, 
                     -(boatLength) / 7, 
                     boatLength / 7, 
                     (3 * boatLength) / 7,
                     (3 * boatLength) / 7, 
                     boatLength / 7, 
                     -(boatLength) / 7, 
                     -(3 * boatLength) / 7 };
    int[] xpoints = new int[x.length];
    int[] ypoints = new int[y.length];

    // Rotation matrix:
    // | cos(alpha)  -sin(alpha) |
    // | sin(alpha)   cos(alpha) |
    for (int i = 0; i < x.length; i++)
    {
      double dx = x[i] * Math.cos(Math.toRadians(trueHeading)) + (y[i] * (-Math.sin(Math.toRadians(trueHeading))));
      double dy = x[i] * Math.sin(Math.toRadians(trueHeading)) + (y[i] * Math.cos(Math.toRadians(trueHeading)));
      xpoints[i] = (int) (pt.x + dx);
      ypoints[i] = (int) (pt.y + dy);
    }
    Polygon p = new Polygon(xpoints, ypoints, xpoints.length);
    g2.fillPolygon(p);
    
    // Reset Transparency
    alpha = 1.0f;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    // Line around the boat
    g2.setColor(c);
    for (int i = 0; i < x.length - 1; i++)
      g2.drawLine(xpoints[i], ypoints[i], xpoints[i + 1], ypoints[i + 1]);
    g2.drawLine(xpoints[x.length - 1], ypoints[x.length - 1], xpoints[0], ypoints[0]);
    g2.setColor(before);
  }
  
  public static void drawArrow(Graphics2D g, Point from, Point to, Color c)
  {
    Color orig = null;
    if (g != null) orig = g.getColor();
    int headLength = 30;
    double headHalfAngle = 15D;
    
    double dir = getDir((float)(from.x - to.x), (float)(to.y - from.y));
//  System.out.println("Dir:" + dir);
    
    Point left = new Point((int)(to.x - (headLength * Math.cos(Math.toRadians(dir - 90 + headHalfAngle)))),
                           (int)(to.y - (headLength * Math.sin(Math.toRadians(dir - 90 + headHalfAngle)))));
    Point right = new Point((int)(to.x - (headLength * Math.cos(Math.toRadians(dir - 90 - headHalfAngle)))),
                            (int)(to.y - (headLength * Math.sin(Math.toRadians(dir - 90 - headHalfAngle)))));
    
    g.drawLine(from.x, from.y, to.x, to.y);
    Polygon head = new Polygon(new int[] { to.x, left.x, right.x }, new int[] { to.y, left.y, right.y }, 3);
    g.fillPolygon(head);
    
    if (g != null) g.setColor(orig);
  }
  
  public static void drawAnemometerArrow(Graphics2D g, Point from, Point to, Color c)
  {
    Color orig = null;
    if (g != null) orig = g.getColor();
    int headLength = 30;
    double headHalfAngle = 15D;
    
    Point middlePoint = new Point((from.x + to.x) / 2, (from.y + to.y) / 2);
    
    double dir = getDir((float)(from.x - to.x), (float)(to.y - from.y));
  //  System.out.println("Dir:" + dir);
    
    Point left = new Point((int)(middlePoint.x - (headLength * Math.cos(Math.toRadians(dir - 90 + headHalfAngle)))),
                           (int)(middlePoint.y - (headLength * Math.sin(Math.toRadians(dir - 90 + headHalfAngle)))));
    Point right = new Point((int)(middlePoint.x - (headLength * Math.cos(Math.toRadians(dir - 90 - headHalfAngle)))),
                            (int)(middlePoint.y - (headLength * Math.sin(Math.toRadians(dir - 90 - headHalfAngle)))));
    
    g.drawLine(from.x, from.y, to.x, to.y);
    Polygon head = new Polygon(new int[] { middlePoint.x, left.x, right.x }, new int[] { middlePoint.y, left.y, right.y }, 3);
    g.fillPolygon(head);
    
    if (g != null) g.setColor(orig);
  }
  
  /**
   * @deprecated Use calculateTWwithGPS instead
   */
  @Deprecated
  public static double[] calculateTW(double aws, double awsCoeff, 
                                     double awa, double awaOffset,
                                     double bsp, double bspCoeff, 
                                     double hdg, double hdgOffset)
  {
    double twa = 0d, tws = 0d, twd = 0d;
    try
    {
      double d = ((aws * awsCoeff) * Math.cos(Math.toRadians(awa + awaOffset))) - (bsp * bspCoeff);
      double h = ((aws * awsCoeff) * Math.sin(Math.toRadians(awa + awaOffset)));
      tws = Math.sqrt((d*d) + (h*h));
      twa = Math.toDegrees(Math.acos(d/tws));
      if (Math.abs(awa) > 180 || awa < 0)
        twa = 360 - twa;
      twd = (int)(hdg + hdgOffset) + (int)twa;
      while (twd > 360) twd -= 360;
      while (twd < 0) twd += 360;
      
      if (twa > 180)
        twa -= 360;
    }
    catch (Exception oops)
    {
      oops.printStackTrace();
    }
    return new double[] { twa, tws, twd };
  }
  
  public static double getLeeway(double awa, double maxLeeway)
  {
    double _awa = awa;
    if (_awa < 0)
      _awa += 360;
    double leeway = 0D;
    if (_awa < 90 || _awa > 270)
    {
      double leewayAngle = maxLeeway * Math.cos(Math.toRadians(awa));
      if (_awa < 90)
        leewayAngle = -leewayAngle;
      leeway = leewayAngle;
    }
//  System.out.println("For AWA:" + awa + ", leeway:" + leeway);
    return leeway;
  }

  public static Hashtable<Double, Double> loadDeviationHashtable(InputStream is)
  {
    Hashtable<Double, Double> data = null;
    try
    {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      data = loadDeviationHashtable(br);
      br.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return data;
  }

  public static Hashtable<Double, Double> loadDeviationHashtable(String deviationFileName)
  {
    Hashtable<Double, Double> data = null;
    try
    {
      FileReader fr = new FileReader(deviationFileName);
      BufferedReader br = new BufferedReader(fr);
      data = loadDeviationHashtable(br);
      br.close();
      fr.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return data;
  }

  public static Hashtable<Double, Double> loadDeviationHashtable(BufferedReader br)
  {
    Hashtable<Double, Double> data = new Hashtable<Double, Double>();  
    
    try
    {
      String line = "";
      while ((line = br.readLine()) != null)
      {
        String[] sa = line.split(",");
        double cm = Double.parseDouble(sa[0]);
        double d  = Double.parseDouble(sa[1]);
        data.put(cm, d);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return data;
  }
  
  public static ArrayList<double[]> loadDeviationCurve(Hashtable<Double, Double> data)
  {
    ArrayList<double[]> ret = null;
    
    try
    {
      Set<Double> set = data.keySet();
      ArrayList<Double> list = new ArrayList<Double>(set.size());
      for (Double d: set)
        list.add(d);
      Collections.sort(list);

      ret = new ArrayList<double[]>(list.size());
      for (Double d : list)
      {
        double deviation = data.get(d);
        double cm        = d.doubleValue();
        ret.add(new double[] { cm, deviation });
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }
    
  public static ArrayList<double[]> loadDeviationCurve(String deviationFileName)
  {
    ArrayList<double[]> ret = null;
    try
    {
      Hashtable<Double, Double> data = loadDeviationHashtable(deviationFileName);
      ret = loadDeviationCurve(data);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }
  
  public static Hashtable<Double, Double> loadDeviationCurve(ArrayList<double[]> data)
  {
    Hashtable<Double, Double> ret = new Hashtable<Double, Double>(data.size());    
    try
    {
      for (double[] da : data)
      {
        ret.put(da[0], da[1]);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }

  public static double getDeviation(double cc)
  {
    return getDeviation(cc, NMEAContext.getInstance().getDeviation());
  }
  
  public static double getDeviation(double cc, ArrayList<double[]> deviationAL)
  {
    double deviation = 0d;
    if (deviationAL != null)
    {
      double prevCm = 0d, prevDev = 0;
      for (double[] dd : deviationAL)
      {
        if (dd[0] == cc)
        {
          deviation = dd[1];
          break;
        }
        else if (cc > prevCm && cc < dd[0])        
        {
          // Extrapolate
          double factor = (cc - prevCm) / (dd[0] - prevCm);
          deviation     = prevDev + ((dd[1] - prevDev) * factor);          
          break;
        }
        prevCm = dd[0];
        prevDev = dd[1];
      }
    }
//  System.out.println("d for " + cc + "=" + deviation);
    return deviation;
  }

  public static ArrayList<double[]> getDataForDeviation(String dataFileName)
  {
    ArrayList<double[]> ret = null;
    
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(dataFileName));
      /*
       * We need:
       * 
       * HDG (possible mag decl), HDM, or VHW for Heading
       * RMC for COG, SOG, TimeStamp, and Mag Decl.
       * GLL for TimeStamp
       * VTG for COG & SOG
       */ 
      HashMap<String, Integer> counter = new HashMap<String, Integer>(4);
      counter.put("HDG", 0);
      counter.put("HDM", 0);
      counter.put("VHW", 0);
      counter.put("RMC", 0);
      counter.put("GLL", 0);
      counter.put("VTG", 0);
      
      String line = "";
      boolean keepLooping = true;
      while (keepLooping)
      {
        line = br.readLine();
        if (line == null)
          keepLooping = false;
        else
        {
          if (line.startsWith("$") && line.length() > 7) // then let's try
          {            
            String key = line.substring(3, 6);
            if ("HDG".equals(key) || 
                "HDM".equals(key) || 
                "VHW".equals(key) || 
                "RMC".equals(key) || 
                "GLL".equals(key) || 
                "VTG".equals(key))
            counter.put(key, counter.get(key).intValue() + 1);
          }
        }
      }      
      br.close();
      System.out.println("We have:");
      Set<String> keys = counter.keySet();
      for (String k : keys)
        System.out.println(counter.get(k).intValue() + " " + k);
      if (counter.get("RMC").intValue() == 0 &&
          counter.get("GLL").intValue() == 0 &&
          counter.get("VTG").intValue() == 0)
        JOptionPane.showMessageDialog(null, "No RMC, GLL, or VTG!", "Logged Data", JOptionPane.ERROR_MESSAGE);
      else if (counter.get("HDG").intValue() == 0 &&
               counter.get("HDM").intValue() == 0 &&
               counter.get("VHW").intValue() == 0)
        JOptionPane.showMessageDialog(null, "No HDM, HDG, or VHW!", "Logged Data", JOptionPane.ERROR_MESSAGE);
      else // Proceed
      {
        System.out.println("Proceeding...");
        // Ideal: RMC + HDG
        if (counter.get("RMC").intValue() > 0 &&
            (counter.get("HDG").intValue() > 0 || counter.get("HDM").intValue() > 0))
        {
          System.out.println("RMC + HDG/HDM, Ideal.");
          ret = new ArrayList<double[]>(counter.get("RMC").intValue());
          // Is there a Declination?
          double decl = -Double.MAX_VALUE;
          double hdg = 0d; // (cc - D) when available
          double cog = -Double.MAX_VALUE;
          try
          {
            br = new BufferedReader(new FileReader(dataFileName));
            keepLooping = true;
            while (keepLooping)
            {
              line = br.readLine();
              if (line == null)
                keepLooping = false;
              else
              {
                if (line.startsWith("$") && line.length() > 7) // then let's try
                {            
                  String key = line.substring(3, 6);
                  if ("HDG".equals(key))
                  {
                    try
                    {
                      double[] val = StringParsers.parseHDG(line);
                      if (val[StringParsers.DEV_in_HDG] != -Double.MAX_VALUE ||
                          val[StringParsers.VAR_in_HDG] != -Double.MAX_VALUE)
                        decl = Math.max(val[StringParsers.DEV_in_HDG], val[StringParsers.VAR_in_HDG]);
                      hdg = val[StringParsers.HDG_in_HDG];
                      // Write data here
                      if (cog != -Double.MAX_VALUE)
                      {
                        ret.add(new double[] { hdg, cog });
                      }
                    }
                    catch (Exception ex) {}
                  }
                  else if ("HDM".equals(key) && counter.get("HDG").intValue() == 0)
                  {
                    try
                    {
                      double hdm = StringParsers.parseHDM(line);
                      if (decl != -Double.MAX_VALUE)
                        hdg = hdm + decl;
                      else
                        hdg = hdm;
                      // Write data here
                      if (cog != -Double.MAX_VALUE)
                      {
                        ret.add(new double[] { hdg, cog });
                      }
                    }
                    catch (Exception ex) {}
                  }
                  else if ("RMC".equals(key))
                  {
                    try
                    {
                      RMC rmc = StringParsers.parseRMC(line);
                      if (rmc.getDeclination() != -Double.MAX_VALUE)
                        decl = rmc.getDeclination();
                      cog = rmc.getCog();
                    }
                    catch (Exception ex) {}
                  }
                }
              }
            } 
            br.close();
            if (decl == -Double.MAX_VALUE)
            {
              System.out.println("No declination found.");
              DeclinationPanel dp = new DeclinationPanel();
              dp.setDeclinationValue(((Angle180EW) NMEAContext.getInstance().getDataCache(NMEADataCache.DEFAULT_DECLINATION)).getValue());
              int resp = JOptionPane.showConfirmDialog(null, dp, "Declination", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
              if (resp == JOptionPane.OK_OPTION)
              {
                double d = dp.getDeclinationValue();
                ArrayList<double[]> adjusted = new ArrayList<double[]>(ret.size());
                for (double[] da : ret)
                {
                  da[0] += d;
                  adjusted.add(da);
                }
                ret = adjusted;
              }
            }
            else
              System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        else if (counter.get("VTG").intValue() > 0 &&
                 counter.get("GLL").intValue() > 0 &&
                 (counter.get("HDM").intValue() > 0 || counter.get("HDG").intValue() > 0))
        {
          ret = new ArrayList<double[]>(counter.get("GLL").intValue());
          System.out.println("VTG, GLL, (HDG or HDM), good enough");
          // Is there a Declination?
          double decl = -Double.MAX_VALUE;
          double hdg = 0d; // (cc - D) when available
          double cog = -Double.MAX_VALUE;
          try
          {
            br = new BufferedReader(new FileReader(dataFileName));
            keepLooping = true;
            while (keepLooping)
            {
              line = br.readLine();
              if (line == null)
                keepLooping = false;
              else
              {
                if (line.startsWith("$") && line.length() > 7) // then let's try
                {            
                  String key = line.substring(3, 6);
                  if ("HDG".equals(key))
                  {
                    try
                    {
                      double[] val = StringParsers.parseHDG(line);
                      if (val[StringParsers.DEV_in_HDG] != -Double.MAX_VALUE ||
                          val[StringParsers.VAR_in_HDG] != -Double.MAX_VALUE)
                        decl = Math.max(val[StringParsers.DEV_in_HDG], val[StringParsers.VAR_in_HDG]);
                      hdg = val[StringParsers.HDG_in_HDG];
                    }
                    catch (Exception ex) {}
                  }
                  else if (counter.get("HDM").intValue() == 0 && "HDG".equals(key))
                  {
                    hdg = StringParsers.parseHDM(line);
                  }
                  else if ("GLL".equals(key))
                  {
                    // Just for the rythm. Write data here
                    if (cog != -Double.MAX_VALUE)
                    {
                      double delta = cog - hdg;
//                    System.out.println("HDG:" + hdg + "\272, W:" + delta + "\272");
                      ret.add(new double[] { hdg, cog });
                    }
                  }
                  else if ("VTG".equals(key))
                  {
                    OverGround og = StringParsers.parseVTG(line);
                    try { cog = og.getCourse(); } catch (Exception ex) {}
                    if (og == null)
                      System.out.println("Null for VTG [" + line + "]");
                  }
                }
              }
            } 
            br.close();
            if (decl == -Double.MAX_VALUE)
            {
              System.out.println("No declination found.");
              DeclinationPanel dp = new DeclinationPanel();
              dp.setDeclinationValue(((Angle180EW) NMEAContext.getInstance().getDataCache(NMEADataCache.DEFAULT_DECLINATION)).getValue());
              int resp = JOptionPane.showConfirmDialog(null, dp, "Declination", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
              if (resp == JOptionPane.OK_OPTION)
              {
                double d = dp.getDeclinationValue();
                ArrayList<double[]> adjusted = new ArrayList<double[]>(ret.size());
                for (double[] da : ret)
                {
                  da[0] += d;
                  adjusted.add(da);
                }
                ret = adjusted;
              }
            }
            else
              System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        else
        {
          System.out.println("Later...");
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }
  
  private static final int  EXTERNAL_BUFFER_SIZE = 128000;
  
  public static void play(final URL sound)
  {
    Thread player = new Thread()
      {
        public void run()
        {
          try
          {
            playSound(sound);            
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      };
    player.start();
  }
  
  public static int getBeaufort(double d)
  {
    int b = 0;
    for (int i=0; i<WindSpeed.BEAUFORT_SCALE.length; i++)
    {
      if (d < WindSpeed.BEAUFORT_SCALE[i])
      {
        b = i - 1;
        break;
      }
    }
    return b;
  }

  public static String getRoseSpeedAndDirection(double tws, double twd)
  {
    String windStr = "";
    int beaufort = getBeaufort(tws);
    windStr = "F " + Integer.toString(beaufort);
    windStr += (", " + getRoseDir(twd));
    return windStr;
  }
  
  public static String getRoseDir(double twd)
  {
    String rose = "";
    float delta = 11.25f; // Un quart
    
    String[] data = new String[] 
      {  
        "N", "N¼NE", "NNE", "NE¼N", "NE", "NE¼E", "ENE", "E¼NE",
        "E", "E¼SE", "ESE", "SE¼E", "SE", "SE¼S", "SSE", "S¼SE",
        "S", "S¼SW", "SSW", "SW¼S", "SW", "SW¼W", "WSW", "W¼SW",
        "W", "W¼NW", "WNW", "NW¼W", "NW", "NW¼N", "NNW", "N¼NW"
      };
    
    int index = 0;
    if (twd > (360 - (delta/2f)) || twd <= (delta/2f))
      index = 0;
    else
    {
      for (int i=0; i<data.length; i++)
      {
//      System.out.println("--- i=" + i + ", is [" + Double.toString((i + 0.5) * delta) + "<" + twd + "<=" + Double.toString((i + 1.5) * delta) + " ?");
        if (twd > ((i + 0.5) * delta) && twd <= ((i + 1.5) * delta))
        {
          index = i + 1;
          break;
        }
      }
    }    
    rose = data[index];    
    return rose;
  }

  public static void playSound(URL sound) throws Exception
  {
    boolean play = System.getProperty("play.sounds", "true").equals("true");
    if (play)
    {
      AudioInputStream  audioInputStream = null;
      try
      {
        audioInputStream = AudioSystem.getAudioInputStream(sound);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        System.exit(1);
      }
  
      AudioFormat audioFormat = audioInputStream.getFormat();
      SourceDataLine  line = null;
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      try
      {
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
      }
      catch (LineUnavailableException e)
      {
        e.printStackTrace();
        System.exit(1);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        System.exit(1);
      }
  
      line.start();
      int nBytesRead = 0;
      byte[]  abData = new byte[EXTERNAL_BUFFER_SIZE];
      while (nBytesRead != -1)
      {
        try
        {
          nBytesRead = audioInputStream.read(abData, 0, abData.length);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        if (nBytesRead >= 0)
        {
          int nBytesWritten = line.write(abData, 0, nBytesRead);
        }
      }
      line.drain();
      line.close();
    }
  }
  
  public static void main2(String[] args) throws Exception
  {
    BufferedReader br = new BufferedReader(new FileReader("nmea.data"));
    BufferedWriter bw = new BufferedWriter(new FileWriter("nmea.out"));
    String line = "";
    boolean go = true;
    while (go)
    {
      line = br.readLine();
      if (line == null)
        go = false;
      else
      {
        bw.write(line.substring(22) + "\n");
      }
    }
    br.close();
    bw.close();
  }
  
  public static void main(String[] args)
  {
    for (int i=0; i<360; i++)
      System.out.println(Integer.toString(i) + ":" + getRoseDir((double)i));
  }
}
