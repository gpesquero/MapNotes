package osm.mapnotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class KeepRightErrorsPreferencesActivity extends AppCompatActivity {

    MyPreferences mPreferences;

    Switch mSwitchShowKeepRightErrors;

    TextView mTextViewCacheMemorySize;
    TextView mTextViewNumberOfRequests;
    TextView mTextViewNumberOfHits;

    ListView mListViewDbInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keeprighterrors_preferences);

        MapNotesApplication app = (MapNotesApplication)getApplication();

        mPreferences = app.getPreferences();

        mSwitchShowKeepRightErrors = findViewById(R.id.switchShowKeepRightErrors);
        mSwitchShowKeepRightErrors.setChecked(mPreferences.mShowKeepRightErrors);

        KeepRightErrorsManager manager = app.getKeepRightErrorManager();

        int memSize = manager.getCacheMemorySize();
        int maxMemSize = manager.getCacheMemoryMaxSize();
        int requestCount = manager.getCacheMemoryNumberOfRequests();
        int hitCount = manager.getCacheMemoryNumberOfHits();

        mTextViewCacheMemorySize = findViewById(R.id.textViewCacheMemorySize);
        mTextViewCacheMemorySize.setText(memSize + " / " + maxMemSize);

        mTextViewNumberOfRequests = findViewById(R.id.textViewNumberOfRequests);

        String text = String.valueOf(requestCount);

        mTextViewNumberOfRequests.setText(text);

        mTextViewNumberOfHits = findViewById(R.id.textViewNumberOfHits);

        text = String.valueOf(hitCount);

        if (requestCount > 0) {

            text += String.format(Locale.US, " (%.1f%%)", hitCount*100.0/requestCount);
        }

        mTextViewNumberOfHits.setText(text);

        ArrayList<String> dbInfoList = app.getKeepRightErrorManager().getDbInfo();

        mListViewDbInfo = findViewById(R.id.listViewDbInfo);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, R.layout.log_text_view,
                dbInfoList);

        mListViewDbInfo.setAdapter(listAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPreferences.mShowKeepRightErrors = mSwitchShowKeepRightErrors.isChecked();
    }
}
