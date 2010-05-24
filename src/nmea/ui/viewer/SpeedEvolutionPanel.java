package nmea.ui.viewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.Date;

import javax.swing.JPanel;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;

import nmea.event.NMEAListener;

import nmea.ui.viewer.elements.SpeedEvolutionDisplay;

import ocss.nmea.parser.Speed;

import oracle.jdeveloper.layout.VerticalFlowLayout;


public class SpeedEvolutionPanel
  extends JPanel
{
  private VerticalFlowLayout verticalFlowLayout = new VerticalFlowLayout();

  private SpeedEvolutionDisplay bspLoggingDisplay = new SpeedEvolutionDisplay("BSP", "Boat Speed", 36);
  private SpeedEvolutionDisplay twsLoggingDisplay = new SpeedEvolutionDisplay("TWS", "True Wind Speed", 36);
  private SpeedEvolutionDisplay sogLoggingDisplay = new SpeedEvolutionDisplay("SOG", "Speed Over Ground", 36);
  private SpeedEvolutionDisplay cspLoggingDisplay = new SpeedEvolutionDisplay("CSP", "Current Speed", 36);

  public SpeedEvolutionPanel()
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
    this.setLayout(verticalFlowLayout);
    this.setSize(new Dimension(450, 350));
    this.add(bspLoggingDisplay, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(twsLoggingDisplay, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(sogLoggingDisplay, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(cspLoggingDisplay, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

    bspLoggingDisplay.setMax(10d);
    
    twsLoggingDisplay.setMax(70d);
    twsLoggingDisplay.setStep(10d);
    
    sogLoggingDisplay.setMax(10d);
    cspLoggingDisplay.setMax(5d);

    NMEAContext.getInstance().addNMEAListener(new NMEAListener()
    {
      public void dataUpdate() 
      {
        NMEADataCache cache = NMEAContext.getInstance().getCache();
        if (cache != null)
        {
          try 
          { 
            bspLoggingDisplay.addValue(new Date(), ((Speed)cache.get(NMEADataCache.BSP)).getValue()); 
            bspLoggingDisplay.repaint();
          } 
          catch (Exception ex) {}
          try 
          { 
            twsLoggingDisplay.addValue(new Date(), ((Speed)cache.get(NMEADataCache.TWS)).getValue()); 
            twsLoggingDisplay.repaint();
          } 
          catch (Exception ex) {}
          try 
          { 
            sogLoggingDisplay.addValue(new Date(), ((Speed)cache.get(NMEADataCache.SOG)).getValue()); 
            sogLoggingDisplay.repaint();
          } 
          catch (Exception ex) {}
          try 
          { 
            cspLoggingDisplay.addValue(new Date(), ((Speed)cache.get(NMEADataCache.CSP)).getValue()); 
            cspLoggingDisplay.repaint();
          } 
          catch (Exception ex) {}
        }
      }
    });
  }
}
