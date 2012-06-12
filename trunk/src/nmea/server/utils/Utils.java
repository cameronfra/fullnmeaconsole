package nmea.server.utils;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javax.swing.JOptionPane;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.ApparentWind;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.Distance;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.OverGround;
import ocss.nmea.parser.RMB;
import ocss.nmea.parser.RMC;

import ocss.nmea.parser.SolarDate;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.StringParsers;

import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.parser.UTC;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.parser.UTCTime;
import ocss.nmea.parser.Wind;

import ocss.nmea.utils.WindUtils;

import oracle.xml.parser.v2.NSResolver;

import user.util.GeomUtil;

public class Utils
{
  private final static SimpleDateFormat SDF = new SimpleDateFormat("EEE dd-MMM-yyyy HH:mm:ss");
  
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
    helpContent += "Displays NMEA Data read from some stream (Serial Port, TCP Port, UDP Port, RMI, Simualtion Data File)\n";
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
    
    props.put("display.temperature", System.getProperty("display.temperature", "false"));
    props.put("wind.scale",          System.getProperty("wind.scale", "-1"));
    
    try
    {
      PrintWriter pw = new PrintWriter(new FileOutputStream(PARAMETER_FILE));
      props.store(pw, " NMEA Calibration Parameters");
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
      
      System.setProperty("display.temperature", props.getProperty("display.temperature", "false"));
      System.setProperty("wind.scale",          props.getProperty("wind.scale", "-1"));
    }
    catch (Exception ex)
    {
      System.err.println(ex.getLocalizedMessage());
//    ex.printStackTrace();
    }
  }
  
  private static boolean rmcPresent = false;
  private static boolean hdtPresent = false;
  
  /**
   * Parse the NMEA String, calculate related data, and feed the cache
   * 
   * @param key NMEA Sentence ID
   * @param value NMEA Sentence
   * @param ndc The data cache
   */
  public static void parseAndCalculate(String key, String value, NMEADataCache ndc)
  {
    if (!StringParsers.validCheckSum(value))
    {
      System.out.println("Rejecting [" + value + "]");
      return;
    }
    // Put the last valid NMEA Value in the cache
    NMEAContext.getInstance().putDataCache(NMEADataCache.LAST_NMEA_SENTENCE, value);
    
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
        Date date = rmc.getRmcDate();
        if (date != null)
          rmcMap.put(NMEADataCache.GPS_DATE_TIME, new UTCDate(date));
        else
          rmcMap.put(NMEADataCache.GPS_DATE_TIME, null);

        Date time = rmc.getRmcTime();
        if (time != null)
          rmcMap.put(NMEADataCache.GPS_TIME, new UTCTime(time));
//      System.out.println("RMC:" + SDF.format(rmc.getRmcDate()));

        rmcMap.put(NMEADataCache.COG,         new Angle360(rmc.getCog()));
        rmcMap.put(NMEADataCache.DECLINATION, new Angle180EW(rmc.getDeclination()));
        
        // Compute Solar Time here
        try
        {
          if (rmc != null && (rmc.getRmcDate() != null || rmc.getRmcTime() != null) && rmc.getGp() != null)
          {
            long solarTime = -1L;
            if (rmc.getRmcDate() != null)
              solarTime = rmc.getRmcDate().getTime() + longitudeToTime(rmc.getGp().lng);        
            else
              solarTime = rmc.getRmcTime().getTime() + longitudeToTime(rmc.getGp().lng);
            Date solarDate = new Date(solarTime);
            rmcMap.put(NMEADataCache.GPS_SOLAR_TIME, new SolarDate(solarDate));
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }

        if (ndc == null)
          NMEAContext.getInstance().putDataCache(rmcMap);
        else
          ndc.putAll(rmcMap);
        rmcPresent = true;
      }
    }
    else if ("ZDA".equals(sentenceId))
    {
      UTC utc = StringParsers.parseZDA(value);
      if (utc != null)
      {
        NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_DATE_TIME, new UTCDate(utc.getDate()));
        NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_TIME, new UTCTime(utc.getDate()));
//      System.out.println("ZDA:" + SDF.format(utc.getDate()));
        
        GeoPos pos = (GeoPos)NMEAContext.getInstance().getDataCache(NMEADataCache.POSITION);
        if (pos != null)
        {
          long solarTime = utc.getDate().getTime() + longitudeToTime(pos.lng);        
          Date solarDate = new Date(solarTime);
          NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_SOLAR_TIME, new SolarDate(solarDate));
        }
      }
    }
    else if ("VHW".equals(sentenceId)) // Water Speed and Heading
    {
      double[] vhw = StringParsers.parseVHW(value);
      if (vhw == null)
        return;
      double bsp = vhw[StringParsers.BSP_in_VHW];
      double hdm = vhw[StringParsers.HDM_in_VHW];
      if (bsp != -Double.MAX_VALUE)
      {
        if (ndc == null)
          NMEAContext.getInstance().putDataCache(NMEADataCache.BSP, new Speed(bsp));
        else
          ndc.put(NMEADataCache.BSP, new Speed(bsp));
      }
      // QUESTION for NMEA, HDG is TRUE when there is a Dec in HDG, or RMC
      double dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DECLINATION)).getValue();
      if (dec == -Double.MAX_VALUE)
        dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdm /* - dec */));        
      else
        ndc.put(NMEADataCache.HDG_COMPASS, new Angle360(hdm /* - dec */));
    }
    else if ("VLW".equals(sentenceId)) // Log
    {
      double[] d = StringParsers.parseVLW(value);
      HashMap<String, Object> map = new HashMap<String, Object>(2);
      map.put(NMEADataCache.LOG      , new Distance(d[0]));
      map.put(NMEADataCache.DAILY_LOG, new Distance(d[1]));

      if (ndc == null)
        NMEAContext.getInstance().putDataCache(map);
      else
        ndc.putAll(map);
    }
    else if ("MTW".equals(sentenceId)) // Water Temperature
    {
      double t = StringParsers.parseMTW(value);
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.WATER_TEMP, new Temperature(t));
      else
        ndc.put(NMEADataCache.WATER_TEMP, new Temperature(t));
    }
    else if ("MWV".equals(sentenceId)) // Apparent Wind Speed and Direction
    {
      Wind wind = StringParsers.parseMWV(value);
      if (wind != null && wind instanceof ApparentWind) // TrueWind not used for now
      {
        HashMap<String, Object> map = new HashMap<String, Object>(2);
        map.put(NMEADataCache.AWS, new Speed(wind.speed));
        int awa = wind.angle;
        if (awa > 180)
          awa -= 360;
        map.put(NMEADataCache.AWA, new Angle180(awa));
 
        if (ndc == null)
          NMEAContext.getInstance().putDataCache(map);
        else
          ndc.putAll(map);
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

        if (ndc == null)
          NMEAContext.getInstance().putDataCache(map);
        else
          ndc.putAll(map);
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

        if (ndc == null)
          NMEAContext.getInstance().putDataCache(map);
        else
          ndc.putAll(map);
      }
    }
    else if ("GLL".equals(sentenceId) && !rmcPresent) // Lat & Long, UTC (No date)
    {
      Object[] obj = StringParsers.parseGLL(value);
      if (obj != null)
      {
        GeoPos pos = (GeoPos)obj[StringParsers.GP_in_GLL];
        if (pos != null)
        {
          if (ndc == null)
            NMEAContext.getInstance().putDataCache(NMEADataCache.POSITION, pos);
          else
            ndc.put(NMEADataCache.POSITION, pos);
        }
        Date date = (Date)obj[StringParsers.DATE_in_GLL];
        if (date != null)
        {
          NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_TIME, new UTCTime(date));
//        System.out.println("GLL:" + SDF.format(date));
          long solarTime = date.getTime() + longitudeToTime(pos.lng);        
          Date solarDate = new Date(solarTime);
          if (ndc == null)
            NMEAContext.getInstance().putDataCache(NMEADataCache.GPS_SOLAR_TIME, new SolarDate(solarDate));
          else
            ndc.put(NMEADataCache.GPS_SOLAR_TIME, new SolarDate(solarDate));
        }
      }
    }
    else if ("HDM".equals(sentenceId)) // Heading, magnetic
    {
      int hdg = StringParsers.parseHDM(value);
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
      else
        ndc.put(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
    }
    else if ("HDT".equals(sentenceId)) // Heading, true
    {
      int hdg = StringParsers.parseHDT(value);
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_TRUE, new Angle360(hdg));
      else
        ndc.put(NMEADataCache.HDG_TRUE, new Angle360(hdg));
      if (!hdtPresent)
        System.out.println("HDT is present.");
      hdtPresent = true;
    }
    else if ("HDG".equals(sentenceId)) // Heading
    {
      int hdg = (int)StringParsers.parseHDG(value)[StringParsers.HDG_in_HDG];
      double dev = StringParsers.parseHDG(value)[StringParsers.DEV_in_HDG];
      double var = StringParsers.parseHDG(value)[StringParsers.VAR_in_HDG];
      if (dev == -Double.MAX_VALUE && var == -Double.MAX_VALUE)
      {
        if (ndc == null)
          NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
        else
          ndc.put(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
      }
      else
      {
        double dec = 0d;
        if (dev != -Double.MAX_VALUE)
          dec = dev;
        else
          dec = var;
        if (ndc == null)
        {
          NMEAContext.getInstance().putDataCache(NMEADataCache.DECLINATION, new Angle180EW(dec));
          NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_COMPASS, new Angle360(hdg /* - dec */));
        }
        else
        {
          ndc.put(NMEADataCache.DECLINATION, new Angle180EW(dec));
          ndc.put(NMEADataCache.HDG_COMPASS, new Angle360(hdg /* - dec */));
        }
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

        if (ndc == null)
          NMEAContext.getInstance().putDataCache(map);
        else
          ndc.putAll(map);
      }
    }
    else if ("DBT".equals(sentenceId)) // Depth
    {
      float f = StringParsers.parseDBT(value, StringParsers.DEPTH_IN_METERS);
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.DBT, new Depth(f));
      else
        ndc.put(NMEADataCache.DBT, new Depth(f));
    }
    else if ("DPT".equals(sentenceId)) // Depth
    {
      float f = StringParsers.parseDPT(value, StringParsers.DEPTH_IN_METERS);
      if (ndc == null)
        NMEAContext.getInstance().putDataCache(NMEADataCache.DBT, new Depth(f));
      else
        ndc.put(NMEADataCache.DBT, new Depth(f));
    }
    if (ndc == null)
    {
      computeAndSendValuesToCache(NMEAContext.getInstance().getCache());
      NMEAContext.getInstance().fireDataChanged();
    }
    else
      computeAndSendValuesToCache(ndc, hdtPresent);
  }

  /**
   * Calculated Data
   * 
   * TWS, TWA, TWD
   * HDG, true
   * CSP, CDR
   * Leeway
   */
  public static void computeAndSendValuesToCache(NMEADataCache cache)
  {
    computeAndSendValuesToCache(cache, false);
  }
  public static void computeAndSendValuesToCache(NMEADataCache cache, boolean isHDTPresent)
  {
    double heading = 0d;
    if (!isHDTPresent)
    {
      double hdc = 0d;
      double dec = 0d;
  //  System.out.println("========================");
      try { hdc = ((Angle360)cache.get(NMEADataCache.HDG_COMPASS)).getValue() + ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue(); } catch (Exception ex) {}
  //  System.out.println("HDG Compass:" + hdc);
      try { dec = ((Angle180EW)cache.get(NMEADataCache.DECLINATION)).getValue(); } catch (Exception ex) {}
      if (dec == -Double.MAX_VALUE)
        dec = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
  //  System.out.println("Declination:" + dec);
      heading = hdc + dec; // Magnetic
      cache.put(NMEADataCache.HDG_MAG, new Angle360(heading));
  //  System.out.println("HDG Mag: " + heading);
      double dev = Utils.getDeviation(heading);
      cache.put(NMEADataCache.DEVIATION, new Angle180EW(dev));
      
      double w = dec + dev;
      cache.put(NMEADataCache.VARIATION, new Angle180EW(w));    
      heading = hdc + w; // true    
      cache.put(NMEADataCache.HDG_TRUE, new Angle360(heading));
  //  System.out.println("HDG True:" + heading);
  //  System.out.println("==========================");
    }
    else
      try { heading = ((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue() + ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue(); } catch (Exception ex) {}
      
    double twa = 0d, 
           tws = 0d; 
    int twd = 0;
    
    double sog = 0d,
           cog = 0d,
           aws = -1d;
    int awa = 0;
    try { sog = ((Speed)cache.get(NMEADataCache.SOG)).getValue(); } catch (Exception ex) {}
    try { cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue(); } catch (Exception ex) {}
    try { aws = ((Speed)cache.get(NMEADataCache.AWS)).getValue() * ((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue(); } catch (Exception ex) {}
    try { awa = (int)(((Angle180)cache.get(NMEADataCache.AWA)).getValue() + ((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue()); } catch (Exception ex) {}
        
    double awsCoeff  = 1d;
    try { awsCoeff = ((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue(); } catch (Exception ex) {}
    double awaOffset = 0d;
    try { awaOffset = ((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue(); } catch (Exception ex) {}
    double bspCoeff  = 0d;
    try { bspCoeff  = ((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue(); } catch (Exception ex) {}
    double hdgOffset = 0d;
    try { hdgOffset = ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue(); } catch (Exception ex) {}
    
    if ("true".equals(System.getProperty("use.gps.method", "true")))
    {
//    System.out.println("Using the GOOD method");
      double[] tw = calculateTWwithGPS(aws, 
                                       awsCoeff,
                                       awa,
                                       awaOffset,
                                       heading,
                                       hdgOffset,
                                       sog, 
                                       cog);
      twa = tw[0]; 
      tws = tw[1];    
      twd = (int)tw[2];
      // To display the other values
      if (false)
      {
        double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
        double[] tw_ = calculateTW(aws, awsCoeff, awa, awaOffset, bsp, bspCoeff, heading, hdgOffset);
        NumberFormat nf = new DecimalFormat("000");
        System.out.println("Good method:\tTWA:" +  nf.format(twa) + "\tTWS:" + nf.format(tws) + "\tTWD:" + (int)twd);
        System.out.println("Bad method:\tTWA:" +  nf.format(tw_[0]) + "\tTWS:" + nf.format(tw_[1]) + "\tTWD:" + (int)tw_[2]);
        System.out.println("================================================");
      }
    }
    else
    {
//    System.out.println("Using the baaaaad method");
      double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
      double[] tw = calculateTW(aws, awsCoeff, awa, awaOffset, bsp, bspCoeff, heading, hdgOffset);
      twa = tw[0]; 
      tws = tw[1];    
      twd = (int)tw[2];
    }
    
    cache.put(NMEADataCache.TWA, new Angle180(twa));
    cache.put(NMEADataCache.TWS, new TrueWindSpeed(tws));
    cache.put(NMEADataCache.TWD, new TrueWindDirection(twd));
    
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
    double twa = 0d, tws = -1d, twd = 0d;
    try
    {
      // Warning, the MHU is carried by the boat, that has the HDG...
      // Only if the boat is moving (ie SOG > 0)
      double diffCogHdg = 0;
      if (sog > 0d)
      {
        diffCogHdg = (cog - (hdg + hdgOffset));
        while (diffCogHdg < 0) diffCogHdg += 360;
        if (diffCogHdg > 180)
        {
//        System.out.println("- diffCogHdg > 180:" + Double.toString(diffCogHdg));
          diffCogHdg -= 360;
        }
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
  
  public static long longitudeToTime(double longitude)
  {
    long offset = (long)(longitude * 3600000L / 15L);
    return offset;
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
  
  private final static float WL_RATIO_COEFF = 0.75f; // Ratio to apply to (3.5 * Width / Length)

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
    
    double[] x = // Half, width
      new double[] { WL_RATIO_COEFF * 0, 
                     WL_RATIO_COEFF * boatLength / 7, 
                     WL_RATIO_COEFF * (2 * boatLength) / 7, 
                     WL_RATIO_COEFF * (2 * boatLength) / 7, 
                     WL_RATIO_COEFF * (1.5 * boatLength) / 7, 
                     WL_RATIO_COEFF * -(1.5 * boatLength) / 7,
                     WL_RATIO_COEFF * -(2 * boatLength) / 7, 
                     WL_RATIO_COEFF * -(2 * boatLength) / 7, 
                     WL_RATIO_COEFF * -boatLength / 7 };
    double[] y = // Half, length
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
  
  public static void drawHollowArrow(Graphics2D g, Point from, Point to, Color c)
  {
    Color orig = null;
    if (g != null) orig = g.getColor();
    int headLength = 30;
    int arrowWidth = 10; // Ondulation width
    int headWidth  = 20;
    
    double dir = getDir((float)(from.x - to.x), (float)(to.y - from.y));
    double len = Math.sqrt((double)((from.x - to.x) * (from.x - to.x)) + (double)((to.y - from.y) * (to.y - from.y)));
//    System.out.println("Dir:" + dir);
//    System.out.println("Len:" + len);
    
    Point one, two, three, four, five, six, seven, eight;
    one   = new Point(0, 0);
    two   = new Point(-arrowWidth / 2,
                      0);
    three = new Point(-arrowWidth / 2,
                      -((int)len - headLength));
    four  = new Point(-headWidth / 2,
                      -((int)len - headLength));
    five  = new Point(0, -(int)len); // to
    six   = new Point(headWidth / 2,
                      -((int)len - headLength));
    seven = new Point(arrowWidth / 2,
                      -((int)len - headLength));
    eight = new Point(arrowWidth / 2,
                      0);
    one   = rotate(one, -dir);
    two   = rotate(two, -dir);
    three = rotate(three, -dir);
    four  = rotate(four, -dir);
    five  = rotate(five, -dir);
    six   = rotate(six, -dir);
    seven = rotate(seven, -dir);
    eight = rotate(eight, -dir);    
    
    Polygon arrow = new Polygon(new int[] { from.x + one.x, 
                                            from.x + two.x, 
                                            from.x + three.x, 
                                            from.x + four.x, 
                                            from.x + five.x, 
                                            from.x + six.x, 
                                            from.x + seven.x, 
                                            from.x + eight.x }, 
                                new int[] { from.y + one.y, 
                                            from.y + two.y, 
                                            from.y + three.y, 
                                            from.y + four.y, 
                                            from.y + five.y, 
                                            from.y + six.y, 
                                            from.y + seven.y, 
                                            from.y + eight.y }, 
                                8);
    g.drawPolygon(arrow);
    
    if (g != null) g.setColor(orig);
  }

  public static void drawCurrentArrow(Graphics2D g, Point from, Point to, Color c)
  {
    Color orig = null;
    if (g != null) orig = g.getColor();
    int headLength = 30;
    int arrowWidth = 10;
    int headWidth  = 20;
    
    double dir = getDir((float)(from.x - to.x), (float)(to.y - from.y));
    double len = Math.sqrt((double)((from.x - to.x) * (from.x - to.x)) + (double)((to.y - from.y) * (to.y - from.y)));
//  System.out.println("Dir:" + dir);
//  System.out.println("Len:" + len);
    
    Point previous = null;
    for (int i=0; i<len; i++)
    {
      double phase = i % arrowWidth;
//    System.out.println("Phase=" + phase);
      double theta = phase * 2 * Math.PI / arrowWidth;
      double ampl = Math.cos(theta + (Math.PI / 2));
      Point p = new Point(i, (int)(ampl * arrowWidth / 2)); // Divide by 2 to have the radius
      p = rotate(p, 90-dir);
      if (previous != null)
        g.drawLine(previous.x, previous.y, 
                   from.x + p.x, from.y + p.y);
      previous = new Point(from.x + p.x, from.y + p.y);
    }
    // TODO Arrow head?
    if (g != null) g.setColor(orig);
  }

  private static Point rotate(Point p, double angle)
  {
    Point r = new Point((int)((p.x * Math.cos(Math.toRadians(angle))) + (p.y * Math.sin(Math.toRadians(angle)))), 
                        (int)((p.x * -Math.sin(Math.toRadians(angle))) + (p.y * Math.cos(Math.toRadians(angle)))));
    return r;
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
   * "Classical" way to do it, which is wrong.
   * But actually used by most of the NMEA Stations
   */
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
  
  public static List<double[]> loadDeviationCurve(Hashtable<Double, Double> data)
  {
    List<double[]> ret = null;
    
    try
    {
      Set<Double> set = data.keySet();
      List<Double> list = new ArrayList<Double>(set.size());
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
    
  public static List<double[]> loadDeviationCurve(String deviationFileName)
  {
    List<double[]> ret = null;
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
  
  public static Hashtable<Double, Double> loadDeviationCurve(List<double[]> data)
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
  
  public static double getDeviation(double cc, List<double[]> deviationAL)
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

  public static List<double[]> getDataForDeviation(String dataFileName)
  {
    List<double[]> ret = null;
    
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
        JOptionPane.showMessageDialog(null, "No HDM, HDG or VHW!", "Logged Data", JOptionPane.ERROR_MESSAGE);
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
                      if (decl != -Double.MAX_VALUE)
                        hdg += decl;
                      else
                        hdg += ((Angle180EW) NMEAContext.getInstance().getDataCache(NMEADataCache.DEFAULT_DECLINATION)).getValue();
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
                List<double[]> adjusted = new ArrayList<double[]>(ret.size());
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
                List<double[]> adjusted = new ArrayList<double[]>(ret.size());
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
  
  public static void playSound(URL sound) throws Exception
  {
    boolean play = System.getProperty("play.sounds", "false").equals("true");
    if (play)
    {
      AudioInputStream  audioInputStream = null;
      try
      {
        audioInputStream = AudioSystem.getAudioInputStream(sound);
      }
      catch (Exception e)
      {
        System.err.println(e.getLocalizedMessage());
//      e.printStackTrace();
//      System.exit(1);
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
  
  public static void dsiplayNMEADetails(String fName)
  {
    String message = ""; // The one to display
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(fName));
      String line = "";
      int nbRec = 0;
      double north = -90d, south = 90d;
      double east  = -180d, west = 180d;
      double bspMin = Double.MAX_VALUE, bspMax = 0d;
      double sogMin = Double.MAX_VALUE, sogMax = 0d;
      double twsMin = Double.MAX_VALUE, twsMax = 0d;
      double awsMin = Double.MAX_VALUE, awsMax = 0d;
      long  timeMin = Long.MAX_VALUE, 
            timeMax = Long.MIN_VALUE;

      NMEADataCache ndc = new NMEADataCache();
      
      double maxLeeway = 0d; // Max Leeway
      try 
      { 
        maxLeeway = ((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.MAX_LEEWAY)).doubleValue();
        ndc.put(NMEADataCache.MAX_LEEWAY, new Double(maxLeeway));

        ndc.put(NMEADataCache.AWS_FACTOR,  new Double(((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.AWS_FACTOR)).doubleValue()));
        ndc.put(NMEADataCache.AWA_OFFSET,  new Double(((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.AWA_OFFSET)).doubleValue()));
        ndc.put(NMEADataCache.BSP_FACTOR,  new Double(((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.BSP_FACTOR)).doubleValue()));
        ndc.put(NMEADataCache.HDG_OFFSET,  new Double(((Double) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_OFFSET)).doubleValue()));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      
      String anomalyMess = "";
      boolean keepReading = true;
      while (keepReading)
      {
        line = br.readLine();
        if (line == null)
          keepReading = false;
        else
        {
          nbRec++;
          // Analyze here
          if (line.startsWith("$") && line.length() > 6)
          {
            if (StringParsers.validCheckSum(line))
            {
              String key = line.substring(1, 6);
              try { Utils.parseAndCalculate(key, line, ndc); } 
              catch (Exception ex) { System.err.println("Oops (" + key + "):" + ex.toString()); }
              // Get values for statistics
              UTCDate utcDate = (UTCDate)ndc.get(NMEADataCache.GPS_DATE_TIME);
  
              if (utcDate != null && utcDate.getValue() != null)
              {
  //            System.out.println("displayNMEADetails:" + SDF.format(utcDate.getValue()) + " from [" + line + "]");
                long time = utcDate.getValue().getTime();                        
                long timeDiff = time - timeMax;
                if (Math.abs(timeDiff) > 50000)
                {
//                System.out.println("Record " + nbRec + ", Time diff:" + timeDiff);
                  anomalyMess = "Suspicious time gap at record " + nbRec + "\n" +
                                "From " + SDF.format(new Date(timeMax)) + " to " + SDF.format(new Date(time)) + "\n" +
                                "----------------------------------------------\n";
                }
                timeDiff = time - timeMin;
                if (Math.abs(timeDiff) > 50000)
                {
//                System.out.println("Record " + nbRec + ", Time diff:" + timeDiff);
                  anomalyMess = "Suspicious time gap at record " + nbRec + "\n" +
                                "From " + SDF.format(new Date(timeMin)) + " to " + SDF.format(new Date(time)) + "\n" +
                                "----------------------------------------------\n";
                }
                timeMax = Math.max(timeMax, time);
                timeMin = Math.min(timeMin, time);
                              
                if (false)
                {
                  GregorianCalendar gc = new GregorianCalendar();
                  gc.setTime(new Date(timeMax));
                  if (gc.get(Calendar.DAY_OF_MONTH) == 7)
                    System.out.println("Ooch");
                }
              }
              Speed bsp = (Speed)ndc.get(NMEADataCache.BSP);
              if (bsp != null)
              {
                bspMin = Math.min(bspMin, bsp.getValue());
                bspMax = Math.max(bspMax, bsp.getValue());
              }
              Speed sog = (Speed)ndc.get(NMEADataCache.SOG);
              if (sog != null)
              {
                sogMin = Math.min(sogMin, sog.getValue());
                sogMax = Math.max(sogMax, sog.getValue());
              }
              TrueWindSpeed tws = (TrueWindSpeed)ndc.get(NMEADataCache.TWS);
              if (tws != null)
              {
                twsMin = Math.min(twsMin, tws.getValue());
                twsMax = Math.max(twsMax, tws.getValue());
              }
              
              Speed aws = (Speed)ndc.get(NMEADataCache.AWS);
              if (aws != null)
              {
                awsMin = Math.min(awsMin, aws.getValue());
                awsMax = Math.max(awsMax, aws.getValue());
              }
              GeoPos pos = (GeoPos)ndc.get(NMEADataCache.POSITION);
              if (pos != null)
              {
                north = Math.max(north, pos.lat);
                south = Math.min(south, pos.lat);
                east  = Math.max(east, pos.lng);
                west  = Math.min(west, pos.lng);
              }
            }
          }
        }
      }
      br.close();
      message += (Integer.toString(nbRec) + " record(s).");
      message += ("\n" + SDF.format(new Date(timeMin)) + " to " + SDF.format(new Date(timeMax)));
      message += ("\n" + duration(timeMin, timeMax));
      if (anomalyMess.trim().length() > 0)
        message += ("\n" + anomalyMess);
      message += ("\nLatitude between " + GeomUtil.decToSex(north, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN) +
                  " and " + GeomUtil.decToSex(south, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN));
      message += ("\nLongitude between " + GeomUtil.decToSex(east, GeomUtil.SWING, GeomUtil.EW, GeomUtil.LEADING_SIGN) +
                  " and " + GeomUtil.decToSex(west, GeomUtil.SWING, GeomUtil.EW, GeomUtil.LEADING_SIGN));
      message += ("\nBSP between " + NMEAContext.DF22.format(bspMin) + " and " + NMEAContext.DF22.format(bspMax));
      message += ("\nSOG between " + NMEAContext.DF22.format(sogMin) + " and " + NMEAContext.DF22.format(sogMax));
      message += ("\nTWS between " + NMEAContext.DF22.format(twsMin) + " and " + NMEAContext.DF22.format(twsMax));
      message += ("\nAWS between " + NMEAContext.DF22.format(awsMin) + " and " + NMEAContext.DF22.format(awsMax));
    }
    catch (Exception ex)
    {
      message = ex.toString();
      ex.printStackTrace();
    }
    JOptionPane.showMessageDialog(null, message, "NMEA Details", JOptionPane.PLAIN_MESSAGE); // LOCALIZE
  }
  
  public final static long NB_MS_PER_SECOND = 1000L;
  public final static long NB_MS_PER_MINUTE = 60 * NB_MS_PER_SECOND;
  public final static long NB_MS_PER_HOUR   = 60 * NB_MS_PER_MINUTE;
  public final static long NB_MS_PER_DAY    = 24 * NB_MS_PER_HOUR;
  
  public static String duration(long from, long to)
  {
    String mess = "";
    boolean sentenceStarted = false;
    long diff = to - from;
    int nbDay = (int)((double)diff / (double)NB_MS_PER_DAY);
    diff -= (nbDay * NB_MS_PER_DAY);
    int nbHour = (int)((double)diff / (double)NB_MS_PER_HOUR);
    diff -= (nbHour * NB_MS_PER_HOUR);
    int nbMin = (int)((double)diff / (double)NB_MS_PER_MINUTE);
    diff -= (nbMin * NB_MS_PER_MINUTE);
    int nbSec = (int)((double)diff / (double)NB_MS_PER_SECOND);
    if (nbDay > 0)
    {
      mess += (nbDay + " day(s) ");
      sentenceStarted = true;
    }
    if (nbHour > 0 || sentenceStarted)
    {
      mess += (nbHour + " hour(s) ");
      sentenceStarted = true;
    }
    if (nbMin > 0 || sentenceStarted)
    {
      mess += (nbMin + " minute(s) ");
    }
    mess += (nbSec + " second(s).");
    return mess;
  }
  
  public static void main1(String[] args)
  {
    for (int i=0; i<360; i++)
      System.out.println(Integer.toString(i) + ":" + WindUtils.getRoseDir((double)i));
  }

  public static boolean isHdtPresent()
  {
    return hdtPresent;
  }
}
