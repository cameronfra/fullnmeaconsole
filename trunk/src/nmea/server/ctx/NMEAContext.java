package nmea.server.ctx;


import coreutilities.sql.SQLUtil;

import java.io.File;
import java.io.Serializable;

import java.sql.Connection;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import java.util.List;

import nmea.event.NMEAListener;

import oracle.xml.parser.v2.DOMParser;


/**
 * A singleton
 */
public class NMEAContext implements Serializable
{
  private static NMEAContext applicationContext = null;  
  private transient List<NMEAListener> NMEAListeners = null;
  private NMEADataCache dataCache = null;
  private transient NMEADataCache frozenDataCache = null;
  private static List<double[]> deviation = null;
  
  public final static DecimalFormat DF22 = new DecimalFormat("00.00");
  public final static DecimalFormat DF3  = new DecimalFormat("000");
  
  public final static int DEFAULT_BUFFER_SIZE = 3600;
  
  private boolean fromFile = false;
  
  private transient DOMParser parser = null;
  private transient Connection dbConnection = null;
  
  private NMEAContext()
  {
    NMEAListeners = new ArrayList<NMEAListener>(2); // 2: Initial Capacity
    parser = new DOMParser();
    dataCache = new NMEADataCache();
  }
    
  public static synchronized NMEAContext getInstance()
  {
    if (applicationContext == null)
      applicationContext = new NMEAContext();    
    return applicationContext;
  }
    
  public List<NMEAListener> getListeners()
  {
    return NMEAListeners;
  }    

  public synchronized void addNMEAListener(NMEAListener l)
  {
    if (!NMEAListeners.contains(l))
    {
      NMEAListeners.add(l);
    }
    if (System.getProperty("verbose", "false").equals("true")) System.out.println("We have " + NMEAListeners.size() + " NMEAListener(s). Last one belongs to group [" + l.getGroupID() + "]");
  }

  public synchronized void removeNMEAListener(NMEAListener l)
  {
    NMEAListeners.remove(l);
  }

  public synchronized void removeNMEAListenerGroup(String groupID)
  {
    System.out.println("Removing NMEAListener from group [" + groupID + "]");
    System.out.println("Start with " + NMEAListeners.size() + " NMEAListener(s).");
    List<NMEAListener> toRemove = new ArrayList<NMEAListener>(1);
    for (NMEAListener listener : NMEAListeners)
    {
      if (listener.getGroupID().equals(groupID))
        toRemove.add(listener);
    }
    for (NMEAListener nl : toRemove)
      removeNMEAListener(nl);
    System.out.println("End up with " + NMEAListeners.size() + " NMEAListener(s):");
    for (NMEAListener listener : NMEAListeners)
    {
      System.out.println("Remaining: Listener belongs to group [" + listener.getGroupID() + "]");
    }
  }

  public DOMParser getParser()
  {
    return parser;
  }
  
  public Connection getDBConnection()
  {
    if (dbConnection == null)
    {
      String dbLoc = System.getProperty("db.location", ".." + File.separator + "all-db");
      try { dbConnection = SQLUtil.getConnection(dbLoc, "LOG", "log", "log"); }
      catch (Exception ex) { ex.printStackTrace(); }
    }
    return dbConnection;
  }
  
  public void closeDBConnection()
  {
    try { SQLUtil.shutdown(dbConnection); } catch (Exception ex) { ex.printStackTrace(); }
    dbConnection = null;
  }

  public void fireInternalFrameClosed()
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.internalFrameClosed();
    }
  }
  
  public void fireLogChanged(boolean b)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.log(b);
    }
  }
  
  public void fireLogChanged(boolean log, boolean withDateTime)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.log(log, withDateTime);
    }
  }
  
  public void fireWindScale(float f)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.setWindScale(f);
    }
  }
  
  public void fireSetMessage(String s)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.setMessage(s);
    }
  }
  
  public void fireNMEAString(String s)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.manageNMEAString(s);
    }
  }
  
  public void fireSaveUserConfig()
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.saveUserConfig();
    }
  }
  
  public void fireDataChanged()
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.dataUpdate();
    }
  }  

  public void fireDataBufferSizeChanged(int size)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.dataBufferSizeChanged(size);
    }
  }  

  public void fireLoadDataPointsForDeviation(List<double[]> data)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.loadDataPointsForDeviation(data);
    }
  }
  
  public void fireDeviationCurveChanged(Hashtable<Double, Double> devCurve)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.deviationCurveChanged(devCurve);
    }
  }
  
  public void fireReplaySpeedChanged(int slider)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.replaySpeedChanged(slider);
    }
  }
  
  public void fireStopReading() throws Exception
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.stopReading();
    }
  }

  public void fireRefreshJournal()
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.refreshLogJournal();
    }
  }
  
  public void fireShowRawData(boolean b)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = NMEAListeners.get(i);
      l.showRawData(b);
    }
  }
    
  public synchronized NMEADataCache getCache()
  {
//  System.out.println("NMEAContext getCache:" + Integer.toString(dataCache.size()) + " pos.");
    return dataCache;
  }
  
  public static NMEADataCache getCache_oneTrip() // Used for RMI
  {
    NMEADataCache cache = getInstance().getCache();
    return cache;
  }
  
  public synchronized Object getDataCache(String key)
  {
    return dataCache.get(key);
  }
  
  public synchronized void putDataCache(String key, Object value)
  {
    dataCache.put(key, value);
    fireDataChanged();
  }

  public synchronized void putDataCache(HashMap<String, Object> map)
  {
    dataCache.putAll(map);
    fireDataChanged();
  }

  public synchronized void setDeviation(List<double[]> deviation)
  {
    NMEAContext.deviation = deviation;
  }

  public synchronized List<double[]> getDeviation()
  {
    return deviation;
  }

  public void setFrozenDataCache(NMEADataCache frozenDataCache)
  {
    this.frozenDataCache = frozenDataCache;
  }

  public NMEADataCache getFrozenDataCache()
  {
    return frozenDataCache;
  }
  
  public synchronized NMEADataCache cloneCache()
  {
    NMEADataCache clone = null;
    synchronized (dataCache)
    {
      if (dataCache != null)
      {
        clone = new NMEADataCache();
        clone.putAll(dataCache);
      }
    }
    return clone;
  }

  public void setFromFile(boolean fromFile)
  {
    this.fromFile = fromFile;
  }

  public boolean isFromFile()
  {
    return fromFile;
  }
}
