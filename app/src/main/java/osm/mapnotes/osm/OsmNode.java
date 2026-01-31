package osm.mapnotes.osm;

import java.util.HashMap;

public class OsmNode
{
  public long mId;
  public String mTimeStamp;
  public double mLat, mLon;

  private final HashMap<String, String> mTags = new HashMap<>();

  public OsmNode(long id, String timeStamp, double lat, double lon)
  {
    mId = id;
    mTimeStamp = timeStamp;
    mLat = lat;
    mLon = lon;
  }

  public void addTag(String key, String value)
  {
    mTags.put(key, value);
  }

  public HashMap<String, String> getTags()
  {
    return mTags;
  }
}
