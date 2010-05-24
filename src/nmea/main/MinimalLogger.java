package nmea.main;

import nmea.ctx.NMEAContext;
import nmea.ctx.Utils;

import nmea.local.LogisailResourceBundle;

import nmea.ui.NMEAFrame;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.UIManager;


public class MinimalLogger
{
  private boolean verbose = false;
  private boolean goLog = false;
  private NMEAFrame frame = null;
  
//private final static SimpleDateFormat dateFormat = new SimpleDateFormat("d-MMM-yyyy hh:mm:ss:");
  private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("HH:mm:ss");
  private static String pfile = Utils.PROPERTIES_FILE;

  public MinimalLogger(boolean verb, 
                       String serial, 
                       int br, 
                       String tcp, 
                       String data, 
                       String prmfile)
  {
    String lnf = System.getProperty("swing.defaultlaf");
    //  System.out.println("LnF:" + lnf);
    if (lnf == null) // Let the -Dswing.defaultlaf do the job.
    {
      try
      {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    verbose = verb;
    pfile = prmfile;
    if (verbose) System.out.println("Displaying minimal UI");
//  NMEAContext.getInstance().setFromFile(data != null && data.trim().length() > 0);
    frame = new NMEAFrame(verb, 
                          serial, 
                          br, 
                          tcp, 
                          data, 
                          pfile);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if(frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if(frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.addWindowListener(new WindowAdapter() 
    {
      public void windowClosing(WindowEvent e)
      {
        if (goLog) // Stop Logging
        {
          System.out.println("Stopping Logging...");
          NMEAContext.getInstance().fireLogChanged(false);
        }
        stop();
      }
    });

    frame.setVisible(true);
    Thread timeThread = new Thread()
      {
        public void run()
        {
          while (true)
          {
            Date now = new Date();
            frame.setTitle(LogisailResourceBundle.buildMessage("frame-title") + " " + DATE_FMT.format(now));
            try { Thread.sleep(1000L); } catch (Exception ex) {}
          }
        }
      };
    if (verb)
      System.out.println("Starting time thread");
    timeThread.start();
    if (data != null && data.trim().length() > 0)
      frame.setStatus("Simulation:" + data);
  }

  private void stop()
  {
    System.exit(0);
  }

  void fileExit_ActionPerformed(ActionEvent e)
  {
    System.exit(0);
  }

  public static void main(String args[])
  {
    System.out.println("Usage:");
    System.out.println(" java http.main.MinimalLogger [-config [configfile]] [-verb [y|n]] [[-simul [filename]] | [-serial [COM1] -br [4800]] | [-tcp [80]] ");
    String fName = null;
    boolean verb = false;
    String serial = "COM1";
    String br     = "4800";
    String tcp = "";
    for (int i = 0; i < args.length; i++)
    {
      if(args[i].equals("-verb"))
        verb = args[i + 1].toUpperCase().equals("Y");
      else if(args[i].equals("-serial"))
        serial = args[i + 1];
      else if(args[i].equals("-br"))
        br = args[i + 1];
      else if(args[i].equals("-tcp"))
        tcp = args[i + 1];
      else if(args[i].equals("-config"))
        pfile = args[i + 1];
      else if(args[i].equals("-simul"))
        fName = args[i + 1];
    }
    int baudrate = 4800;
    try
    {
      baudrate = Integer.parseInt(br);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    new MinimalLogger(verb, 
                      System.getProperty("nmea.serial.port", serial), 
                      Integer.parseInt(System.getProperty("nmea.baud.rate", Integer.toString(baudrate))), 
                      System.getProperty("nmea.tcp.port", tcp), // TCP Port
                      fName, 
                      System.getProperty("nmea.config.file", pfile));
  }

  
}
