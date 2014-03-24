
package nmea.ui.viewer.spot;

import astro.calc.GeoPoint;

import coreutilities.Utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.TimeZone;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.SVData;

import user.util.GeomUtil;

public class SpotExtraData
  extends javax.swing.JPanel
{
  private final static DecimalFormat DF2 = new DecimalFormat("##00");
  private final static DecimalFormat DF21 = new DecimalFormat("##00.0");
  private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-YYYY HH:mm");
  static 
  {
    SDF.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
  }
  private transient List<SpotLine> parsedSpotLines = null;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane = null; // new JScrollPane();
  private GeoPos spotPos = null;
  private JPanel dataPanel = new JPanel()
    {
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
          if (parsedSpotLines != null)
          {
            gr.setColor(Color.red);
            Font f = gr.getFont();
            gr.setFont(f.deriveFont(Font.BOLD));
            
            gr.drawString(parsedSpotLines.size() + " Spot line(s)", 5, 20);
            // Display All Data Values:
            List<Object[]> dataTable = new ArrayList<Object[]>();
            dataTable.add(new Object[] {"UTC", "hPa", "kts", "\272", "mm/h"}); // Titles
            for (SpotLine sl : parsedSpotLines)
            {
              dataTable.add(new Object[] { SDF.format(sl.getDate()),
                                           DF2.format(sl.getPrmsl()), 
                                           DF21.format(sl.getTws()), 
                                           DF2.format(sl.getTwd()), 
                                           DF21.format(sl.getRain()) });
            }
            gr.setColor(Color.red);
            // Position and GRID
            try
            {
              GeoPos pos = (GeoPos)NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
              if (spotPos != null)
                pos = spotPos;
              if (pos != null)
              {
                int x = (this.getWidth() / 2) - 20;
                gr.drawString("Position:" + pos.toString(), x, 20);
                gr.drawString("Square GRID:" + GeomUtil.gridSquare(pos.lat, pos.lng), x, 34);
              }
            }
            catch (Exception ex)
            {
              // No Cache
            }
            //
            String[][] data = new String[dataTable.size()][5];
            int i = 0;
            for (Object[] line : dataTable)
            {
              for (int j=0; j<5; j++)
                data[i][j] = (String)line[j];
              i++;
            }
            int x = 5;
            int y = 40;
            gr.setColor(Color.white);        
            int ord = Utilities.drawPanelTable(data, 
                                               gr, 
                                               new Point(x + 10, y + 20 + gr.getFont().getSize() + 2), 
                                               10, 
                                               2, 
                                               new int[] { Utilities.CENTER_ALIGNED, 
                                                           Utilities.RIGHT_ALIGNED, 
                                                           Utilities.RIGHT_ALIGNED, 
                                                           Utilities.RIGHT_ALIGNED, 
                                                           Utilities.RIGHT_ALIGNED }, 
                                               true, 
                                               Color.cyan,
                                               Color.blue,
                                               0.35f,
                                               0.9f);        
            this.setPreferredSize(new Dimension(200, ord));
            gr.setFont(f);
          }
        }
        catch (Exception ex)
        {
          // No cache yet
        }
      }
    };

  public SpotExtraData()
  {
    jbInit();
  }

  private void jbInit()
  {
    this.setLayout(borderLayout1);
    this.setPreferredSize(new Dimension(200, 600));
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener()
      {
        public void newSpotData(List<SpotLine> spotLines, GeoPos pos)
        {
          parsedSpotLines = spotLines;
          spotPos = pos;
          repaint();
        }
      });
    jScrollPane = new JScrollPane(dataPanel);
//  jScrollPane.getViewport().add(dataPanel, null);
    this.add(jScrollPane, BorderLayout.CENTER);
    dataPanel.setPreferredSize(new Dimension(200, 500));
  }
}
