package nmea.server.datareader.specific;

import nmea.server.ctx.NMEAContext;

import java.io.InputStream;

import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import java.net.SocketException;

import java.sql.Connection;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import javax.swing.JOptionPane;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;
/**
 * Works with SailMail rebroadcast
 */
public class CustomTCPReader extends NMEAReader implements DataReader
{
  private int tcpport     = 80;
  private String hostName = "localhost";

  public CustomTCPReader(List<NMEAListener> al)
  {
    super(al);
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
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
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
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
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
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
    boolean verbose = "true".equals((System.getProperty("verbose", "false")));
    System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpport + " on " + hostName);
    super.enableReading();
    try
    {
      InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
      skt = new Socket(address, tcpport);
      
      InputStream theInput = skt.getInputStream();
      byte buffer[] = new byte[4096];
      String s;
      int nbReadTest = 0;
      while (canRead())
      {
        int bytesRead = theInput.read(buffer);
        if (bytesRead == -1)
        {
          System.out.println("Nothing to read...");
          if (nbReadTest++ > 10)
            break;
        }
        else
        {
          if (verbose)
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
          NMEAEvent n = new NMEAEvent(this, s);
          super.fireDataRead(n);
          NMEAContext.getInstance().fireBulkDataRead(n);
        }
      }

      System.out.println("Stop Reading TCP port.");
      theInput.close();
    }
    catch (BindException be)
    {
      System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpport);
      be.printStackTrace();   
      manageError(be);
    }
    catch (SocketException se)
    {
//    se.printStackTrace();
      if (se.getMessage().indexOf("Connection refused") > -1)
        System.out.println("Refused (1)");
      else if (se.getMessage().indexOf("Connection reset") > -1)
        System.out.println("Reset (2)");
      else
      {
        if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) // : Connection timed out: connect)
        {
          // Wait and try again
          try
          {
            Thread userThread = new Thread("TCPReader")
            {
              public void run()
              {
                try { JOptionPane.showMessageDialog(null, "Timeout on TCP.\nWill re-try to connect again in 10s", "TCP Connection", JOptionPane.WARNING_MESSAGE);  }
                catch (Exception ex)
                {
                  System.out.println("Timeout on TCP.\nWill re-try to connect again in 10s");
                }
              }
            };
            userThread.start();
//          System.out.println("Timeout on TCP. Re-trying to connect in 10s");
            closeReader();
            Thread.sleep(10000L);
            System.out.println("Re-trying now.");
            read();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        else
          manageError(se);
      }
    }
    catch(Exception e)
    {
//    e.printStackTrace();
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
  
  public static void main(String[] args)
  {
    try
    {
      List<NMEAListener> ll = new ArrayList<NMEAListener>();
      NMEAListener nl = new NMEAListener()
      {
        @Override
        public void dataRead(NMEAEvent nmeaEvent)
        {
          System.out.println(nmeaEvent.getContent()); // TODO Send to the GUI?
        }
      };
      ll.add(nl);
      
//    CustomTCPReader ctcpr = new CustomTCPReader(ll, "192.168.0.124", 2947);
      CustomTCPReader ctcpr = new CustomTCPReader(ll, "localhost", 2947);
      // Works fine with the SailMail rebroadcast
//    CustomTCPReader ctcpr = new CustomTCPReader(ll, "theketch-lap.mshome.net", 7001);
      ctcpr.read();
    }
    catch (Exception e)
    {
      // TODO: Add catch code
      e.printStackTrace();
    }
  }
}
