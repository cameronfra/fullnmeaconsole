package nmea.ui.viewer.spot;

import coreutilities.gui.SpeedoPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;

import java.util.Date;
import java.util.List;

import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;

import nmea.ui.viewer.elements.CurrentDisplay;
import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;


public class SpotParserPanel
     extends JPanel
{
  private final static DecimalFormat DF2 = new DecimalFormat("00");
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel parsedDataPanel = new JPanel();
  private SpeedoPanel twsSpeedoPanel = null;
  private CurrentDisplay twdDisplay = null;
  private WindGaugePanel windGauge = null;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private SpotCanvas spotCanvas = new SpotCanvas();
  private transient List<SpotLine> spotLines = null;
  private JPanel controlPanel = new JPanel();
  private JCheckBox rawCheckBox = new JCheckBox();
  private JCheckBox smoothCheckBox = new JCheckBox();
  private JComboBox timeZoneComboBox = new JComboBox();

  public SpotParserPanel()
  {
    jbInit();
  }

  private void jbInit()
  {
    this.setLayout(borderLayout1);
    this.setBackground(Color.black);
    this.setSize(new Dimension(525, 300));
    parsedDataPanel.setBackground(Color.black);
//  spotScrollPane.setMaximumSize(new Dimension(32767, 37));
    parsedDataPanel.setLayout(gridBagLayout1);
    this.add(parsedDataPanel, BorderLayout.CENTER);
    
    windGauge = new WindGaugePanel();
    windGauge.setPreferredSize(new Dimension(50, 100));

    rawCheckBox.setText("Raw Data");
    rawCheckBox.setSelected(true);
    rawCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        rawCheckBox_actionPerformed(e);
      }
    });
    smoothCheckBox.setText("Smooth Data");
    smoothCheckBox.setSelected(true);
    smoothCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        smoothCheckBox_actionPerformed(e);
      }
    });
    timeZoneComboBox.removeAllItems();
    String[] tzIDs = TimeZone.getAvailableIDs();
    for (String tz : tzIDs)
      timeZoneComboBox.addItem(tz);
    timeZoneComboBox.setSelectedItem("Etc/UTC");
    timeZoneComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        timeZoneComboBox_actionPerformed(e);
      }
    });
    parsedDataPanel.add(windGauge,
                        new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    
    double maxTWS = Double.parseDouble(System.getProperty("max.analog.tws", "50"));
    System.out.println("Setting MAX BSP to " + maxTWS);
    twsSpeedoPanel = new SpeedoPanel(maxTWS, false);
    twsSpeedoPanel.withBeaufortScale(true);

    twsSpeedoPanel.setPreferredSize(new Dimension(200, 120));
    twsSpeedoPanel.setLabel("TWS");
    parsedDataPanel.add(twsSpeedoPanel,
                        new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    twdDisplay = new CurrentDisplay("TWD", "000", "True Wind");
    twdDisplay.setDisplayColor(Color.cyan);
    twdDisplay.setBackground(Color.black);
    parsedDataPanel.add(twdDisplay,
                        new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));

    parsedDataPanel.add(spotCanvas,
                        new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                               new Insets(10, 5, 10, 5), 0, 0));
    controlPanel.add(rawCheckBox, null);
    controlPanel.add(smoothCheckBox, null);
    controlPanel.add(timeZoneComboBox, null);
    parsedDataPanel.add(controlPanel,
                        new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener()
      {
        public void newSpotData(List<SpotLine> spotLines, GeoPos pos)
        {
          setSpotLines(spotLines);
          if (spotLines != null && spotLines.size() > 0)
          {
            // Display data
            if (spotLines.size() > 0)
            {
              windGauge.setTws(10 * (float)spotLines.get(0).getRain());
              windGauge.setToolTipText("Rain:" + DF2.format(spotLines.get(0).getRain()) + " mm/h");
              twsSpeedoPanel.setSpeed(spotLines.get(0).getTws());
              twdDisplay.setDirection(spotLines.get(0).getTwd());
              twdDisplay.setAngleValue(spotLines.get(0).getTwd());
              // Set min max TWS
              double maxWind = 0D;
              double minWind = Double.MAX_VALUE;
              for (SpotLine sp : spotLines)
              {
                double tws = sp.getTws();
                maxWind = Math.max(maxWind, tws);
                minWind = Math.min(minWind, tws);
              }
              twsSpeedoPanel.setMinMax(minWind, maxWind);
            }
            repaint();
          }
          else
            twsSpeedoPanel.resetMinMax();
        }

        public void setSpotLineIndex(int i) 
        {
          if (spotLines != null)
          {
            // Display data
            if (spotLines.size() > 0)
            {
              windGauge.setTws(10 * (float)spotLines.get(i).getRain());
              windGauge.setToolTipText("Rain:" + DF2.format(spotLines.get(i).getRain()) + " mm/h");
              twsSpeedoPanel.setSpeed(spotLines.get(i).getTws());
              twdDisplay.setDirection(spotLines.get(i).getTwd());
              twdDisplay.setAngleValue(spotLines.get(i).getTwd());
            }
            repaint();
          }
        }

      });
  }

  private void setSpotLines(List<SpotLine> spotLines)
  {
    this.spotLines = spotLines;
  }
  
  private void rawCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithRawData(rawCheckBox.isSelected());
  }

  private void smoothCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithSmoothData(smoothCheckBox.isSelected());
  }

  private void timeZoneComboBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setTimeZone((String)timeZoneComboBox.getSelectedItem());
  }
}
