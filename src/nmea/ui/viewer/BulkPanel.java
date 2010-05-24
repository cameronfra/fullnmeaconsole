package nmea.ui.viewer;

import nmea.ctx.NMEAContext;

import nmea.event.NMEAListener;

import java.awt.BorderLayout;

import java.awt.Graphics;

import java.util.ArrayList;

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
        super.paintComponent(gr);
        // Calculate nb lines
        int fontSize = this.getFont().getSize();
        int height   = this.getHeight();
        nbLines  = height / fontSize;
//      System.out.println("nblines:" + nbLines);
      }
    };

  int nbLines = 0;
  ArrayList<String> sentences = new ArrayList<String>(10);
  
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
