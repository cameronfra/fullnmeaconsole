package nmea.server.data;

import nmea.ctx.NMEAContext;

import java.io.InputStream;
import java.util.ArrayList;
import javax.comm.CommPort;
import javax.comm.NoSuchPortException;
import javax.comm.UnsupportedCommOperationException;
import javax.comm.SerialPort;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;

import javax.swing.JOptionPane;

import nmea.server.constants.Constants;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAReader;

public class CustomSerialReader extends NMEAReader implements DataReader
{
  private String comPort = "COM1";
  private int br = 4800;

  private CommPort thePort = null;

  public CustomSerialReader(ArrayList<NMEAListener> al)
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

  public CustomSerialReader(ArrayList<NMEAListener> al, String com, int br)
  {
    super(al);
    comPort = com;
    this.br = br;
    NMEAContext.getInstance().addNMEAListener(new nmea.event.NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public void read()
  {
    System.out.println("read() - From " + this.getClass().getName() + " Reading Serial Port " + comPort);
    super.enableReading();
    CommPortIdentifier com = null;
    try
    {
      com = CommPortIdentifier.getPortIdentifier(comPort);
    }
    catch(NoSuchPortException nspe)
    {
      nspe.printStackTrace();
      System.err.println(this.getClass().getName() + ":read() - No Such Port");
      JOptionPane.showMessageDialog(null, "No such port " + comPort + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException(nspe);
   // return;
    }
    try
    {
      thePort = com.open("PortOpener", 10);
    }
    catch(PortInUseException piue)
    {
      piue.printStackTrace();
      System.err.println(this.getClass().getName() + ":read() - Port In Use");
      JOptionPane.showMessageDialog(null, comPort + " in use!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException(piue);
   // return;
    }
    int portType = com.getPortType();
    if(portType == 2)
      System.out.println(this.getClass().getName() + ":read() - This is a parallel port");
    else
    if(portType == 1)
      System.out.println(this.getClass().getName() + ":read() - This is a serial port");
    else
      System.out.println(this.getClass().getName() + ":read() - This is an unknown port:" + portType);
    if(portType == 1)
    {
      SerialPort sp = (SerialPort)thePort;
      try
      {
        sp.setSerialPortParams(br, 8, 1, 0);
      }
      catch(UnsupportedCommOperationException ucoe)
      {
        System.err.println("read() - Unsupported Comm Operation");
        return;
      }
    }
    try
    {
      byte buffer[] = new byte[4096];
      InputStream theInput = thePort.getInputStream();
      System.out.println(this.getClass().getName() + ":read() - Reading serial port...");
      String s;
      for(; canRead(); super.fireDataRead(new NMEAEvent(this, s)))
      {
        int bytesRead = theInput.read(buffer);
        if(bytesRead == -1)
          break;
//      System.out.println("# Read " + bytesRead + " characters");
//      System.out.println("# " + (new Date()).toString());
        int nn = bytesRead;
        for(int i = 0; i < Math.min(buffer.length, bytesRead); i++)
        {
          if (buffer[i] != 0)
            continue;
          nn = i;
          break;
        }

        byte toPrint[] = new byte[nn];
        for(int i = 0; i < nn; i++)
          toPrint[i] = buffer[i];

        s = new String(toPrint);
//      System.out.println(s);
      }
      System.out.println("2 - " + this.getClass().getName() + ":Stop Reading serial port.");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void closeReader() throws Exception
  {
    System.out.println("1 - Stop reading Serial Port");
    try
    {
      if (thePort != null)
      {
        this.goRead = false;
        thePort.close();
        thePort = null;
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
}
