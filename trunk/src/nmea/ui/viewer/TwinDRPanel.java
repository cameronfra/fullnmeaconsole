package nmea.ui.viewer;


import astro.calc.GeoPoint;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import javax.swing.border.BevelBorder;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.event.NMEAListener;

import nmea.server.constants.Constants;

import nmea.ui.viewer.elements.CurrentDisplay;
import nmea.ui.viewer.elements.DrawingBoard;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.Temperature;


public class TwinDRPanel
  extends JPanel
{
  private DeadReckoningPlottingSheet psOne = new DeadReckoningPlottingSheet(270, 270, 37d, -122d, 1d);
  private DeadReckoningPlottingSheet psTwo = new DeadReckoningPlottingSheet(270, 270, 37d, -122d, 1d);
  private DrawingBoard drawingBoard = new DrawingBoard();
  private CurrentDisplay currentDisplay = new CurrentDisplay("Current", "00.00", "Current", 30);
  private JPanel panelHolder = new JPanel(new BorderLayout());
  private JPanel bottomPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public TwinDRPanel()
  {
    super();
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
    this.setSize(new Dimension(884, 452));
    this.setLayout(gridBagLayout1);
    currentDisplay.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//  panelHolder.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    drawingBoard.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    panelHolder.add(drawingBoard, BorderLayout.CENTER);
    bottomPanel.add(currentDisplay);
    panelHolder.add(bottomPanel, BorderLayout.SOUTH);
    
    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try { currentDisplay.setDirection(((Angle360)cache.get(NMEADataCache.CDR)).getValue()); } catch (Exception ex) {}
            try { currentDisplay.setSpeed(((Speed)cache.get(NMEADataCache.CSP)).getValue()); } catch (Exception ex) {}            
            currentDisplay.repaint();
          }
        }
      });
    
//  psOne.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    psOne.getPlottingSheet().setChartBackGround(new GradientPaint(0, 0, 
                                                                  Color.white, 
                                                                  this.getWidth(), this.getHeight(), 
                                                                  Color.lightGray));
//  psTwo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    psTwo.getPlottingSheet().setChartBackGround(new GradientPaint(0, 0, 
                                                                  Color.white, 
                                                                  this.getWidth(), this.getHeight(), 
                                                                  Color.lightGray));
    panelHolder.setPreferredSize(psOne.getPreferredSize());

    this.add(panelHolder, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(psOne, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(psTwo, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));

    if (false)
    {
      // Sample, on psOne
      GeoPoint[] groundData = new GeoPoint[]
        {
          new GeoPoint(37d,    -122.0),
          new GeoPoint(36.85d, -122.1),
          new GeoPoint(37.1d,  -121.9),
          new GeoPoint(37.25d,  -122.15),
          new GeoPoint(37.26d,  -121.95)
        };
      GeoPoint[] drData = new GeoPoint[]
        {
          new GeoPoint(37d,    -122.0),
          new GeoPoint(36.859d, -122.14),
          new GeoPoint(37.09d,  -121.87),
          new GeoPoint(37.25d,  -122.14),
          new GeoPoint(37.20d,  -121.90)
        };
      psOne.setGroundData(groundData);
      psOne.setDrData(drData);
      psOne.repaint();
    }
  }
  /*
  @Override
  protected void paintComponent(Graphics g)
  {
//  super.paintComponent(g);
    Dimension dim = new Dimension(this.getWidth() / 3, this.getWidth() / 3);
    psOne.getPlottingSheet().setPreferredSize(dim);
    psTwo.getPlottingSheet().setPreferredSize(dim);
    drawingBoard.setPreferredSize(dim);
  }
*/
}
