package osm.mapnotes;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import osm.mapnotes.keepright.KeepRightErrorsManager;
import osm.mapnotes.preferences.MapNotesPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MapNotesApplication extends Application
{
  private MarkerDatabase mMarkerDatabase = null;

  private KeepRightErrorsManager mKeepRightErrorsManager = null;

  MapNotesPreferences mPreferences = null;

  private final ArrayList<String> mLogList = new ArrayList<>();

  SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

  @Override
  public void onCreate()
  {
    super.onCreate();

    mMarkerDatabase = new MarkerDatabase();

    mKeepRightErrorsManager = new KeepRightErrorsManager(this);
  }

  public void closeApp()
  {
    Log.w("MapNotes_destroy", "closeApp()");

    if (mKeepRightErrorsManager != null)
    {
      // Close KeepRight Error Manager.
      mKeepRightErrorsManager.close();
      mKeepRightErrorsManager = null;
    }

    if (mMarkerDatabase != null)
    {
      mMarkerDatabase.close();
      mMarkerDatabase = null;
    }

    if (mPreferences != null)
    {
      mPreferences.savePreferences(this);
      mPreferences = null;
    }
  }

  public MapNotesPreferences getPreferences()
  {
    if (mPreferences == null)
    {
      mPreferences = new MapNotesPreferences();
      mPreferences.loadPreferences(this);
    }

    return mPreferences;
  }

  public String getAppName(Context context)
  {
    return context.getString(R.string.app_name);
  }

  public String getAppVersion()
  {
    return BuildConfig.VERSION_NAME;
  }

  public MarkerDatabase getMarkerDatabase()
  {
    return mMarkerDatabase;
  }

  public KeepRightErrorsManager getKeepRightErrorManager()
  {
    return mKeepRightErrorsManager;
  }

  private void log(String logType, String text)
  {
    Date currentTime = new Date();

    String timeString = mDateFormat.format(currentTime);

    mLogList.add(0, timeString + " " + logType + " " + text);
  }

  public ArrayList<String> getLogList()
  {
    return mLogList;
  }

  public void logError(String text)
  {
    log("<<<ERROR>>>", text);
  }

  public void logWarning(String text)
  {
    log("<<<WARNING>>>", text);
  }

  public void logInfo(String text)
  {
    log("<INFO>", text);
  }
}
