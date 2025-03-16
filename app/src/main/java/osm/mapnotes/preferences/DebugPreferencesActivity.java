package osm.mapnotes.preferences;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;

import osm.mapnotes.MapNotesApplication;
import osm.mapnotes.R;

public class DebugPreferencesActivity extends AppCompatActivity
{
  MapNotesPreferences mPreferences;

  Switch mSwitchShowDebugOverlay;

  ListView mListViewLog;

  ArrayAdapter<String> mLogListAdapter;

  private int MAX_LOG_SIZE = 1000;

  private Handler mTimerHandler;

  // Timer every second.
  private final int TIMER_DELAY = 1000;

  private Runnable mTimerRunnable = new Runnable()
  {
    private int mCount = 0;

    @Override
    public void run()
    {
      mLogListAdapter.notifyDataSetChanged();

      mTimerHandler.postDelayed(this, TIMER_DELAY);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_debug_preferences);

    MapNotesApplication app = (MapNotesApplication)getApplication();

    mPreferences = app.getPreferences();

    mSwitchShowDebugOverlay=findViewById(R.id.switchShowDebugOverlays);
    mSwitchShowDebugOverlay.setChecked(mPreferences.mShowDebugOverlay);

    mListViewLog = findViewById(R.id.listViewLog);

    mLogListAdapter = new ArrayAdapter<>(this, R.layout.log_text_view, app.getLogList());

    mListViewLog.setAdapter(mLogListAdapter);

    mTimerHandler=new Handler();
    mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    mPreferences.mShowDebugOverlay = mSwitchShowDebugOverlay.isChecked();
  }
}
