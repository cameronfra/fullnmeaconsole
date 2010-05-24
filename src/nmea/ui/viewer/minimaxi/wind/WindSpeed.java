package nmea.ui.viewer.minimaxi.wind;

import nmea.ctx.NMEAContext;

import nmea.ctx.NMEADataCache;

import nmea.event.NMEAListener;

import nmea.ui.viewer.elements.JumboDisplay;

import nmea.ui.viewer.minimaxi.MinMaxPanelInterface;

import nmea.ui.viewer.minimaxi.GaugePanel;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.Graphics2D;

import java.text.DecimalFormat;

import java.util.Date;

import javax.swing.JPanel;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Speed;

public class WindSpeed
  extends JPanel
implements MinMaxPanelInterface
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private GaugePanel wgp = null;
  private JumboDisplay   tws = null;

  private int jumboFontSize = 36;
  
  private double minimum =  Double.MAX_VALUE;
  private double maximum = -Double.MAX_VALUE;
  
  private Date minDate = null;
  private Date maxDate = null;
                                // Beaufort Scale 0   1   2   3    4    5    6    7    8    9   10   11   12
  public final static double[] BEAUFORT_SCALE = { 0d, 1d, 4d, 7d, 11d, 16d, 22d, 28d, 34d, 41d, 48d, 56d, 64d };

  public WindSpeed(int refSize)
  {
    jumboFontSize = refSize;
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
    wgp = new GaugePanel(this, 0d, 70d, new DecimalFormat("00.00 'kts'"), new Color(128, 0, 0), Color.white, Color.black)
      {
//      public void paintComponent(Graphics gr)
        public void paintBackGround(Graphics gr)
        {
//        super.paintComponent(gr);
          super.paintBackGround(gr);
          int fontSize = gr.getFont().getSize();
          
          Graphics2D g2d = (Graphics2D)gr;
          // Print beaufort scale
          gr.setColor(Color.red);
          int h = this.getHeight() - fontSize;
          for (int b=0; b<BEAUFORT_SCALE.length; b++)
          {
            int y = (int)(h + (fontSize / 2) - ((BEAUFORT_SCALE[b] / (maxValue - minValue)) * h));
            String str = /* "F " + */ Integer.toString(b);
            int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
            gr.drawString(str, this.getWidth() - strWidth, y + (gr.getFont().getSize() / 2));
//          y = (int)(h + (fontSize / 2) - ((beaufortScale[b] / (maxValue - minValue)) * h));
//          g2d.drawLine(3 * this.getWidth() / 4, y, this.getWidth() - strWidth - strOffset, y);
            g2d.drawLine(this.getWidth() / 2, y, this.getWidth() - strWidth - strOffset, y);
          }
          // Print speed in knots
          for (int s=0; s<=70; s += 10)
          {
            int y = (int)(h + (fontSize / 2) - ((s / (maxValue - minValue)) * h));
            String str = Integer.toString(s) + " kts";
            int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
            gr.drawString(str, 2, y + (gr.getFont().getSize() / 2));
//          y = (int)(h + (fontSize / 2) - ((beaufortScale[b] / (maxValue - minValue)) * h));
            g2d.drawLine(strWidth + 4, y, this.getWidth() / 2, y);
          }
          
          // Find current beaufort
          int currentBeaufort = BEAUFORT_SCALE.length;
          for (int b=0; b<BEAUFORT_SCALE.length; b++)
          {
            if (value < BEAUFORT_SCALE[b])
            {
              currentBeaufort = b;
              break;
            }
          }
          try
          {
            setToolTipText("<html>" + 
                           valueFormat.format(value) + " = " + Integer.toString(currentBeaufort - 1) + " Beaufort" + "<br>" +
                           "Min at " + minDate.toString() + "<br>" +
                           "Max at " + maxDate.toString() + 
                           "</html>");
          }
          catch (Exception ex)
          {
            System.err.println("Managed:" + ex.toString());            
          }
        }        
      }; 
    this.setLayout(borderLayout1);
    resize(jumboFontSize);
    this.add(wgp, BorderLayout.CENTER);
    tws = new JumboDisplay("TWS min-max", "00.00", "True Wind Speed", jumboFontSize);
    this.add(tws, BorderLayout.SOUTH);
    tws.setDisplayColor(Color.orange);
    
    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        @Override
        public void dataUpdate()
        {
          double trueWS = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.TWS)).getValue();
          if (trueWS != -Double.MAX_VALUE && !Double.isInfinite(trueWS))
          {
  //        wgp.setValue(trueWS);          
            tws.setValue(NMEAContext.DF22.format(trueWS));
            if (trueWS > maximum) 
            {
              maximum = trueWS;
              maxDate = new Date();
            }
            if (trueWS < minimum) 
            {
              minimum = trueWS;
              minDate = new Date();
            }
            wgp.setValues(trueWS, maximum, minimum);
          }
        }
      });
  }

  public void resize(int bigFontSize)
  {
    jumboFontSize  = bigFontSize;
    int width = (int)(120d * (double)jumboFontSize / 36d);
    int height = (int)(200d * (double)jumboFontSize / 36d);
    this.setSize(new Dimension(width, height));
    this.setPreferredSize(new Dimension(width, height));
  }
  
  public void paintBackGround(Graphics gr)
  {
    wgp.paintBackGround(gr);
  }
  
  public void reset()
  {
    minimum = Double.MAX_VALUE;
    maximum = -Double.MAX_VALUE;
  }
}
