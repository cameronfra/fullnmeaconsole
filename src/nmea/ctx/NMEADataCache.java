package nmea.ctx;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Set;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.NMEADoubleValueHolder;

public class NMEADataCache extends HashMap<String, Object> implements Serializable
{
  public final static String SOG         = "SOG";
  public final static String POSITION    = "Boat Position";
  public final static String GPS_DATE_TIME = "GPS Date & Time";
  public final static String GPS_TIME    = "GPS Time";
  public final static String GPS_SOLAR_TIME = "Solar Time";
  public final static String COG         = "COG";
  public final static String DECLINATION = "D";
  public final static String BSP         = "BSP";
  public final static String LOG         = "Log";
  public final static String DAILY_LOG   = "Daily";
  public final static String WATER_TEMP  = "Water Temperature";
  public final static String AWA         = "AWA";
  public final static String AWS         = "AWS";
  public final static String HDG_COMPASS = "HDG c.";
  public final static String HDG_MAG     = "HDG mag.";
  public final static String HDG_TRUE    = "HDG true";
  public final static String DEVIATION   = "d";
  public final static String VARIATION   = "W";
  public final static String TWA         = "TWA";
  public final static String TWS         = "TWS";
  public final static String TWD         = "TWD";
  public final static String CSP         = "CSP";
  public final static String CDR         = "CDR";
  public final static String XTE         = "XTE";
  public final static String FROM_WP     = "From Waypoint";
  public final static String TO_WP       = "To Waypoint";
  public final static String WP_POS      = "WayPoint pos";
  public final static String DBT         = "DBT";
  public final static String D2WP        = "Distance to WP";
  public final static String B2WP        = "Bearing to WP";
  public final static String S2WP        = "Speed to WP";
  public final static String S2STEER     = "Steer";  
  public final static String LEEWAY      = "Leeway";
  public final static String CMG         = "CMG";
  
  public final static String BSP_FACTOR  = "BSP Factor";
  public final static String AWS_FACTOR  = "AWS Factor";
  public final static String AWA_OFFSET  = "AWA Offset";
  public final static String HDG_OFFSET  = "HDG Offset";
  public final static String MAX_LEEWAY  = "Max Leeway";

  public final static String DEVIATION_FILE      = "Deviation file name";
  public final static String DEFAULT_DECLINATION = "Default Declination";
  public final static String DAMPING             = "Damping";

  public final static HashMap<String, String> TOOLTIP_MAP = new HashMap<String, String>();

  // Damping ArrayList's
  private int dampingSize = 1;  
  
  private HashMap<String, ArrayList<Object>> dampingMap = new HashMap<String, ArrayList<Object>>();
  
  public NMEADataCache()
  {
    super();
    
    TOOLTIP_MAP.put(SOG,         "<html>Spped Over Ground<br>From the GPS</html>");
    TOOLTIP_MAP.put(POSITION,    "<html>Boat Position<br>From the GPS</html>");
    TOOLTIP_MAP.put(GPS_TIME,    "<html>GPS Time<br>From the GPS (GLL)</html>");
    TOOLTIP_MAP.put(GPS_DATE_TIME, "<html>GPS Time<br>From the GPS (RMC)</html>");
    TOOLTIP_MAP.put(GPS_SOLAR_TIME, "<html>Solar Time<br>From the GPS Time and Position</html>");
    TOOLTIP_MAP.put(COG,         "<html>Course Over Ground<br>From the GPS</html>");
    TOOLTIP_MAP.put(DECLINATION, "<html>Magnetic Declination<br>From the GPS</html>");
    TOOLTIP_MAP.put(BSP,         "<html>Boat Speed<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(LOG,         "<html>Log<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(DAILY_LOG,   "<html>Daily Log<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(WATER_TEMP,  "<html>Water Temperature<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(AWA,         "<html>Apparent Wind Angle<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(AWS,         "<html>Apparent Wind Speed<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(HDG_COMPASS, "<html>Heading, Compass<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(HDG_MAG,     "<html>Heading, Magnetic<br>Calculated with the deviation</html>");
    TOOLTIP_MAP.put(HDG_TRUE,    "<html>Heading, True<br>Calculated with the Variation (W = D + d)</html>");
    TOOLTIP_MAP.put(DEVIATION,   "<html>deviation<br>Estimated, see deviation curve</html>");
    TOOLTIP_MAP.put(VARIATION,   "<html>Variation<br>Calculated, W = D + d</html>");
    TOOLTIP_MAP.put(TWA,         "<html>True Wind Angle<br>Calculated</html>");
    TOOLTIP_MAP.put(TWS,         "<html>True Wind Speed<br>Calculated</html>");
    TOOLTIP_MAP.put(TWD,         "<html>True Wind Direction<br>Calculated</html>");
    TOOLTIP_MAP.put(CSP,         "<html>Current Speed<br>Calculated</html>");
    TOOLTIP_MAP.put(CDR,         "<html>Current Direction<br>Calculated</html>");
    TOOLTIP_MAP.put(XTE,         "<html>Cross Track Error<br>From the GPS</html>");
    TOOLTIP_MAP.put(FROM_WP,     "<html>From Waypoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(TO_WP,       "<html>To Waypoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(WP_POS,      "<html>WayPoint position<br>From the GPS</html>");
    TOOLTIP_MAP.put(DBT,         "<html>Depth Below Transducer<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(D2WP,        "<html>Distance to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(B2WP,        "<html>Bearing to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(S2WP,        "<html>Speed to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(S2STEER,     "<html>Steer left or right<br>From the GPS</html>");  
    TOOLTIP_MAP.put(LEEWAY,      "<html>Leeway<br>Estimated</html>");
    TOOLTIP_MAP.put(CMG,         "<html>Course Made Good<br>Calculated</html>");
    
    TOOLTIP_MAP.put(BSP_FACTOR, "<html>Coefficient to apply to Boat Speed<br>(1.0 = 100%)</html>");
    TOOLTIP_MAP.put(AWS_FACTOR, "<html>Coefficient to apply to Appaprent Wind Speed<br>(1.0 = 100%)</html>");
    TOOLTIP_MAP.put(AWA_OFFSET, "<html>Apparent Wind Angle Offset<br>In degrees, positive to the right, negative to the left</html>");
    TOOLTIP_MAP.put(HDG_OFFSET, "<html>Heading Offset<br>In degrees, positive to the right, negative to the left</html>");
    TOOLTIP_MAP.put(MAX_LEEWAY, "<html>Max Leeway, in degrees<br>Estimated</html>");
    
    TOOLTIP_MAP.put(DEVIATION_FILE, "Name of the CSV file containing the deviation curve points");
    TOOLTIP_MAP.put(DEFAULT_DECLINATION, "Declination to use, in case there is none in the NMEA Data");
    TOOLTIP_MAP.put(DAMPING,    "Damping for Data smoothing");

    dampingMap.put(BSP,      new ArrayList<Object>());
    dampingMap.put(HDG_TRUE, new ArrayList<Object>());
    dampingMap.put(AWA,      new ArrayList<Object>());
    dampingMap.put(AWS,      new ArrayList<Object>());
    dampingMap.put(TWA,      new ArrayList<Object>());
    dampingMap.put(TWS,      new ArrayList<Object>());
    dampingMap.put(TWD,      new ArrayList<Object>());
    dampingMap.put(CSP,      new ArrayList<Object>());
    dampingMap.put(CDR,      new ArrayList<Object>());
    dampingMap.put(COG,      new ArrayList<Object>());
    dampingMap.put(SOG,      new ArrayList<Object>());
    dampingMap.put(LEEWAY,   new ArrayList<Object>());
  }

  @Override
  public synchronized Object put(String key, Object value)
  {
    Object o = super.put(key, value);
    if (dampingSize > 1 && dampingMap.containsKey(key))
    {
      ArrayList<Object> ald = dampingMap.get(key);
      ald.add(value);      
      while (ald.size() > dampingSize)
        ald.remove(0);
    }
    return o;
  }

  // For debug
  double prevTWD = 0d;
  
  @Override
  public synchronized Object get(Object key)
  {
//  System.out.println("Damping = " + dampingSize);
    if (dampingSize > 1 && dampingMap.containsKey(key))
    {
      Object ret = null;
      Class cl = null;
      ArrayList<Object> ald = dampingMap.get(key);
      double sum = 0d;
      double sumCos = 0d,
             sumSin = 0d;
      
      for (Object v : ald)
      {
        if (cl == null)
          cl = v.getClass();
        if (v instanceof Double)
          sum += ((Double)v).doubleValue();
        else if (v instanceof NMEADoubleValueHolder)
        {
          // Debug
          if (false && key.equals(TWD))
            System.out.print(((NMEADoubleValueHolder)v).getDoubleValue() + ";");

          if (v instanceof Angle360 || v instanceof Angle180 || v instanceof Angle180EW || v instanceof Angle180LR)
          {
            double val = ((NMEADoubleValueHolder)v).getDoubleValue();
            sumCos += (Math.cos(Math.toRadians(val)));
            sumSin += (Math.sin(Math.toRadians(val)));
          }
          else
            sum += ((NMEADoubleValueHolder)v).getDoubleValue();
        }
        else
          System.out.println("What'zat:" + v.getClass().getName());
      }
      try
      {
        if (ald.size() != 0)
        {
          sum /= ald.size();
          sumCos /= ald.size();
          sumSin /= ald.size();
        }
        if (cl != null)
        {          
          if (cl.equals(Double.class))
          {
            ret = new Double(sum);
          }
          else
          {
            ret = Class.forName(cl.getName()).newInstance();
            if (ret instanceof Angle360 || ret instanceof Angle180 || ret instanceof Angle180EW || ret instanceof Angle180LR)
            {
              double a = Math.toDegrees(Math.acos(sumCos));
              if (sumSin < 0)
                a = 360d - a;
              sum = a;
              if (false && key.equals(TWD))
              {
                System.out.println(" Average:" + sum);
                if (ald.size() == dampingSize && Math.abs(sum - prevTWD) > 2)
                  System.out.println("Hoonk!!");
                prevTWD = sum;
              }
            }
            ((NMEADoubleValueHolder)ret).setDoubleValue(sum);
            
            if (false) // Debug...
            {
              double orig = 0;
              if (cl.equals(Double.class))
                orig = ((Double)super.get(key)).doubleValue();
              else
                orig = ((NMEADoubleValueHolder)super.get(key)).getDoubleValue();
              System.out.println("Damping on " + dampingSize + " value(s):" + sum + " instead of " + orig + " for " + key);
            }
          }
        }
        else
          ret = super.get(key);
      }
      catch (Exception ex)
      {
        System.err.println("For key:" + key);
        ex.printStackTrace();
      }
      return ret;
    }
    else
      return super.get(key);
  }

  public void setDampingSize(int dampingSize)
  {
    System.out.println("Setting Damping to " + dampingSize);
    this.dampingSize = dampingSize;
    NMEAContext.getInstance().fireDataChanged();
  }

  public int getDampingSize()
  {
    return dampingSize;
  }
  
  public void resetDampingBuffers()
  {
    Set<String> keys = dampingMap.keySet();
    for (String k : keys)
      dampingMap.get(k).clear();
  }
}
