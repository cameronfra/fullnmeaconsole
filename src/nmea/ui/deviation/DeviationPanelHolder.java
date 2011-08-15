package nmea.ui.deviation;

import java.awt.Dimension;
import java.awt.Rectangle;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import nmea.event.NMEAListener;

import java.awt.BorderLayout;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import nmea.server.constants.Constants;

public class DeviationPanelHolder
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private DeviationPanel deviationPanel = new DeviationPanel();
  private JPanel bottomPanel = new JPanel();
  private JButton suggestButton = new JButton();
  private JCheckBox sprayCheckBox = new JCheckBox();
  private JCheckBox deleteCheckBox = new JCheckBox();
  private JButton resetButton = new JButton();
  private JButton zoomInButton = new JButton();
  private JButton zoomOutButton = new JButton();
  private JCheckBox showHideDataPoints = new JCheckBox();
  private JCheckBox showHideCurvePoints = new JCheckBox();
  private JButton movePointsButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JCheckBox printCheckBox = new JCheckBox();
  
  private boolean printVersion = false;

  public DeviationPanelHolder()
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
    this.setLayout(borderLayout1);
    this.setBounds(new Rectangle(10, 10, 600, 600));
    this.setSize(new Dimension(733, 599));
    bottomPanel.setLayout(gridBagLayout1);
    this.add(deviationPanel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    suggestButton.setText("Suggest");
    suggestButton.setToolTipText("From logged data");
    bottomPanel.add(suggestButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    suggestButton.setEnabled(false);
    suggestButton.setVisible(false);
    suggestButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          suggestButton_actionPerformed(e);
        }
      });
    sprayCheckBox.setText("Spray");
    sprayCheckBox.setToolTipText("Spray more points...");
    bottomPanel.add(sprayCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    sprayCheckBox.setSelected(false);
    sprayCheckBox.setEnabled(false);
    sprayCheckBox.setVisible(false);
    sprayCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.setSprayPoints(sprayCheckBox.isSelected());
        }
      });
        
    deleteCheckBox.setText("Del.");
    deleteCheckBox.setToolTipText("Delete points...");
    bottomPanel.add(deleteCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    deleteCheckBox.setSelected(false);
    deleteCheckBox.setEnabled(false);
    deleteCheckBox.setVisible(false);
    deleteCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          sprayCheckBox.setEnabled(!deleteCheckBox.isSelected());
          deviationPanel.setDeletePoints(deleteCheckBox.isSelected());
        }
      });
            
    resetButton.setText("Reset");
    resetButton.setToolTipText("From the file");
    bottomPanel.add(resetButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    resetButton.setEnabled(true);
    resetButton.setVisible(true);
    resetButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          resetButton_actionPerformed(e);
        }
      });
    zoomInButton.setText("Zoom in");
    zoomInButton.setToolTipText("Narrow width");
    bottomPanel.add(zoomInButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    zoomInButton.setEnabled(true);
    zoomInButton.setVisible(true);
    zoomInButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.setWidthFactor(deviationPanel.getWidthFactor() * 1.1);
          deviationPanel.repaint();
        }
      });
    zoomOutButton.setText("Zoom out"); 
    zoomOutButton.setToolTipText("Widen width");
    bottomPanel.add(zoomOutButton, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    zoomOutButton.setEnabled(true);
    zoomOutButton.setVisible(true);
    zoomOutButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.setWidthFactor(deviationPanel.getWidthFactor() / 1.1);
          deviationPanel.repaint();
        }
      });
    
    showHideDataPoints.setText("Show data points");
    showHideDataPoints.setSelected(true);
    bottomPanel.add(showHideDataPoints, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    showHideDataPoints.setEnabled(false);
    showHideDataPoints.setVisible(false);
    showHideDataPoints.setSelected(true);
    showHideDataPoints.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.setShowData(showHideDataPoints.isSelected());
          suggestButton.setVisible(showHideDataPoints.isSelected());
          deviationPanel.repaint();
        }
      });

    showHideCurvePoints.setText("Show curve points");
    bottomPanel.add(showHideCurvePoints, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    showHideCurvePoints.setEnabled(true);
    showHideCurvePoints.setVisible(true);
    showHideCurvePoints.setSelected(true);
    showHideCurvePoints.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.setShowCurveData(showHideCurvePoints.isSelected());
          movePointsButton.setEnabled(showHideCurvePoints.isSelected());
          deviationPanel.repaint();
        }
      });

    movePointsButton.setText("Move Points");
    movePointsButton.setToolTipText("<html>Move the points of the deviation curve (red)<br>on the calculated one (yellow)</html>");
    bottomPanel.add(movePointsButton, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 3, 0, 3), 0, 0));
    bottomPanel.add(printCheckBox, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    movePointsButton.setEnabled(true);
    movePointsButton.setVisible(true);
    movePointsButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deviationPanel.stickPointsToCurve();
          deviationPanel.repaint();
        }
      });

    printCheckBox.setText("Print");
    printCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          printCheckBox_actionPerformed(e);
        }
      });
    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void loadDataPointsForDeviation(ArrayList<double[]> dp) 
        {
          setDataPoint(dp);
        }
      });
    
  }

  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    showHideDataPoints.setEnabled(deviationPanel.getDataPoint() != null);
    showHideDataPoints.setVisible(deviationPanel.getDataPoint() != null);

    if (deviationPanel.getDataPoint() != null && showHideDataPoints.isSelected())
    {
      suggestButton.setEnabled(true);
      suggestButton.setVisible(true);
//    sprayCheckBox.setEnabled(true);      
//    sprayCheckBox.setVisible(true);
//    deleteCheckBox.setEnabled(true);
//    deleteCheckBox.setVisible(true);
    }
    
    else
    {
      resetButton.setEnabled(true);
      resetButton.setVisible(true);
    }
  }
  
  public void setHt(Hashtable<Double, Double> ht)
  {
    deviationPanel.setHtDeviationCurve(ht);
  }

  public Hashtable<Double, Double> getHt()
  {
    return deviationPanel.getHtDeviationCurve();
  }
  
  public void setDataCheckBoxVisible(boolean b)
  {
    showHideDataPoints.setVisible(b);
    showHideDataPoints.setEnabled(b);                                 
  }
  
  public void setDataPoint(ArrayList<double[]> dataPoint)
  {
    deviationPanel.setDataPoint(dataPoint);
    suggestButton.setEnabled(dataPoint != null);
    suggestButton.setVisible(dataPoint != null);
    sprayCheckBox.setEnabled(dataPoint != null);
    sprayCheckBox.setVisible(dataPoint != null);
    deleteCheckBox.setEnabled(dataPoint != null);
    deleteCheckBox.setVisible(dataPoint != null);
  }

  private void suggestButton_actionPerformed(ActionEvent e)
  {
    deviationPanel.suggestCurve();
  }
  
  private void resetButton_actionPerformed(ActionEvent e)
  {
    String deviationFileName = (String) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION_FILE);
    NMEAContext.getInstance().setDeviation(Utils.loadDeviationCurve(deviationFileName));
    Hashtable<Double, Double> data = Utils.loadDeviationHashtable(deviationFileName); // Load from file
    deviationPanel.setHtDeviationCurve(data);
    deviationPanel.resetSprayedPoints();
    deviationPanel.repaint();
  }

  private void printCheckBox_actionPerformed(ActionEvent e)
  {
    printVersion = printCheckBox.isSelected();
    deviationPanel.setPrintVersion(printVersion);
    deviationPanel.repaint();
  }
}