package nmea.server.datareader.specific;

import nmea.server.ctx.NMEAContext;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import java.util.List;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;

public class CustomUDPReader extends NMEAReader implements DataReader
{
  private int udpport  = 8001;
  private long timeout = 5000L; // Default value

  public CustomUDPReader(List<NMEAListener> al)
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

  public CustomUDPReader(List<NMEAListener> al, int udp)
  {
    super(al);
    udpport = udp;
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

//private DatagramSocket  skt   = null;
  private MulticastSocket skt   = null;
  private InetAddress     group = null;
  
  private boolean verbose = System.getProperty("verbose", "false").equals("true");

  public void read()
  {
    System.out.println("From " + getClass().getName() + " Reading UDP Port " + udpport);
    super.enableReading();
    try
    {
//    skt = new DatagramSocket(udpport);
      skt = new MulticastSocket(udpport);
      group = InetAddress.getByName("230.0.0.1");
      skt.joinGroup(group);

      byte buffer[] = new byte[4096];
      String s;
      while (canRead())
      {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        // Wait here.
        Thread waiter = Thread.currentThread();
        DatagramReceiveThread drt = new DatagramReceiveThread(skt, packet, waiter, this);
        drt.start();

        synchronized (waiter)
        {
          try 
          { 
            long before = System.currentTimeMillis();
            if (timeout > -1)
              waiter.wait(timeout);  
            else
              waiter.wait();
            long after = System.currentTimeMillis();
            if (verbose) System.out.println("- (UDP) Done waiting (" + Long.toString(after - before) + " vs " + Long.toString(timeout) + ")");
            if (drt.isAlive())
            {
//            System.out.println("Interrupting the DatagramReceiveThread");
              drt.interrupt(); 
              if (timeout != -1 && (after - before) >= timeout)
                throw new RuntimeException("UDP took too long.");
            }
          }
          catch (InterruptedException ie) 
          { 
            if (verbose) System.out.println("Waiter Interrupted! (before end of wait, good)");              
          }
        }    
        s = new String(buffer, 0, packet.getLength());
        // For simulation from file:
        if (System.getProperty("os.name").toUpperCase().contains("LINUX"))
        {
          if (s.endsWith(NMEAParser.WINDOWS_NMEA_EOS))
            s = s.substring(0, s.length() - NMEAParser.WINDOWS_NMEA_EOS.length());
        }
        if (!s.endsWith(NMEAParser.getEOS()))
          s += NMEAParser.getEOS();
        if (verbose) System.out.println("UDP:" + s);
        super.fireDataRead(new NMEAEvent(this, s));        
      }
    }
    catch(Exception e)
    {
//    e.printStackTrace();
//    JOptionPane.showMessageDialog(null, "No such UDP port " + udpport + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      manageError(e);
    }
    finally
    {
      try
      {
        if (skt != null)
        {
          if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 1 - Leaving group " + group.toString());
          skt.leaveGroup(group);
          if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 2 - Closing MulticastSocket");
          skt.close();
        }
        else
          if (verbose) System.out.println("Multicast socket already null (closed)...");
      }
      catch (Exception ex)
      {
        System.err.println(">> Error when Closing Multicast Socket...");
        ex.printStackTrace();
      }
//    closeReader();
    }
  }

  public void closeReader() throws Exception
  {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading UDP Port");
    try
    {
      if (skt != null)
      {
        this.goRead = false;
        if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 1 - Leaving group " + group.toString());
        skt.leaveGroup(group);
        if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 2 - Closing MulticastSocket");
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
  {
    this.timeout = timeout;
  }

  public long getTimeout()
  {
    return timeout;
  }

  private class DatagramReceiveThread extends Thread
  {
    private DatagramSocket ds = null;
    private DataReader parent = null;
    private Thread waiter;
    private DatagramPacket packet;
    
    public DatagramReceiveThread(DatagramSocket ds, DatagramPacket packet, Thread from, DataReader dr)
    {
      super();
      this.ds = ds;
      this.parent = dr;
      this.waiter = from;
      this.packet = packet;
    }
    
    public void run()
    {
      try 
      { 
        skt.receive(packet);
        synchronized (waiter) 
        { 
//        System.out.println("Notifying waiter (Done).");
          waiter.notify(); 
        }
      }
      catch (Exception ex)
      {
//      ex.printStackTrace();
        parent.manageError(ex);
      }
    }
  }
}
