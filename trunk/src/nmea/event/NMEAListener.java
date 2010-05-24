package nmea.event;

import java.util.ArrayList;
import java.util.Hashtable;

public abstract class NMEAListener
{
  private String groupID = "";
  
  public NMEAListener() {}
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
  public void loadDataPointsForDeviation(ArrayList<double[]> dp) {}
  public void deviationCurveChanged(Hashtable<Double, Double> devCurve) {}
  public void replaySpeedChanged(int slider) {}
  public void dataBufferSizeChanged(int size) {}
  
  public void stopReading() throws Exception {}

  public void refreshLogJournal() {}
}
