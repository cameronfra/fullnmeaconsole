
package nmea.ui.viewer.gsv;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class GSVPanelHolder
  extends JPanel
{
  private GPSSatellitesPanel satPanel;
  private ExtraData dataPanel;

  public GSVPanelHolder()
  {
    jbInit();
  }

  private void jbInit()
  {
    satPanel = new GPSSatellitesPanel();
    dataPanel = new ExtraData();

    setLayout(new BorderLayout());

    add(satPanel, java.awt.BorderLayout.CENTER);
    add(dataPanel, java.awt.BorderLayout.EAST);
  }
}
