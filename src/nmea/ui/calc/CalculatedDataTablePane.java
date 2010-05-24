package nmea.ui.calc;

import nmea.ctx.NMEAContext;
import nmea.ctx.NMEADataCache;

import nmea.ctx.Utils;

import nmea.event.NMEAListener;

import nmea.local.LogisailResourceBundle;

import nmea.ui.NMEAFrameInterface;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;

import java.awt.Font;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.Distance;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.parser.UTCTime;


public class CalculatedDataTablePane
  extends JPanel
{
  Object[][] defaultTable = 
    {
      // GPS
      { NMEADataCache.COG,         new Angle360() },           // 0
      { NMEADataCache.SOG,         new Speed(0d) },
      { NMEADataCache.POSITION ,   GeoPos.init() },
      { NMEADataCache.GPS_DATE_TIME, new UTCDate(new Date()) },
      { NMEADataCache.GPS_TIME,    new UTCTime(new Date()) },
      // Boat
      { NMEADataCache.HDG_COMPASS, new Angle360() },           // 5
      { NMEADataCache.DECLINATION, new Angle180EW(-Double.MAX_VALUE) },
      { NMEADataCache.DEVIATION,   new Angle180EW(-Double.MAX_VALUE) },
      { NMEADataCache.VARIATION,   new Angle180EW(-Double.MAX_VALUE) },
      { NMEADataCache.HDG_MAG,     new Angle360() },
      { NMEADataCache.HDG_TRUE,    new Angle360() },
      { NMEADataCache.BSP,         new Speed() },
      { NMEADataCache.LEEWAY,      new Angle180LR() },
      { NMEADataCache.CMG,         new Angle360() },
      { NMEADataCache.LOG,         new Distance() },          // 14
      { NMEADataCache.DAILY_LOG,   new Distance() },
      { NMEADataCache.WATER_TEMP,  new Temperature() },
      { NMEADataCache.DBT,         new Depth(0) },
      // Wind
      { NMEADataCache.AWA,         new Angle180() },          // 18
      { NMEADataCache.AWS,         new Speed() },
      { NMEADataCache.TWA,         new Angle180() },
      { NMEADataCache.TWS,         new Speed() },
      { NMEADataCache.TWD,         new Angle360() },
      // Others
      { NMEADataCache.CSP,         new Speed() },             // 23
      { NMEADataCache.CDR,         new Angle360() },
      // Nav
      { NMEADataCache.FROM_WP,     "" },                      // 25
      { NMEADataCache.TO_WP,       "" },
      { NMEADataCache.WP_POS,      GeoPos.init() },
      { NMEADataCache.D2WP,        new Distance() },
      { NMEADataCache.B2WP,        new Angle360() },
      { NMEADataCache.S2WP,        new Speed(0d) },
      { NMEADataCache.XTE,         new Distance() },
      { NMEADataCache.S2STEER,     "" },
      // Coeff
      { NMEADataCache.MAX_LEEWAY, 10d },                       // 33
      { NMEADataCache.BSP_FACTOR,  1d },
      { NMEADataCache.AWS_FACTOR,  1d },
      { NMEADataCache.AWA_OFFSET,  0d },
      { NMEADataCache.HDG_OFFSET,  0d },
      { NMEADataCache.DEVIATION_FILE, "zero-deviation.csv" },
      { NMEADataCache.DEFAULT_DECLINATION, new Angle180EW() },
      { NMEADataCache.DAMPING,     1 }
    };

  
  static final String KEY   = "Key"; // LogisailResourceBundle.buildMessage("key");
  static final String VALUE = LogisailResourceBundle.buildMessage("value");

  final String names[] = new String[] { KEY, VALUE };
  Object data[][] = new Object[0][names.length];
  TableModel dataModel;
  JTable table;
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel centerPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JScrollPane centerScrollPane = null;
  JPanel topPanel = new JPanel();
  private JButton logButton = new JButton();
//private NMEAFrameInterface parent;
  private BorderLayout borderLayout3 = new BorderLayout();

  void init()
  {
    data = defaultTable;
    // Init cache with default values    
    for (int i=0; i<data.length; i++)
      NMEAContext.getInstance().putDataCache((String)data[i][0], data[i][1]);

    Utils.readNMEAParameters();
    
    // Init dev curve
    String deviationFileName = (String) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION_FILE);
    NMEAContext.getInstance().setDeviation(Utils.loadDeviationCurve(deviationFileName));

    borderLayout1 = new BorderLayout();
    centerPanel = new JPanel();
    borderLayout2 = new BorderLayout();
    centerScrollPane = null;
    topPanel = new JPanel();
    logButton = new JButton();
    borderLayout3 = new BorderLayout();
  }

  public CalculatedDataTablePane(NMEAFrameInterface f)
  {
    init();
//  parent = f;
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
    setLayout(borderLayout1);
    centerPanel.setLayout(borderLayout2);
    add(centerPanel, BorderLayout.CENTER);
    add(topPanel, BorderLayout.NORTH);
    initTable();
    
    TableColumn colOne = table.getColumnModel().getColumn(0);
    TableColumn colTwo = table.getColumnModel().getColumn(1);
    
//  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    colOne.setMaxWidth(150);
    colOne.setPreferredWidth(150);
    
    colOne.setCellRenderer(new ColorCellRenderer());
    colTwo.setCellRenderer(new ColorCellRenderer());

    NMEAContext.getInstance().addNMEAListener(new NMEAListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void dataUpdate() 
        {
//        System.out.println("Updating Data!!!");
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          for (int i=0; i<data.length; i++)
          {
            Object o = cache.get(data[i][0]);
            if (o != null)
              data[i][1] = o;
          }
          ((AbstractTableModel)dataModel).fireTableDataChanged();
        }
      });
  }

  private void initTable()
  {
    dataModel = new AbstractTableModel()
      {
        public int getColumnCount()
        {
          return names.length;
        }

        public int getRowCount()
        {
          return data.length;
        }

        public Object getValueAt(int row, int col)
        {
          return data[row][col];
        }

        public String getColumnName(int column)
        {
          return names[column];
        }

        public Class getColumnClass(int c)
        {
          return Object.class; // getValueAt(1, c).getClass();
        }

        public boolean isCellEditable(int row, int col)
        {
          return false;
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          data[row][column] = aValue;
        }
      };
    table = new JTable(dataModel);
    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }

  public void dropTable()
  {
    data = null;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
  }
  
  class ColorCellRenderer extends JLabel implements TableCellRenderer
  {
    public ColorCellRenderer()
    {
      this.setOpaque(true);  
    }
    
    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column)
    {
      this.setText(value.toString());
      this.setForeground(Color.black);
      if (row < 5)
        this.setBackground(Color.pink);
      else if (row < 12)
        this.setBackground(Color.cyan);
      else if (row < 14)
        this.setBackground(Color.red);
      else if (row < 18)
        this.setBackground(Color.orange);
      else if (row < 23)
        this.setBackground(Color.green);
      else if (row < 25)
        this.setBackground(Color.lightGray);
      else if (row < 33)
        this.setBackground(Color.lightGray);
      else
      {
        this.setBackground(Color.gray);
        this.setForeground(Color.white);
      }
      if (column == 1)
      {
        this.setFont(this.getFont().deriveFont(Font.BOLD, this.getFont().getSize()));
      }
      if (defaultTable[row][0] == NMEADataCache.COG || // GPS Data
          defaultTable[row][0] == NMEADataCache.SOG ||
          defaultTable[row][0] == NMEADataCache.POSITION ||
          defaultTable[row][0] == NMEADataCache.GPS_TIME ||
          defaultTable[row][0] == NMEADataCache.GPS_DATE_TIME ||
          defaultTable[row][0] == NMEADataCache.FROM_WP ||
          defaultTable[row][0] == NMEADataCache.D2WP ||
          defaultTable[row][0] == NMEADataCache.XTE ||
          defaultTable[row][0] == NMEADataCache.TO_WP ||
          defaultTable[row][0] == NMEADataCache.WP_POS ||
          defaultTable[row][0] == NMEADataCache.B2WP ||
          defaultTable[row][0] == NMEADataCache.S2WP ||
          defaultTable[row][0] == NMEADataCache.S2STEER)
        this.setForeground(Color.blue);
      else if (defaultTable[row][0] == NMEADataCache.HDG_COMPASS ||  // NMEA
               defaultTable[row][0] == NMEADataCache.DECLINATION ||
               defaultTable[row][0] == NMEADataCache.BSP ||
               defaultTable[row][0] == NMEADataCache.LOG ||
               defaultTable[row][0] == NMEADataCache.DAILY_LOG ||
               defaultTable[row][0] == NMEADataCache.WATER_TEMP ||
               defaultTable[row][0] == NMEADataCache.DBT ||
               defaultTable[row][0] == NMEADataCache.AWA ||
               defaultTable[row][0] == NMEADataCache.AWS)
        this.setForeground(Color.red);
      else if (defaultTable[row][0] == NMEADataCache.DEVIATION ||
               defaultTable[row][0] == NMEADataCache.MAX_LEEWAY)
        this.setForeground(new Color(0, 113, 0)); // Dark green
      
      this.setToolTipText(NMEADataCache.TOOLTIP_MAP.get(defaultTable[row][0]));
      
      return this;
    }
  }  
}
