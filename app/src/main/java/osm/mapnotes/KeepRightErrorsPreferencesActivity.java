package osm.mapnotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

public class KeepRightErrorsPreferencesActivity extends AppCompatActivity {

    MyPreferences mPreferences;

    Switch mSwitchShowKeepRightErrors;

    TextView mTextViewCacheMemorySize;
    TextView mTextViewNumberOfRequests;
    TextView mTextViewNumberOfHits;

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

            text += String.format(" (%.1f%%)", hitCount*100.0/requestCount);
        }

        mTextViewNumberOfHits.setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPreferences.mShowKeepRightErrors = mSwitchShowKeepRightErrors.isChecked();
    }
}
