package nmea.event;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;

public abstract class NMEAListener implements EventListener
{
  private String groupID = "";
  
  public NMEAListener() 
  { this("default"); }
  public NMEAListener(String gID)
  { this.groupID = gID; }
  public final String getGroupID() { return groupID; }
  
  public void log(boolean b) {}
  public void log(boolean log, boolean withDateTime) {}
  public void setMessage(String s) {}
  public void manageNMEAString(String str) {}
  public void saveUserConfig() {}
  
  public void internalFrameClosed() {}
  
  public void dataUpdate() {}
  public void loadDataPointsForDeviation(List<double[]> dp) {}
  public void deviationCurveChanged(Hashtable<Double, Double> devCurve) {}
  public void replaySpeedChanged(int slider) {}
  public void dataBufferSizeChanged(int size) {}
  
  public void stopReading() throws Exception {}
  public void jumpToOffset(long recordOffset) {}

  public void refreshLogJournal() {}
  public void showRawData(boolean b) {}
  
  public void fireError(Throwable t) {}
  
  public void setWindScale(float f) {}
  public void dampingHasChanged(int damping) {}
}
