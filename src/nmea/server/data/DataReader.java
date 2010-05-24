package nmea.server.data;

public interface DataReader
{
  public void read();
  public void closeReader() throws Exception;
}
