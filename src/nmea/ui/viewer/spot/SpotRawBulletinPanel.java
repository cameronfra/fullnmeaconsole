package nmea.ui.viewer.spot;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nmea.server.ctx.NMEAContext;

import nmea.ui.viewer.spot.utils.SpotParser;
import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;

public class SpotRawBulletinPanel
     extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JEditorPane spotBulletinEditorPane = new JEditorPane();
  private JScrollPane spotScrollPane = null;

  public SpotRawBulletinPanel()
  {
    jbInit();
  }

  private void jbInit()
  {
    this.setLayout(borderLayout1);
//  this.setBackground(Color.white);
    this.setToolTipText("Paste the SPOT Bulletin here, and tab out of the field.");
    spotBulletinEditorPane.setBorder(BorderFactory.createTitledBorder("SPOT Bulletin"));
    spotBulletinEditorPane.setFont(new Font("Source Code Pro", 0, 11));
    spotScrollPane = new JScrollPane();
    spotScrollPane.getViewport().add(spotBulletinEditorPane, null);

    spotBulletinEditorPane.getDocument().addDocumentListener(new DocumentListener()
      {
          public void insertUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }

          public void removeUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }

          public void changedUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }
        });    
    this.add(spotScrollPane, BorderLayout.CENTER);
  }
  
  public void paintComponent(Graphics gr)
  {    
  }
  
  private void parseContent(String str)
  {
    try
    {
      List<SpotLine> spotLines = SpotParser.parse(str);
      GeoPos spotPos = SpotParser.getSpotPos();
      System.out.println("SPOT Data parsed, " + spotLines.size() + " line(s).");
      // Broadcast parsed data
      NMEAContext.getInstance().fireNewSpotData(spotLines, spotPos);
    }
    catch (Exception ex)
    {
      System.out.println("Cannot parse the SPOT data...");
      ex.printStackTrace();
      NMEAContext.getInstance().fireNewSpotData(null, null);
    }
  }
}
