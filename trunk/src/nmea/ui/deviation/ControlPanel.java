package nmea.ui.deviation;

import coreutilities.Utilities;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ControlPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JButton loadDataPointsButton = new JButton();
  private JLabel deviationCurveName = new JLabel();
  private JButton loadDevCurveButton = new JButton();
  private JButton saveDevCurveButton = new JButton();

  public ControlPanel()
  {
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
    this.setLayout(gridBagLayout1);
    loadDataPointsButton.setIcon(new ImageIcon(this.getClass().getResource("importIcon.png")));
    loadDataPointsButton.setToolTipText("Load logged data points");
    loadDataPointsButton.setSize(new Dimension(24, 24));
    loadDataPointsButton.setPreferredSize(new Dimension(24, 24));
    loadDataPointsButton.setBorderPainted(false);
    loadDataPointsButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dataFileButton_actionPerformed(e);
        }
      });
    deviationCurveName.setText(truncateFileName((String) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION_FILE)));
    loadDevCurveButton.setIcon(new ImageIcon(this.getClass().getResource("open.png")));
    loadDevCurveButton.setToolTipText("Load deviation curve");
    loadDevCurveButton.setPreferredSize(new Dimension(24, 24));
    loadDevCurveButton.setBorderPainted(false);
    loadDevCurveButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          loadDevCurveButton_actionPerformed(e);
        }
      });
    saveDevCurveButton.setIcon(new ImageIcon(this.getClass().getResource("save.png")));
    saveDevCurveButton.setToolTipText("Save deviation curve");
    saveDevCurveButton.setPreferredSize(new Dimension(24, 24));
    saveDevCurveButton.setBorderPainted(false);
    saveDevCurveButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          saveDevCurveButton_actionPerformed(e);
        }
      });
    this.add(loadDataPointsButton,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
    this.add(loadDevCurveButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    this.add(saveDevCurveButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    this.add(deviationCurveName, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    
    this.validate();
  }

  private void dataFileButton_actionPerformed(ActionEvent e)
  {
    final String fName = Utilities.chooseFile(JFileChooser.FILES_ONLY,                                   
                                              null, 
                                              "NMEA Data Files",
                                              "NMEA Data",
                                              "Open");
    if (fName != null && fName.trim().length() > 0)
    {
//    System.out.println("Loading " + fName);
      Thread dataThread = new Thread()
        {
          public void run()
          {
            List<double[]> data = Utils.getDataForDeviation(fName);
            NMEAContext.getInstance().fireLoadDataPointsForDeviation(data);
          }
        };
      dataThread.start();
    }
  }

  private void loadDevCurveButton_actionPerformed(ActionEvent e)
  {
    String fName = Utilities.chooseFile(JFileChooser.FILES_ONLY,                                   
                                        "csv", 
                                        "Deviation Curves",
                                        "Deviation Curves",
                                        "Load");
    if (fName != null && fName.trim().length() > 0)
    {
      String fLabel = truncateFileName(fName);
      deviationCurveName.setText(fLabel);
      NMEAContext.getInstance().setDeviation(Utils.loadDeviationCurve(fName));
      NMEAContext.getInstance().fireDeviationCurveChanged(Utils.loadDeviationHashtable(fName));
      NMEAContext.getInstance().getCache().put(NMEADataCache.DEVIATION_FILE, fName);
    }
  }
  
  private String truncateFileName(String f)
  {
    String fLabel = f;
    if (fLabel.lastIndexOf(File.separator) > -1)
      fLabel = fLabel.substring(0, 3) + "..." + fLabel.substring(fLabel.lastIndexOf(File.separator));
    return fLabel;
  }

  private void saveDevCurveButton_actionPerformed(ActionEvent e)
  {
    String fName = Utilities.chooseFile(JFileChooser.FILES_ONLY,                                   
                                        "csv", 
                                        "Deviation Curves",
                                        "Deviation Curves",
                                        "Save");
    if (fName != null && fName.trim().length() > 0)
    {
      List<double[]> dc = NMEAContext.getInstance().getDeviation();
      try
      {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
        for (double[] dev : dc)
        {
          String line = Double.toString(dev[0]) + "," + Double.toString(dev[1]) + "\n";
          bw.write(line);
        }
        bw.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
