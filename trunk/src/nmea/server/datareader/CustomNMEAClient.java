package nmea.server.datareader;

import nmea.server.NMEAEventManager;

import java.io.File;

import nmea.server.datareader.specific.CustomFileReader;

import nmea.server.datareader.specific.CustomSerialReader;

import nmea.server.datareader.specific.CustomTCPReader;

import nmea.server.datareader.specific.CustomUDPReader;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;

public abstract class CustomNMEAClient extends NMEAClient
{
  private NMEAEventManager parent;
  private String port = "";
  private int br = 4800;
  private int tcpOrUdpPort = -1;
  private File data = null;
  
  private final static long DEFAULT_TIMEOUT = -1L;
  
  private long timeout = DEFAULT_TIMEOUT;
  
  public final static int TCP_OPTION = 0;
  public final static int UDP_OPTION = 1;
  
  private int option = TCP_OPTION;

  /**
   * Read Serial Port
   * @param parent
   * @param p
   * @param br
   */
  public CustomNMEAClient(NMEAEventManager parent, String p, int br)
  {
    this(parent, p, br, DEFAULT_TIMEOUT);
  }
  /**
   * Read Serial Port
   * @param parent
   * @param p    Serial port name, like COM10
   * @param br   Baud rate, like 4800
   * @param tout Timeout in milliseconds
   */
  public CustomNMEAClient(NMEAEventManager parent, String p, int br, long tout)
  {
    port = p;
    this.br = br;
    setDevicePrefix("*");
    this.parent = parent;
    this.timeout = tout;
    init();
  }

  /**
   * Read TCP/UDP Port
   * @param parent
   * @param port
   */
  public CustomNMEAClient(NMEAEventManager parent, int option, int port)
  {
    this(parent, option, port, DEFAULT_TIMEOUT);
  }
  /**
   * Read TCP/UDP Port
   * @param parent
   * @param port TCP or UDP port number, like 8001
   * @param tout Timeout in milliseconds
   */
  public CustomNMEAClient(NMEAEventManager parent, int option, int port, long tout)
  {
    this.option = option;
    tcpOrUdpPort = port;
    setDevicePrefix("*");
    this.parent = parent;
    this.timeout = tout;
    init();
  }

  /**
   * Read from log file
   * @param parent
   * @param f
   */
  public CustomNMEAClient(NMEAEventManager parent, File f)
  {
    data = f;
    setDevicePrefix("*");
    this.parent = parent;
    this.setEOS("\n");
    init();
  }
  private DataReader reader = null;
  
  private void init()
  {
    super.initClient();
    if (data != null)
      this.setReader(new CustomFileReader(getListeners(), data));            // File (Simulation, replay)
    else if (tcpOrUdpPort == -1)
    {
      this.setReader(new CustomSerialReader(getListeners(), port, br)        // Serial
        {
          @Override
          public void manageError(Throwable t)
          {
//          System.out.println("Aha!");
//          System.err.println(t.toString());
            manageNMEAError(t);
          }
        });     
      ((CustomSerialReader)this.getReader()).setTimeout(timeout);
    }
    else if (option == TCP_OPTION)
      this.setReader(new CustomTCPReader(getListeners(), tcpOrUdpPort)       // TCP
        {
          @Override
          public void manageError(Throwable t)
          {
            manageNMEAError(t);
          }
        });
    else if (option == UDP_OPTION)
    {
      this.setReader(new CustomUDPReader(getListeners(), tcpOrUdpPort)       // UDP. 
        {
          @Override
          public void manageError(Throwable t)
          {
            manageNMEAError(t);
          }                       
        });
      ((CustomUDPReader)this.getReader()).setTimeout(timeout);
    }
    reader = (DataReader)this.getReader();
    
    super.startWorking();
    
    System.out.println(this.getClass().getName() + ":NMEA Client initialized, and started");
  }

  public void dataDetectedEvent(NMEAEvent e)
  {
    String val = e.getContent();
//  if (parent.verbose())
//    System.out.println("Detected:" + val);
    parent.manageDataEvent(val);
  }

  public void stopReading() throws Exception
  {
    if (reader != null)
    {
      reader.closeReader();
    }
  }
  
  public abstract void manageNMEAError(Throwable t);
}
