package nmea.server.data;

import nmea.ctx.NMEAContext;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import nmea.server.constants.Constants;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAReader;

public class CustomTCPReader extends NMEAReader implements DataReader
{
  public CustomTCPReader(ArrayList<NMEAListener> al)
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

  public CustomTCPReader(ArrayList<NMEAListener> al, int tcp)
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

  private Socket skt = null;
  
  public void read()
  {
    System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpport);
    super.enableReading();
    try
    {
      skt = new Socket("localhost", tcpport);
      InputStream theInput = skt.getInputStream();
      byte buffer[] = new byte[4096];
      String s;
      for(; canRead(); super.fireDataRead(new NMEAEvent(this, s)))
      {
        int bytesRead = theInput.read(buffer);
        if(bytesRead == -1)
        {
          System.out.println("Nothing to read...");
          break;
        }
        System.out.println("# Read " + bytesRead + " characters");
        System.out.println("# " + (new Date()).toString());
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

        s = new String(toPrint);
        System.out.println(s);
      }

      System.out.println("Stop Reading tcp port.");
      theInput.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void closeReader() throws Exception
  {
    System.out.println("Stop Reading TCP Port");
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

  int tcpport = 80;
}
