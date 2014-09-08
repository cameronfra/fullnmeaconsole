package utils.log;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.text.ParseException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.TreeMap;

import utils.astro.*;

import ocss.nmea.parser.GeoPos;

import utils.NMEAAnalyzer.ScalarValue;

public class LogAnalysis
{
  private LogAnalysisFrame frame = null;
  private static double valueCoeff = 1f; // MULTIPLYING VALUE by this one !
  private static int hourOffset   = 0;
  private final Map<Date, ScalarValue> logdata = new HashMap<Date, ScalarValue>();
  private GeoPos pos = null;
  private String timeZone = null;
  
  public LogAnalysis(Map<Date, ScalarValue> data, String title, String unit) throws IOException, ParseException
  {
    this(data, title, unit, null, null);
  }
  public LogAnalysis(Map<Date, ScalarValue> data, String title, String unit, GeoPos pos, String tz) throws IOException, ParseException
  {
    this.pos = pos;
    this.timeZone = tz;
    valueCoeff = Double.parseDouble(System.getProperty("value.coeff", "1.0"));
    hourOffset = Integer.parseInt(System.getProperty("hour.offset",   "0"));
    
    frame = new LogAnalysisFrame(this, title, unit);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
//  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//  frame.addWindowListener(new WindowAdapter() 
//    {
//      public void windowClosing(WindowEvent e)
//      {
//        System.exit(0);
//      }
//    });

//  frame.setVisible(true);

    Date minDate = null, maxDate = null;
    long smallestTimeInterval = Long.MAX_VALUE;
    long previousDate = -1L;
    double prevValue = 0;
    double minValue = Double.MAX_VALUE, maxValue = Double.MIN_VALUE;

    boolean withSmoothing = "true".equals(System.getProperty("with.smoothing", "true"));
    Set<Date> keys = data.keySet();
    long recNum = 0;
    for (Date d : keys)
    {
      recNum++;
      ScalarValue ld = data.get(d);
      try
      {
        Date logDate  = d;
        logDate = new Date(logDate.getTime() + (hourOffset * 3600 * 1000)); // TODO Make sure the gap is not too big (like > 1 day)
        double value = ld.getValue() * valueCoeff; 
        if (withSmoothing && previousDate != -1)
        { 
          // Smooth...
          long deltaT = (logDate.getTime() - previousDate) / 1000; // in seconds
          double deltaValue = (value - prevValue);
          for (int i=0; i<deltaT; i++)
          {
            Date smoothDate  = new Date(previousDate + (i * 1000));
            double smoothValue = prevValue + (float)((double)deltaValue * ((double)i / (double)deltaT));
            logdata.put(smoothDate, new ScalarValue(smoothValue));
          }
        }
        else
        {
          logdata.put(logDate, new ScalarValue(value));
        }
        if (minDate == null)
          minDate = logDate;
        else
        {
          long interval = logDate.getTime() - previousDate;
          if (previousDate > logDate.getTime())
            System.out.println("Record " + Long.toString(recNum) + ", Previous:" + new Date(previousDate).toString() + ", Current:" + logDate.toString());
          else
            smallestTimeInterval = Math.min(smallestTimeInterval, interval);
          if (logDate.before(minDate))
            minDate = logDate;
        }
        prevValue  = value;
        previousDate = logDate.getTime();
        if (maxDate == null)
          maxDate = logDate;
        else
        {
          if (logDate.after(maxDate))
            maxDate = logDate;
        }
        minValue = Math.min(minValue, value);
        maxValue = Math.max(maxValue, value);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
  }
  // Sort
//  SortedSet<Date> keys = new TreeSet<Date>(logdata.keySet());
//  for (Date key : keys) 
//  { 
//     LogData value = logdata.get(key);
//     // do something
//    System.out.println(value.getDate() + ": " + value.getVoltage()  + " V");
//  }
    System.out.println("From  [" + minDate + "] to [" + maxDate + "] (" + Long.toString((maxDate.getTime() - minDate.getTime()) / 1000) + " s)");
    System.out.println(msToHMS(maxDate.getTime() - minDate.getTime()));
    System.out.println("Data range [" + minValue + ", " + maxValue + "]");
    System.out.println("Smallest interval:" + (smallestTimeInterval / 1000) + " s.");
    System.out.println("LogData has " + logdata.size() + " element(s)");
    // Calculate sun rise and set here.
    Calendar reference = new GregorianCalendar();
    reference.setTimeInMillis(minDate.getTime());
    reference.set(Calendar.HOUR_OF_DAY, 0);
    reference.set(Calendar.MINUTE, 0);
    reference.set(Calendar.SECOND, 0);
    
    if (pos == null)
      pos = new GeoPos(37.501315, -122.48090); // HMB
    if (this.timeZone == null)
      this.timeZone = "America/Los_Angeles"; // HMB
    
    Map riseAndSet = new TreeMap<Long, Calendar[]>();
    
    System.out.println("MaxDate:" + maxDate.toString());
    
    while (reference.getTime().before(maxDate))
    {
      Calendar[] sunRiseSet = AstroUtil.calculateRiseSet(pos, this.timeZone, reference);
      riseAndSet.put(reference.getTimeInMillis(), sunRiseSet);
//    System.out.println("-- Sunrise:" + sunRiseSet[0].getTime());
//    System.out.println("-- Sunset :" + sunRiseSet[1].getTime());    
      reference.add(Calendar.HOUR, 24);
//    System.out.println("Ref:" + reference.getTime().toString());
    }
    frame.setLogData(logdata, riseAndSet, this.timeZone);
  }
  
  public void setTimeZone(String tz)
  {
    this.timeZone = tz;
    frame.setTimeZone(tz);
  }
  
  private final static long SEC  = 1000L;
  private final static long MIN  = 60 * SEC;
  private final static long HOUR = 60 * MIN;
  private final static long DAY  = 24 * HOUR;
  
  private static String msToHMS(long ms)
  {
    String str = "";
    long remainder = ms;
    int days = (int)(remainder / DAY);
    remainder -= (days * DAY);
    int hours = (int)(remainder / HOUR);
    remainder -= (hours * HOUR);
    int minutes = (int)(remainder / MIN);
    remainder -= (minutes * MIN);
    float seconds = (float)(remainder / SEC);
    if (days > 0)
      str = days + " day(s) "; 
    if (hours > 0 || str.trim().length() > 0)
      str += hours + " hour(s) ";
    if (minutes > 0 || str.trim().length() > 0)
      str += minutes + " minute(s) ";
    str += seconds + " sec(s)";    
    return str.trim();
  }
  
  public void show()
  {
    frame.setVisible(true);
  }
  
  public void hide()
  {
    frame.setVisible(false);
  }
  
  public static class LogData
  {
    private Date date;
    private double value;

    public Date getDate()
    {
      return date;
    }

    public double getValue()
    {
      return this.value;
    }
    
    public LogData(Date d, double value)
    {
      this.date = d;
      this.value = value;
    }
  }
}
