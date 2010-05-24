package nmea.ui.deviation;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;
import nmea.ctx.Utils;

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
  private JButton resetButton = new JButton();
  private JButton zoomInButton = new JButton();
  private JButton zoomOutButton = new JButton();
  private JCheckBox showHideDataPoints = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  
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
    bottomPanel.setLayout(gridBagLayout1);
    this.add(deviationPanel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    suggestButton.setText("Suggest");
    suggestButton.setToolTipText("From logged data");
    bottomPanel.add(suggestButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 3, 0, 3), 0, 0));
    suggestButton.setEnabled(false);
    suggestButton.setVisible(false);
    suggestButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          suggestButton_actionPerformed(e);
        }
      });
    resetButton.setText("Reset");
    resetButton.setToolTipText("From the file");
    bottomPanel.add(resetButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 3, 0, 3), 0, 0));
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
    bottomPanel.add(zoomInButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 3, 0, 3), 0, 0));
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
    bottomPanel.add(zoomOutButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 3, 0, 3), 0, 0));
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
    bottomPanel.add(showHideDataPoints, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 3, 0, 3), 0, 0));
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
    if (dataPoint != null)
    {
      suggestButton.setEnabled(true);
      suggestButton.setVisible(true);
//    resetButton.setEnabled(false);
//    resetButton.setVisible(false);
    }
    else
    {
      suggestButton.setEnabled(false);
      suggestButton.setVisible(false);
//    resetButton.setEnabled(true);
//    resetButton.setVisible(true);
    }
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
    deviationPanel.repaint();
  }
}
