package nmea.ui.viewer.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.FontFormatException;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.Point;
import java.awt.RenderingHints;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class JumboDisplay
  extends JPanel
{
  private static JumboDisplay instance = null;
  private Font digiFont = null;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel dataNameLabel = new JLabel();
  private JLabel dataValueLabel = new JLabel();
  private Color displayColor = Color.green;
  
  private String toolTipText = null;
  
  private String origName = "BSP", 
                 origValue = "00.00";

  public JumboDisplay(String name, String value)
  {
    instance = this;
    origName = name;
    origValue = value;
  }
  public JumboDisplay(String name, String value, String ttText)
  {
    this(name, value, ttText, 36);
  }
  public JumboDisplay(String name, String value, String ttText, int basicSize)
  {
    instance = this;
    origName = name;
    origValue = value;
    toolTipText = ttText;
    this.jumboFontSize = basicSize;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private int jumboFontSize = 36;

  private void jbInit()
    throws Exception
  {
    try { digiFont = tryToLoadFont("ds-digi.ttf", this); }
    catch (Exception ex) { System.err.println(ex.getMessage()); }
    if (digiFont == null)
      digiFont = new Font("Courier New", Font.BOLD, jumboFontSize);
    else
      digiFont = digiFont.deriveFont(Font.BOLD, jumboFontSize);
    digiFont = loadDigiFont();
    this.setLayout(gridBagLayout1);
    this.setBackground(Color.black);
    
    resize(jumboFontSize);
    
//  this.setSize(new Dimension(120, 65));
//  this.setPreferredSize(new Dimension(120, 65));
    dataNameLabel.setText(origName);
    dataValueLabel.setText(origValue);
    if (toolTipText != null)
      this.setToolTipText(toolTipText);
    this.add(dataNameLabel, new GridBagConstraints(0, 0, 1, 1, 10.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    this.add(dataValueLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 3), 0, 0));
    
//  dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, 20));
    dataNameLabel.setForeground(displayColor);
    dataNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
//  dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, 40));
    dataValueLabel.setForeground(displayColor);
    dataValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dataValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
  }
  
  public void resize(int bigFontSize)
  {
    jumboFontSize  = bigFontSize;
    int width = (int)(120d * (double)jumboFontSize / 36d);
    int height = (int)(65d * (double)jumboFontSize / 36d);
    this.setSize(new Dimension(width, height));
    this.setPreferredSize(new Dimension(width, height));
    int big   = (int)(40d * jumboFontSize / 36d);
    int small = (int)(20d * jumboFontSize / 36d);
    dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, small));
    dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, big));    
  }

  private Font loadDigiFont()
  {
    Font f = null;
    try { f = tryToLoadFont("ds-digi.ttf", instance); }
    catch (Exception ex) { System.err.println(ex.getMessage()); }
    if (f == null)
      f = new Font("Courier New", Font.BOLD, jumboFontSize);
    else
      f = f.deriveFont(Font.BOLD, jumboFontSize);
    return f;
  }

  public static Font tryToLoadFont(String fontName, Object parent) 
  {
    final String RESOURCE_PATH = "resources" + "/"; // A slash! Not File.Separator, it is a URL.
    try 
    {
      String fontRes = RESOURCE_PATH + fontName;
      InputStream fontDef = parent.getClass().getResourceAsStream(fontRes);
      if (fontDef == null) 
      {
        throw new NullPointerException("Could not find font resource \"" + fontName +
                                       "\"\n\t\tin \"" + fontRes +
                                       "\"\n\t\tfor \"" + parent.getClass().getName() +
                                       "\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
      } 
      else
        return Font.createFont(Font.TRUETYPE_FONT, fontDef);
    } 
    catch (FontFormatException e) 
    {
      System.err.println("getting font " + fontName);
      e.printStackTrace();
    } 
    catch (IOException e) 
    {
      System.err.println("getting font " + fontName);
      e.printStackTrace();
    }
    return null;
  }
  
  public void setName(String s)
  {
    dataNameLabel.setText(s);
  }
  
  public void setValue(String s)
  {
    dataValueLabel.setText(s);
  }
  
  public void setDisplayColor(Color c)
  {
    displayColor = c;
    dataNameLabel.setForeground(displayColor);
    dataValueLabel.setForeground(displayColor);
  }
  public void paintComponent(Graphics g)
  {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);      
 // resize(jumboFontSize);
    
//    Dimension dim =  this.getSize();
//    System.out.println("Dim:" + dim.getWidth() + "x" + dim.getHeight());
    if (false)
    {
      Color startColor = Color.black; // new Color(255, 255, 255);
      Color endColor   = Color.gray; // new Color(102, 102, 102);
  //  GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor); // Diagonal, top-left to bottom-right
  //  GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
  //  GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical
      
      GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      ((Graphics2D)g).setPaint(gradient);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
    if (true)
      drawGlossyRectangularDisplay((Graphics2D)g, 
                                   new Point(0, 0), 
                                   new Point(this.getWidth(), this.getHeight()), 
                                   Color.gray, 
                                   Color.black, 
                                   1f);
  }

  private static void drawGlossyRectangularDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
    
    int width  = bottomRight.x - topLeft.x;
    int height = bottomRight.y - topLeft.y;

    g2d.fillRoundRect(topLeft.x , topLeft.y, width, height, 10, 10);

    Point gradientOrigin = new Point(0, //topLeft.x + (width) / 2,
                                     0);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (height / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    int offset = (int)(width * 0.025);
    int arcRadius = 5;
    g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius);
    
    g2d.setColor(Color.lightGray);
    Composite comp = g2d.getComposite();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
    for (int n=1; n<4; n++)
      g2d.drawLine(0, n * (height / 4), width, n * (height / 4));
    for (int n=1; n<6; n++)
      g2d.drawLine(n * (width / 6), 0, n * (width / 6), height);
    
    g2d.setComposite(comp);
  }
}
