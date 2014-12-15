
package nmea.ui.viewer.gsv;

import astro.calc.GeoPoint;

import coreutilities.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.SVData;

import user.util.GeomUtil;

public class GSVExtraData
  extends javax.swing.JPanel
{
  private final static DecimalFormat DF2 = new DecimalFormat("00");

  public GSVExtraData()
  {
    jbInit();
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("GPS", "GSV")
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
    this.setLayout(null);
    this.setPreferredSize(new Dimension(200, 600));
  }
  
  public void paintComponent(Graphics gr)
  {    
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);  
    gr.setColor(Color.white);
    gr.fillRect(0, 0, this.getWidth(), this.getHeight());
    try
    {
      if (NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW) != null)
      {
        Map<Integer, SVData> hm = ( Map<Integer, SVData>)NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW);
        
        gr.setColor(Color.red);
        Font f = gr.getFont();
        gr.setFont(f.deriveFont(Font.BOLD));
        gr.drawString(hm.size() + " Satellites in view", 5, 20);
        int inUse = 0;
        // Display All Data Values:
        List<Object[]> dataTable = new ArrayList<Object[]>();
        dataTable.add(new Object[] {"Sat.#", "El.(\272)", "Z(\272)", "SNR(db)"}); // Titles
        for (Integer sn : hm.keySet())
        {
          SVData svd = hm.get(sn);
          int snr = svd.getSnr();
          if (snr > 0)
            inUse++;
          dataTable.add(new Object[] { DF2.format(svd.getSvID()), 
                                       Integer.toString(svd.getElevation()), 
                                       Integer.toString(svd.getAzimuth()), 
                                       Integer.toString(svd.getSnr()) });
        }
        gr.setColor(Color.red);
        gr.drawString(inUse + " Satellites in use", 5, 34);
        // Position and GRID
        try
        {
          GeoPos pos = (GeoPos)NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
          if (pos != null)
          {
            int x = this.getWidth() / 2;
            gr.drawString("Position:" + pos.toString(), x, 20);
            gr.drawString("Square GRID:" + GeomUtil.gridSquare(pos.lat, pos.lng), x, 34);
          }
        }
        catch (Exception ex)
        {
          // No Cache
        }
        //
        String[][] data = new String[dataTable.size()][4];
        int i = 0;
        for (Object[] line : dataTable)
        {
          for (int j=0; j<4; j++)
            data[i][j] = (String)line[j];
          i++;
        }
        int x = 5;
        int y = 40;
        gr.setColor(Color.white);        
        Utilities.drawPanelTable(data, 
                                 gr, 
                                 new Point(x + 10, y + 20 + gr.getFont().getSize() + 2), 
                                 10, 
                                 2, 
                                 new int[] { Utilities.CENTER_ALIGNED, Utilities.RIGHT_ALIGNED, Utilities.RIGHT_ALIGNED, Utilities.RIGHT_ALIGNED }, 
                                 true, 
                                 Color.cyan,
                                 Color.blue,
                                 0.35f,
                                 0.9f);        

        gr.setFont(f);
      }
    }
    catch (Exception ex)
    {
      // No cache yet
    }
  }
}
