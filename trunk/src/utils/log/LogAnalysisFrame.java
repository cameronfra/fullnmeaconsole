package utils.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import utils.NMEAAnalyzer.ScalarValue;

public class LogAnalysisFrame
  extends JFrame
{
  private LogAnalysisPanel displayPanel = null;
  private transient LogAnalysis caller;
  private JScrollPane jScrollPane = null;
  private String title = "";

  public LogAnalysisFrame(LogAnalysis parent, String title, String unit)
  {
    this.caller = parent;
    this.title = title;
    displayPanel = new LogAnalysisPanel(unit);
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
    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(1000, 275));
    this.setTitle(this.title);
    
    displayPanel.setPreferredSize(new Dimension(1400, 275));
    jScrollPane = new JScrollPane(displayPanel);
//  this.getContentPane().add(displayPanel, BorderLayout.CENTER);
    this.getContentPane().add(jScrollPane, BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel();
    this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    JButton widerButton = new JButton("< >");
    widerButton.setToolTipText("Make the graph wider");
    bottomPanel.add(widerButton, null);
    JButton narrowerButton = new JButton("> <");
    narrowerButton.setToolTipText("Make the graph narrower");
    bottomPanel.add(narrowerButton, null);
    widerButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        adjustSize(WIDER);
      }
    });
    narrowerButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        adjustSize(NARROWER);
      }
    });
    
    final JCheckBox withRawData    = new JCheckBox("Raw Data");
    final JCheckBox withSmoothData = new JCheckBox("Smooth Data");
    bottomPanel.add(withRawData, null);
    bottomPanel.add(withSmoothData, null);
    withRawData.setSelected(true);
    withSmoothData.setSelected(true);
    withRawData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        displayPanel.setWithRawData(withRawData.isSelected());
        displayPanel.repaint();
      }
    });
    withSmoothData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        displayPanel.setWithSmoothData(withSmoothData.isSelected());
        displayPanel.repaint();
      }
    });
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
                           {
                             @Override
                             public void windowClosing(WindowEvent e) 
                             {
                               System.out.println(">>> Closing " + getTitle());
                               setVisible(false);
                             }
                           });
  }
  
  private final static int WIDER    = 0;
  private final static int NARROWER = 1;
  
  private void adjustSize(int mode)
  {
    Dimension dim = displayPanel.getSize();
    Dimension newDim = new Dimension(mode == WIDER ? (int)(dim.width * 1.1) : (int)(dim.width / 1.1), dim.height);
    displayPanel.setPreferredSize(newDim);
    displayPanel.setSize(newDim);
    displayPanel.repaint();
  }
  
  public void setLogData(Map<Date, ScalarValue> logdata, Map<Long, Calendar[]> riseAndSet, String tz)
  {
    displayPanel.setLogData(logdata, riseAndSet, tz);
  }
  
  public void setTimeZone(String tz)
  {
    displayPanel.setTimeZone(tz);
  }
}
