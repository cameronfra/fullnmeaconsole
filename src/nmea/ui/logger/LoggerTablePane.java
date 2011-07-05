package nmea.ui.logger;

import nmea.server.ctx.NMEAContext;

import nmea.local.LogisailResourceBundle;

import nmea.server.utils.HTTPServer;
import nmea.server.utils.ServerStop;

import nmea.ui.NMEAFrameInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ocss.nmea.parser.StringParsers;

public class LoggerTablePane extends JPanel
{
  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
  private JCheckBox httpCheckBox = new JCheckBox();
  private JTextField httpPortTextField = new JTextField();
  static final String SENTENCE_ID = LogisailResourceBundle.buildMessage("sentence-id");
  static final String VALUE = LogisailResourceBundle.buildMessage("value");
  static final String VALID = LogisailResourceBundle.buildMessage("valid");
  static final String DATE = LogisailResourceBundle.buildMessage("read-at");
  static final String DELTA = LogisailResourceBundle.buildMessage("delta");
  
  final String names[] = new String[] {
      SENTENCE_ID, VALUE, VALID, DATE, DELTA
    };
  Object data[][] = new Object[0][names.length];
  TableModel dataModel;
  JTable table;
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel centerPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JScrollPane centerScrollPane = null;
  JPanel topPanel = new JPanel();
  private JButton logButton = new JButton();
  private NMEAFrameInterface parent;
  private BorderLayout borderLayout3 = new BorderLayout();

  private HTTPServer nmeaHttpServer = null;
  private JCheckBox addDateCheckBox = new JCheckBox();

  void init()
  {
    data = new Object[0][names.length];
    borderLayout1 = new BorderLayout();
    centerPanel = new JPanel();
    borderLayout2 = new BorderLayout();
    centerScrollPane = null;
    topPanel = new JPanel();
    logButton = new JButton();
    borderLayout3 = new BorderLayout();
  }

  public LoggerTablePane(NMEAFrameInterface f)
  {
    init();
    parent = f;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    setLayout(borderLayout1);
    this.setSize(new Dimension(570, 300));
    centerPanel.setLayout(borderLayout2);
    logButton.setText(LogisailResourceBundle.buildMessage("log-data"));
    logButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            logButton_actionPerformed(e);
          }
        });
    addDateCheckBox.setText("Time in Log");
    addDateCheckBox.setSelected(true);
    addDateCheckBox.setToolTipText("Add local date and time to the logged data");
    httpPortTextField.setText(System.getProperty("http.port", "6666"));
    httpPortTextField.setSize(new Dimension(40, 20));
    httpPortTextField.setPreferredSize(new Dimension(40, 20));
    httpPortTextField.setHorizontalAlignment(JTextField.CENTER);
    httpCheckBox.setText(LogisailResourceBundle.buildMessage("rebroadcast"));
    httpCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            httpCheckBox_actionPerformed(e);
          }
        });
    add(centerPanel, BorderLayout.CENTER);
    topPanel.add(logButton, null);
    topPanel.add(addDateCheckBox, null);
    topPanel.add(httpCheckBox, null);
    topPanel.add(httpPortTextField, null);
    add(topPanel, BorderLayout.NORTH);
    initTable();
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
        return getValueAt(0, c).getClass();
      }

      public boolean isCellEditable(int row, int col)
      {
        return col == 0;
      }

      public void setValueAt(Object aValue, int row, int column)
      {
        data[row][column] = aValue;
      }
    };
    table = new JTable(dataModel);

    table.getColumn(SENTENCE_ID).setPreferredWidth(60); // Sentence ID
    table.getColumn(SENTENCE_ID).setMaxWidth(70);
    table.getColumn(VALID).setPreferredWidth(40); // Valid
    table.getColumn(VALID).setMaxWidth(60); // Valid
    table.getColumn(DATE).setPreferredWidth(60); // Read at
    table.getColumn(DATE).setMaxWidth(60); // Read at
    table.getColumn(DELTA).setPreferredWidth(60); // delta
    table.getColumn(DELTA).setMaxWidth(60); // delta

    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }

  public void dropTable()
  {
    data = null;
    table.repaint();
  }
  
  public void addLineInTable(String s)
  {
    int len = (data==null)?0:data.length;
    Object newData[][] = new Object[len + 1][names.length];
    for(int i = 0; i < len; i++)
    {
      for(int j = 0; j < names.length; j++)
        newData[i][j] = data[i][j];
    }

    newData[len][0] = s;
    newData[len][1] = "";
    newData[len][2] = Boolean.valueOf(false);
    newData[len][3] = "";
    newData[len][4] = "";
    data = newData;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  public void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if(selectedRow < 0)
    {
      JOptionPane.showMessageDialog(null, "Please choose a row to remove", "Removing an entry", 2);
    } else
    {
      int l = data.length;
      Object newData[][] = new Object[l - 1][names.length];
      int newInd = 0;
      for(int oldInd = 0; oldInd < l; oldInd++)
      {
        if (oldInd != selectedRow)
        {
          for (int j = 0; j < names.length; j++)
            newData[newInd][j] = data[oldInd][j];

          newInd++;
        }
      }
      data = newData;
      ((AbstractTableModel)dataModel).fireTableDataChanged();
      table.repaint();
    }
  }

  public String[] getKeys()
  {
    String sa[] = new String[data.length];
    for(int i = 0; i < data.length; i++)
      sa[i] = new String((String)data[i][0]);
    return sa;
  }

  Hashtable<String, Long> timemap = new Hashtable<String, Long>();
  
  public void setValue(String key, String val)
  {    
    for (int i = 0; i < data.length; i++)
    {
      if(!((String)data[i][0]).toUpperCase().equals(key))
        continue;
      data[i][1] = val;
      
      // Validation
      data[i][2] = Boolean.valueOf(StringParsers.validCheckSum(val));
      
      long previous = 0L;
      try
      {
        previous = (timemap.get(key)).longValue();
      }
      catch (Exception ignore) { }
      long current = System.currentTimeMillis();
      timemap.put(key, new Long(current));
      data[i][3] = dateFormat.format(new Date(current));
      if(previous != 0L)
        data[i][4] = Long.toString(current - previous);
      break;
    }

    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  private void httpCheckBox_actionPerformed(ActionEvent e)
  {
    httpPortTextField.setEnabled(!httpCheckBox.isSelected());
    System.setProperty("http.port", httpPortTextField.getText());
    if (httpCheckBox.isSelected())
    {
      // Create listener
      String[] prms = new String[] { "-verbose=n", "-fmt=xml" };
      nmeaHttpServer = new HTTPServer(prms, parent.getPfile(), data);
    }
    else
    {
      // remove listener
       try
       {
         ServerStop client = new ServerStop();
         String port = System.getProperty("http.port", "6666");
         String host = System.getProperty("http.host", "localhost");
      /* String resp = */ client.getResponse(host, port, "/exit");
//       String[] prms = new String[] { "-verbose=y", "-fmt=xml" };
//       nmeaHttpServer = new HTTPServer(prms, parent.getPfile(), data);
       }
       catch (Exception ex)
       {
         ex.printStackTrace();
       }
      nmeaHttpServer = null;
    }
  }

  public void refreshHTTPServer()
  {
    if (httpCheckBox.isSelected())
    {
      try
      {
        ServerStop client = new ServerStop();
        String port = System.getProperty("http.port", "6666");
        String host = System.getProperty("http.host", "localhost");
      /* String resp = */ client.getResponse(host, port, "/exit");
        String[] prms = new String[] { "-verbose=n", "-fmt=xml" };
        boolean refreshed = false;
        while (!refreshed)
        {
          try
          {
            nmeaHttpServer = new HTTPServer(prms, parent.getPfile(), data);
            refreshed = true;
          }
          catch (Exception bindException)
          {
            try { Thread.sleep(1000L); } catch (Exception ignore) {}
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  private boolean log = false;
  private void logButton_actionPerformed(ActionEvent e)
  {
    log = !log;
    logButton.setText(log? LogisailResourceBundle.buildMessage("stop-log"): LogisailResourceBundle.buildMessage("log-data"));
    NMEAContext.getInstance().fireLogChanged(log, addDateCheckBox.isSelected());
  }
}
