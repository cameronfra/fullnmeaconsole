package nmea.server.data;

import nmea.server.NMEAEventManager;

import java.io.File;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;

public class CustomNMEAClient extends NMEAClient
{
  private NMEAEventManager parent;
  private String port = "";
  private int br = 4800;
  private int tcpport = -1;
  private File data = null;

  /**
   * Read Serial Port
   * @param parent
   * @param p
   * @param br
   */
  public CustomNMEAClient(NMEAEventManager parent, String p, int br)
  {
    port = p;
    this.br = br;
    setDevicePrefix("*");
    this.parent = parent;
    init();
  }

  /**
   * Read TCP Port
   * @param parent
   * @param tcp
   */
  public CustomNMEAClient(NMEAEventManager parent, int tcp)
  {
    tcpport = tcp;
    setDevicePrefix("*");
    this.parent = parent;
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
      this.setReader(new CustomFileReader(getListeners(), data));
    else if (tcpport == -1)
      this.setReader(new CustomSerialReader(getListeners(), port, br));
    else
      this.setReader(new CustomTCPReader(getListeners(), tcpport));
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
}
