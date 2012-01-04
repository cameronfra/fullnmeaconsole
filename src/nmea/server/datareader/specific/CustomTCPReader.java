package nmea.server.datareader.specific;

import nmea.server.ctx.NMEAContext;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;

public class CustomTCPReader extends NMEAReader implements DataReader
{
  private int tcpport = 80;
  private String hostName = "localhost";

  public CustomTCPReader(List<NMEAListener> al)
  {
    super(al);
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomTCPReader(List<NMEAListener> al, int tcp)
  {
    super(al);
    tcpport = tcp;
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomTCPReader(List<NMEAListener> al, String host, int tcp)
  {
    super(al);
    hostName = host;
    tcpport = tcp;
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  private Socket skt = null;
  
  public void read()
  {
    System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpport);
    super.enableReading();
    try
    {
      skt = new Socket(hostName, tcpport);
      InputStream theInput = skt.getInputStream();
      byte buffer[] = new byte[4096];
      String s;
      while (canRead())
      {
        int bytesRead = theInput.read(buffer);
        if(bytesRead == -1)
        {
          System.out.println("Nothing to read...");
          break;
        }
        if (false /* verbose */)
        {
          System.out.println("# Read " + bytesRead + " characters");
          System.out.println("# " + (new Date()).toString());
        }
        int nn = bytesRead;
        for(int i = 0; i < Math.min(buffer.length, bytesRead); i++)
        {
          if(buffer[i] != 0)
            continue;
          nn = i;
          break;
        }

        byte toPrint[] = new byte[nn];
        for(int i = 0; i < nn; i++)
          toPrint[i] = buffer[i];

        s = new String(toPrint) + NMEAParser.getEOS();
//      System.out.println("TCP:" + s);
        super.fireDataRead(new NMEAEvent(this, s));
      }

      System.out.println("Stop Reading TCP port.");
      theInput.close();
    }
    catch(Exception e)
    {
//    e.printStackTrace();
//    JOptionPane.showMessageDialog(null, "No such TCP port " + tcpport + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      manageError(e);
    }
  }

  public void closeReader() throws Exception
  {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
    try
    {
      if (skt != null)
      {
        this.goRead = false;
        skt.close();
        skt = null;
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
  { /* Not used for TCP */  }
}
