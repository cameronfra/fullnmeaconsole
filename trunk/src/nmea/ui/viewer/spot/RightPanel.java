
package nmea.ui.viewer.spot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.SVData;

public class RightPanel
  extends javax.swing.JPanel
{
  private SpotChartPanel chartPanel = new SpotChartPanel();
  private SpotExtraData  extraDataPanel = new SpotExtraData();
  
  public RightPanel()
  {
    jbInit();
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("Viewers", "Right Panel")
    {
      public void manageNMEAString(String str)
      {
    //      System.out.println("NMEA:" + str);
        if (str.trim().length() > 6 && str.startsWith("$"))
        {
          if (str.substring(3, 6).equals("GSV"))
          {
            try
            {
              if (NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW) != null)
              {
                repaint();
              }
            }
            catch (Exception ex)
            {
              // No cache yet
            }
          }
        }
      }
    });
  }

  private void jbInit()
  {
    this.setLayout(new BorderLayout());
    this.setPreferredSize(new Dimension(SpotChartPanel.CHART_WIDTH, 600));
    this.add(chartPanel, BorderLayout.NORTH);
    this.add(extraDataPanel, BorderLayout.CENTER);
  }  
}
