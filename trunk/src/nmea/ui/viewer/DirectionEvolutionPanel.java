package nmea.ui.viewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import java.util.Date;

import javax.swing.JPanel;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;

import nmea.event.NMEAListener;

import nmea.ui.viewer.elements.DirectionEvolutionDisplay;

import ocss.nmea.parser.Angle360;


public class DirectionEvolutionPanel
     extends JPanel  
{

  private DirectionEvolutionDisplay hdgLoggingDisplay = new DirectionEvolutionDisplay("HDG", "Heading", 36);
  private DirectionEvolutionDisplay cogLoggingDisplay = new DirectionEvolutionDisplay("COG", "Course Over Ground", 36);
  private DirectionEvolutionDisplay twdLoggingDisplay = new DirectionEvolutionDisplay("TWD", "True Wind Direction", 36);
  private DirectionEvolutionDisplay cdrLoggingDisplay = new DirectionEvolutionDisplay("CDR", "Current Direction", 36);
  
  private FlowLayout flowLayout1 = new FlowLayout();

  public DirectionEvolutionPanel()
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
    this.setLayout(flowLayout1);
    this.setSize(new Dimension(450, 430));
    this.add(hdgLoggingDisplay, null); // new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(cogLoggingDisplay, null); // new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(twdLoggingDisplay, null); // new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(cdrLoggingDisplay, null); // new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

    Dimension dim = new Dimension(DirectionEvolutionDisplay.DEFAULT_WIDTH, this.getHeight());
    hdgLoggingDisplay.setPreferredSize(dim);
    cogLoggingDisplay.setPreferredSize(dim);
    twdLoggingDisplay.setPreferredSize(dim);
    cdrLoggingDisplay.setPreferredSize(dim);

    NMEAContext.getInstance().addNMEAListener(new NMEAListener()
      {
        public void dataUpdate()
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try
            {
              hdgLoggingDisplay.addValue(new Date(), ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue());
              hdgLoggingDisplay.repaint();
            }
            catch (Exception ex)
            {
            }
            try
            {
              cogLoggingDisplay.addValue(new Date(), ((Angle360) cache.get(NMEADataCache.COG)).getValue());
              cogLoggingDisplay.repaint();
            }
            catch (Exception ex)
            {
            }
            try
            {
              twdLoggingDisplay.addValue(new Date(), ((Angle360) cache.get(NMEADataCache.TWD)).getValue());
              twdLoggingDisplay.repaint();
            }
            catch (Exception ex)
            {
            }
            try
            {
              cdrLoggingDisplay.addValue(new Date(), ((Angle360) cache.get(NMEADataCache.CDR)).getValue());
              cdrLoggingDisplay.repaint();
            }
            catch (Exception ex)
            {
            }
          }
        }
      });
  }
  
  public void paintComponent(Graphics gr)
  {
    Dimension dim = new Dimension(DirectionEvolutionDisplay.DEFAULT_WIDTH, this.getHeight());
    hdgLoggingDisplay.setPreferredSize(dim);
    cogLoggingDisplay.setPreferredSize(dim);
    twdLoggingDisplay.setPreferredSize(dim);
    cdrLoggingDisplay.setPreferredSize(dim);
     
/*  hdgLoggingDisplay.repaint();
    cogLoggingDisplay.repaint();
    twdLoggingDisplay.repaint();
    cdrLoggingDisplay.repaint(); */
  }
}
