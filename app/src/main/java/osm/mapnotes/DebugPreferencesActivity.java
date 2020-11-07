package osm.mapnotes;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DebugPreferencesActivity extends AppCompatActivity {

    MyPreferences mPreferences;

    Switch mSwitchShowDebugOverlay;

    ListView mListViewLog;

    ArrayList<String> mLogList = new ArrayList<>();

    ArrayAdapter<String> mLogListAdapter;

    private int MAX_LOG_SIZE = 1000;

    private Handler mTimerHandler;

    private final int TIMER_DELAY = 1000;      // Timer every second

    private Runnable mTimerRunnable = new Runnable() {

        private int mCount = 0;

        @Override
        public void run() {

            /*
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

            Date currentTime = new Date();

            String dateString = df.format(currentTime);

            mLogList.add(0, dateString + " (INFO) Log message #"+mCount);

            while (mLogList.size() > MAX_LOG_SIZE) {

                mLogList.remove(mLogList.size()-1);
            }

            mCount++;
            */

            mLogListAdapter.notifyDataSetChanged();

            mTimerHandler.postDelayed(this, TIMER_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_preferences);

        MapNotesApplication app = (MapNotesApplication)getApplication();

        mPreferences = app.getPreferences();

        mSwitchShowDebugOverlay=findViewById(R.id.switchShowDebugOverlays);
        mSwitchShowDebugOverlay.setChecked(mPreferences.mShowDebugOverlay);

        mListViewLog = findViewById(R.id.listViewLog);

        /*
        for (int i=0; i < 1000; i++) {

            mLogList.add("10:00:00 (INFO) Log message #"+i);
        }
        */

        //mLogListAdapter = new ArrayAdapter<>(this, R.layout.log_text_view, mLogList);

        mLogListAdapter = new ArrayAdapter<>(this, R.layout.log_text_view, app.getLogList());

        mListViewLog.setAdapter(mLogListAdapter);

        mTimerHandler=new Handler();
        mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPreferences.mShowDebugOverlay = mSwitchShowDebugOverlay.isChecked();
    }
}
