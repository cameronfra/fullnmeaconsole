package nmea.ui.viewer.elements;

import java.awt.AlphaComposite;

import nmea.server.utils.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.io.IOException;
import java.io.InputStream;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CurrentDisplay
  extends JPanel
{
  public static final NumberFormat speedFmt = new DecimalFormat("00.00");
  public static final NumberFormat angleFmt = new DecimalFormat("000");

  private static CurrentDisplay instance = null;
  private Font digiFont = null;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel dataNameLabel = new JLabel();
  private JLabel dataValueLabel = new JLabel();
  private Color displayColor = Color.green;

  private String toolTipText = null;

  private String origName = "Current", origValue = "00.00";

  private double direction = 0d;

  public CurrentDisplay(String name, String value)
  {
    instance = this;
    origName = name;
    origValue = value;
  }

  public CurrentDisplay(String name, String value, String ttText)
  {
    this(name, value, ttText, 36);
  }
  public CurrentDisplay(String name, String value, String ttText, int basicFontSize)
  {
    instance = this;
    origName = name;
    origValue = value;
    toolTipText = ttText;
    jumboFontSize = basicFontSize;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private int jumboFontSize = 20;

  private void jbInit()
    throws Exception
  {
    try
    {
      digiFont = tryToLoadFont("ds-digi.ttf", this);
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
    }
    if (digiFont == null)
      digiFont = new Font("Courier New", Font.BOLD, 20);
    else
      digiFont = digiFont.deriveFont(Font.BOLD, 20);
    digiFont = loadDigiFont();
    this.setLayout(gridBagLayout1);
//  this.setBackground(Color.lightGray);
    
    resize(jumboFontSize);
    
//  this.setSize(new Dimension(120, 120));
//  this.setPreferredSize(new Dimension(120, 120));
    dataNameLabel.setText(origName);
    dataValueLabel.setText(origValue);
    if (toolTipText != null)
      this.setToolTipText(toolTipText);
    //  this.add(dataNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(dataValueLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

//  dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, 20));
    dataNameLabel.setForeground(displayColor);
    dataNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
//  dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, 24)); // Was 40
    dataValueLabel.setForeground(displayColor);
    dataValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dataValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
  }

  public void resize(int bigFontSize)
  {
    jumboFontSize  = bigFontSize;
    int width  = (int)(120d * (double)jumboFontSize / 36d);
    int height = (int)(120d * (double)jumboFontSize / 36d);
    this.setSize(new Dimension(width, height));
    this.setPreferredSize(new Dimension(width, height));
    int big   = (int)(24d * jumboFontSize / 36d);
    int small = (int)(20d * jumboFontSize / 36d);
    dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, small));
    dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, big));    
  }

  private static Font loadDigiFont()
  {
    Font f = null;
    try
    {
      f = tryToLoadFont("ds-digi.ttf", instance);
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
    }
    if (f == null)
      f = new Font("Courier New", Font.BOLD, 20);
    else
      f = f.deriveFont(Font.BOLD, 20);
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
        throw new NullPointerException("Could not find font resource \"" + fontName + "\"\n\t\tin \"" + fontRes + "\"\n\t\tfor \"" + parent.getClass().getName() + "\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
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

  public void setDirection(double d)
  {
    this.direction = d;
  }

  public void setSpeed(double speed)
  {
    dataValueLabel.setText(speedFmt.format(speed));
  }

  public void setAngleValue(double angle)
  {
    dataValueLabel.setText(angleFmt.format(angle));
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
    // Background
    //  Dimension dim =  this.getSize();
    //  System.out.println("Dim:" + dim.getWidth() + "x" + dim.getHeight());
    Color startColor = new Color(0x94, 0x9c, 0x84); // new Color(0, 128, 128); // Color.black; // new Color(255, 255, 255);
    Color endColor = new Color(0, 64, 64); // Color.gray; // new Color(102, 102, 102);
    GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down

    if (false)
    {
      //  GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor); // Diagonal, top-left to bottom-right
      //  GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
      //  GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical
      ((Graphics2D) g).setPaint(gradient);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
    Dimension dim =  this.getSize();
    double radius = (Math.min(dim.width, dim.height) - 10d) / 2d;
    if (true)
    {
      Point center = new Point((dim.width / 2), (dim.height / 2));
      if (true) // With shaded bevel
      {
        RadialGradientPaint rgp = new RadialGradientPaint(center, 
                                                          (int)(radius * 1.15), 
                                                          new float[] {0f, 0.9f, 1f}, 
                                                          new Color[] {this.getBackground(), Color.gray, this.getBackground()});
        ((Graphics2D)g).setPaint(rgp);
        ((Graphics2D)g).fillRect(0, 0, dim.width, dim.height);
      }
      drawGlossyCircularDisplay((Graphics2D)g, center, (int)radius, Color.lightGray, Color.black, 1f);
    }
    // Boat

    // Rose
    g.setColor(Color.lightGray);
    for (int i = 0; i < 360; i += 10)
    {
      int x1 = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians(i)));
      int y1 = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians(i)));
      int x2 = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(i)));
      int y2 = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(i)));
      g.drawLine(x1, y1, x2, y2);
    }
    g.setColor(Color.lightGray);
    String n = "N";
    int fontSize = 14;
    g.setFont(g.getFont().deriveFont(Font.BOLD, fontSize));
    int strWidth  = g.getFontMetrics(g.getFont()).stringWidth(n);
    g.drawString(n, (dim.width / 2) - strWidth / 2, (int)(fontSize * 1.5));
    
    // Arrow
    g.setColor(Color.white);
    Stroke origStroke = ((Graphics2D) g).getStroke();
    Stroke stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    ((Graphics2D) g).setStroke(stroke);
    int x = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(direction - 90)));
    int y = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(direction - 90)));
//  g.drawLine((dim.width / 2), (dim.height / 2), x, y);
    Utils.drawArrow((Graphics2D)g, new Point((dim.width / 2), (dim.height / 2)), new Point(x, y), Color.white);    
    ((Graphics2D) g).setStroke(origStroke);
  }

  private static void drawGlossyCircularDisplay(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
    g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);

    Point gradientOrigin = new Point(center.x - radius,
                                     center.y - radius);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (2 * radius / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    g2d.fillOval((int)(center.x - (radius * 0.90)), 
                 (int)(center.y - (radius * 0.95)), 
                 (int)(2 * radius * 0.9), 
                 (int)(2 * radius * 0.95));
  }
}
