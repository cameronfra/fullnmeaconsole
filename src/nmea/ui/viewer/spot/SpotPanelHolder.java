
package nmea.ui.viewer.spot;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;

import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;

public class SpotPanelHolder
  extends JPanel
{
  private SpotParserPanel spotParserPanel;
  private SpotRawBulletinPanel spotBulletinPanel;
  private SpotComposerPanel composer;
  private JPanel helpPanel = new JPanel(); // TASK Implement that one...
  
  private JTabbedPane mainTabbedPanel;
  private RightPanel dataPanel;

  public SpotPanelHolder()
  {
    jbInit();
  }

  private void jbInit()
  {
    mainTabbedPanel = new JTabbedPane();
    composer = new SpotComposerPanel();
    spotParserPanel = new SpotParserPanel();
    spotBulletinPanel = new SpotRawBulletinPanel();
    dataPanel = new RightPanel();

    mainTabbedPanel.add("Compose",     composer);
    mainTabbedPanel.add("Raw Data",    spotBulletinPanel);
    mainTabbedPanel.add("Parsed Data", spotParserPanel);
    mainTabbedPanel.add("Help",        helpPanel);
    mainTabbedPanel.setEnabledAt(2, false);

    setLayout(new BorderLayout());

    add(mainTabbedPanel, java.awt.BorderLayout.CENTER);
    add(dataPanel, java.awt.BorderLayout.EAST);
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener()
      {
        public void newSpotData(List<SpotLine> spotLines, GeoPos pos)
        {
          mainTabbedPanel.setEnabledAt(2, (spotLines != null));
        }
      });
    
    helpPanel.setPreferredSize(new Dimension(500, 500));
    JEditorPane jEditorPane = new JEditorPane();
    JScrollPane jScrollPane = new JScrollPane();
    helpPanel.setLayout(new BorderLayout());
    jEditorPane.setEditable(false);
    jEditorPane.setFocusable(false);
    jEditorPane.setFont(new Font("Verdana", 0, 10));
    jEditorPane.setBackground(Color.lightGray);
    jScrollPane.getViewport().add(jEditorPane, null);

    try
    {
      jEditorPane.setPage(this.getClass().getResource("spot.help.html"));
      jEditorPane.repaint();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    helpPanel.add(jScrollPane, BorderLayout.CENTER);
  }
}
