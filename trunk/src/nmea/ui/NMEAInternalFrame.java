package nmea.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;
import nmea.ctx.Utils;

import nmea.local.LogisailResourceBundle;

import nmea.server.constants.Constants;

import nmea.ui.widgets.BeaufortPanel;
import nmea.ui.widgets.ConfigTablePanel;

import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Speed;


public class NMEAInternalFrame
  extends JInternalFrame
{
  private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("HH:mm:ss");

  private NMEAInternalFrame instance = this;

  private JMenuBar menuBar = new JMenuBar();
  private JMenu menuFile = new JMenu(LogisailResourceBundle.buildMessage("file"));
//private JMenuItem menuFileExit = new JMenuItem(LogisailResourceBundle.buildMessage("exit"));
  private JMenuItem menuFileSave = new JMenuItem(LogisailResourceBundle.buildMessage("save"));
  private JMenuItem menuFileManageConfig = new JMenuItem(LogisailResourceBundle.buildMessage("manage"));
  private JMenuItem menuFileSavePrms = new JMenuItem("Save Calibration Parameters");

  private JMenu menuHelp = new JMenu("Help");
  private JMenuItem menuHelpAbout = new JMenuItem("About");
  private JMenuItem menuHelpBeaufort = new JMenuItem("Beaufort Scale");

  private NMEAMasterPanel nmeaTP = null;

  private String pfile = "";

  private BorderLayout borderLayout1 = new BorderLayout();

  private String[] currentSentencesToLog = null;

  private boolean verbose = false;
  private String serial = null;
  private int br = 0;
  private String tcp = "";
  private String data = null;

  public NMEAInternalFrame(boolean v, 
                           String serial, 
                           int br, 
                           String port, 
                           String fName, // simulation file
                           String propertiesFile)
  {
    this.verbose = v;
    this.serial = serial;
    this.br = br;
    this.tcp = port;
    this.data = fName;
    pfile = propertiesFile;
    NMEAContext.getInstance().setFromFile(data != null && data.trim().length() > 0);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    nmeaTP = new NMEAMasterPanel(verbose, serial, br, tcp, data, pfile, false);
    this.setJMenuBar(menuBar);
    menuBar.add(menuFile);
    menuFile.add(menuFileSave);
    menuFile.add(menuFileManageConfig);
    menuFile.add(menuFileSavePrms);
    menuFile.add(new JSeparator());
//  menuFile.add(menuFileExit);
//    menuFileExit.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent ae)
//        {
//          NMEAContext.getInstance().fireLogChanged(false);
//          System.exit(0);
//        }
//      });
    menuFileSave.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          NMEAContext.getInstance().fireSaveUserConfig();
        }
      });
    menuFileManageConfig.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          HashMap<String, String> userConfig = nmeaTP.getVp().getUserConfig();
          Object[][] data = new Object[userConfig.size()][2];
          int i = 0;
          for (String s: userConfig.keySet())
          {
            data[i][0] = s;
            data[i][1] = userConfig.get(s);
            i++;
          }
          ConfigTablePanel ctp = new ConfigTablePanel();
          ctp.setData(data);
          int resp = JOptionPane.showConfirmDialog(instance, ctp, LogisailResourceBundle.buildMessage("manage"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
          if (resp == JOptionPane.OK_OPTION)
          {
            // Save Configs
            data = ctp.getData();
            userConfig = new HashMap<String, String>(data.length);
            for (i = 0; i < data.length; i++)
              userConfig.put((String) data[i][0], (String) data[i][1]);
            nmeaTP.getVp().setUserConfig(userConfig);
            nmeaTP.getVp().saveUserConfigs();
          }
        }
      });
    menuFileSavePrms.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          Utils.writeNMEAParameters();
        }
      });
    menuBar.add(menuHelp); 
    menuHelp.add(menuHelpAbout);
    menuHelpAbout.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          Utils.displayHelpAbout(instance);
        }
      });    
    menuHelp.add(menuHelpBeaufort);
    menuHelpBeaufort.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          BeaufortPanel bp = new BeaufortPanel();
          JOptionPane.showMessageDialog(instance, bp, "Beaufort Scale", JOptionPane.PLAIN_MESSAGE);
        }
      });

    getContentPane().setLayout(borderLayout1);
    setSize(new Dimension(770, 650));
    setTitle(LogisailResourceBundle.buildMessage("frame-title"));
    try { this.setFrameIcon(new ImageIcon(this.getClass().getResource("controller.png"))); } catch (Exception ignore) {}
    this.addInternalFrameListener(new InternalFrameAdapter()
      {
        public void internalFrameClosed(InternalFrameEvent e)
        {
     //   try { NMEAContext.getInstance().fireStopReading(); } catch (Exception ex) {}
          NMEAContext.getInstance().getCache().resetDampingBuffers();
          NMEAContext.getInstance().fireInternalFrameClosed();
//        NMEAContext.getInstance().removeNMEAListener(nmeaTP.getNMEAListener());
          NMEAContext.getInstance().removeNMEAListenerGroup(Constants.NMEA_SERVER_LISTENER_GROUP_ID);
          Utils.play(this.getClass().getResource("R2D2.wav"));
        }
      });
    getContentPane().add(nmeaTP, BorderLayout.CENTER);

    nmeaTP.getVp().setCurrentSentences(currentSentencesToLog);

    Thread timeThread = new Thread()
      {
        public void run()
        {
          while (true)
          {
            Date now = new Date();
            String title = DATE_FMT.format(now);
            NMEADataCache cache = NMEAContext.getInstance().getCache();
            if (cache != null)
            {
              double tws = 0d, twd = 0d;
              boolean ok = true;
              try { tws = ((Speed)cache.get(NMEADataCache.TWS)).getValue(); } catch (Exception ex) { ok = false; }
              try { twd = ((Angle360)cache.get(NMEADataCache.TWD)).getValue(); } catch (Exception ex) { ok = false; }
              if (ok)
                title += (" - Wind: " + Utils.getRoseSpeedAndDirection(tws, twd) + " - ");
            }            
            setTitle(title + " " + LogisailResourceBundle.buildMessage("frame-title"));
            try { Thread.sleep(1000L); } catch (Exception ex) {}
          }
        }
      };
    if (verbose)
      System.out.println("Starting time thread");
    timeThread.start();
  }

}
