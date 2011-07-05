package nmea.server.datareader.specific;


import nmea.server.ctx.NMEAContext;

import java.io.File;
import java.io.FileInputStream;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAReader;
import ocss.nmea.api.NMEAEvent;
import java.util.ArrayList;

import nmea.server.constants.Constants;

import ocss.nmea.api.NMEAListener;

/**
 * A Simulator, taking its inputs from a file
 */
public class CustomFileReader extends NMEAReader implements DataReader
{
  File dataFile = null;
  public CustomFileReader(ArrayList<NMEAListener> al, File f)
  {
    super(al);
    dataFile = f;
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void replaySpeedChanged(int slider) 
        {
          // 50 :  1000
          // 100:     1
          // 0  : 10000
          int x = 100 - slider;
          double d = x / 50d;
          sleepTime = (int)(1000d * (Math.pow(d, 4)));
//        System.out.println("Sleeptime:" + sleepTime);
        }
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }
  
  private FileInputStream fis = null;
  private long sleepTime = 1000L;
  
  public void read()
  {
    // Simulation
    super.enableReading();
    try
    {
      fis = new FileInputStream(dataFile);
//    InputStream fis = this.getClass().getResourceAsStream(fileName);
//    assert fis != null;
      while (canRead())
      {
        double size = Math.random();
        int dim = (int)(750 * size);
        byte[] ba = new byte[dim];
        int l = fis.read(ba);
//      System.out.println("Read " + l);
        if (l != -1 && dim > 0)
        {
          String nmeaContent = new String(ba);
          super.fireDataRead(new NMEAEvent(this, nmeaContent));
          try { Thread.sleep(sleepTime); } catch (Exception ignore) {}
        }
        else
        {
          System.out.println("===== Reseting Reader =====");
          fis.close();
          fis = new FileInputStream(dataFile);
//        fis = this.getClass().getResourceAsStream(fileName);
        }
      }
    }
    catch (Exception e)
    {
     e.printStackTrace();
    }
  }

  public void closeReader() throws Exception
  {
    System.out.println("Stop reading Data File.");
    try
    {
      if (fis != null)
      {
        this.goRead = false;
        fis.close();
        fis = null;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void manageError(Throwable t)
  {
    throw new RuntimeException(t);
  }
  
  public void setTimeout(long timeout)
  { /* Not used for File Reader */  }
}
