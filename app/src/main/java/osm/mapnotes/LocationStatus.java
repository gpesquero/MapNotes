package osm.mapnotes;

public class LocationStatus
{
  private boolean mHasPermissions = false;
  private boolean mProviderEnabled = false;
  private int mStatus = -1;
  private int mNumSats = -1;

  public void setHasPermissions(boolean hasPermissions)
  {
    mHasPermissions = hasPermissions;
  }

  public boolean hasPermissions()
  {
    return mHasPermissions;
  }

  public void setProviderEnabled(boolean providerEnabled)
  {
    mProviderEnabled=providerEnabled;
  }

  public boolean isProviderEnabled()
  {
    return mProviderEnabled;
  }

  public void setStatus(int status)
  {
    mStatus = status;
  }

  public int getStatus()
  {
    return mStatus;
  }

  public void setNumSats(int numSats)
  {
    mNumSats = numSats;
  }

  public int getNumSats()
  {
    return mNumSats;
  }
}
