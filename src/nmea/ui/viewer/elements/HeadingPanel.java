package nmea.ui.viewer.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;

import java.awt.Graphics2D;
import javax.swing.JPanel;

public class HeadingPanel 
     extends JPanel 
{
  private int hdg = 0;
  private boolean whiteOnBlack = true;
  private boolean draggable = true;
  private float roseWidth = 60;
  private boolean withNumber = true;
  private boolean withCardinalPoints = true;
  
  public HeadingPanel()
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
    this.setLayout( null );
    this.setSize(new Dimension(200, 50));
    this.setMinimumSize(new Dimension(200, 50));
    this.setPreferredSize(new Dimension(200, 50));
  }
  
  public void setValue(double d)
  {
    hdg = (int)d;
    repaint();
  }

  private static final boolean withColorGradient = true;
  
  public void paintComponent(Graphics gr)
  {    
    int w = this.getWidth();
    int h = this.getHeight();
    final int FONT_SIZE = 12;
    if (withColorGradient)
    {
      Color startColor = Color.black; // new Color(255, 255, 255);
      Color endColor   = Color.gray; // new Color(102, 102, 102);
      if (!whiteOnBlack)
      {
        startColor = Color.lightGray;
        endColor   = Color.white;
      }
//    GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor);
//    GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
//    GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical
      GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      ((Graphics2D)gr).setPaint(gradient);
    }
    else
    {
      gr.setColor(whiteOnBlack?Color.black:Color.white);
    }
    gr.fillRect(0, 0, w, h);
    // Width: 30 on each side = 60 (default)
    gr.setColor(whiteOnBlack?Color.white:Color.black);
    float oneDegree = (float)w / roseWidth; // 30 degrees each side
    // One graduation every 1 & 5, one label every 15
    for (int rose=hdg-(int)(roseWidth / 2f); rose<=hdg+(int)(roseWidth / 2f); rose++)
    {
      int roseToDisplay = rose;
      while (roseToDisplay >= 360) roseToDisplay -= 360;
      while (roseToDisplay < 0) roseToDisplay += 360;
      int abscisse = Math.round((float)(rose + (roseWidth / 2f) - hdg) * oneDegree);
//    System.out.println("(w=" + w + ") Abscisse for " + rose + "=" + abscisse);
      gr.drawLine(abscisse, 0, abscisse, 2);
      gr.drawLine(abscisse, h - 2, abscisse, h);
      if (rose % 5 == 0)
      {
        gr.drawLine(abscisse, 0, abscisse, 5);
        gr.drawLine(abscisse, h - 5, abscisse, h);
      }
      if (rose % 15 == 0)
      {
        Font f = gr.getFont();
        gr.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        String roseStr = Integer.toString(Math.round(roseToDisplay));
        if (withCardinalPoints)
        {
          if (roseToDisplay == 0)
            roseStr = "N";
          else if (roseToDisplay == 180)
            roseStr = "S";    
          else if (roseToDisplay == 90)
            roseStr = "E";    
          else if (roseToDisplay == 270)
            roseStr = "W";    
          else if (roseToDisplay == 45)
            roseStr = "NE";    
          else if (roseToDisplay == 135)
            roseStr = "SE";    
          else if (roseToDisplay == 225)
            roseStr = "SW";    
          else if (roseToDisplay == 315)
            roseStr = "NW";    
        }
//      System.out.println("String:" + roseStr);
        boolean cardinal = false;
        try { int x = Integer.parseInt(roseStr); } catch (NumberFormatException nfe) { cardinal = true; }
        if (withNumber || (cardinal && withCardinalPoints))
        {
          int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(roseStr);
          gr.drawString(roseStr, abscisse - strWidth / 2, (h / 2) + (FONT_SIZE / 2) );
        }
        gr.setFont(f);        
      }
    }    
    gr.setColor(Color.red);
    gr.drawLine(w/2, 0, w/2, h);
  }

  public void setHdg(int hdg)
  {
    this.hdg = hdg;
    repaint();
  }

  public int getHdg()
  {
    return hdg;
  }

  public void setWhiteOnBlack(boolean whiteOnBlack)
  {
    this.whiteOnBlack = whiteOnBlack;
  }

  public void setDraggable(boolean draggable)
  {
    this.draggable = draggable;
  }

  public void setRoseWidth(float roseWidth)
  {
    this.roseWidth = roseWidth;
  }

  public float getRoseWidth()
  {
    return roseWidth;
  }

  public void setWithNumber(boolean withNumber)
  {
    this.withNumber = withNumber;
  }

  public void setWithCardinalPoints(boolean withCardinalPoints)
  {
    this.withCardinalPoints = withCardinalPoints;
  }

  public boolean isWithCardinalPoints()
  {
    return withCardinalPoints;
  }
}
