package nmea.ui.viewer;

import nmea.server.ctx.NMEAContext;

import nmea.event.NMEAListener;

import java.awt.BorderLayout;

import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.ArrayList;

import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import nmea.server.constants.Constants;

public class BulkPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JEditorPane bulkEditorPane = new JEditorPane()
    {
      public void paintComponent(Graphics gr)
      {
        ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
        ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                          RenderingHints.VALUE_ANTIALIAS_ON);      
        super.paintComponent(gr);
        // Calculate nb lines
        int fontSize = this.getFont().getSize();
        int height   = this.getHeight();
        nbLines  = height / fontSize;
//      System.out.println("nblines:" + nbLines);
      }
    };

  int nbLines = 0;
  List<String> sentences = new ArrayList<String>(10);
  
  public BulkPanel()
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
    this.setLayout(borderLayout1);
    this.add(bulkEditorPane, BorderLayout.CENTER);

    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
     {
       public void manageNMEAString(String str) 
       {
         // Add the new loine to the panel.
         sentences.add(str);
         while (sentences.size() > nbLines)
           sentences.remove(0);

         StringBuffer content = new StringBuffer();
         for (String s : sentences)
           content.append(s + "\n");
         bulkEditorPane.setText(content.toString());
       }
     });
  }  
}
