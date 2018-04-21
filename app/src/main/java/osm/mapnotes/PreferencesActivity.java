package osm.mapnotes;

import android.os.Bundle;
import android.widget.CheckBox;

import android.support.v7.app.AppCompatActivity;

public class PreferencesActivity extends AppCompatActivity {

    CheckBox mCheckBoxShowDebugOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        mCheckBoxShowDebugOverlay=(CheckBox) findViewById(R.id.checkBoxDebug);

        mCheckBoxShowDebugOverlay.setChecked(MainActivity.mPreferences.mShowDebugOverlay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainActivity.mPreferences.mShowDebugOverlay=mCheckBoxShowDebugOverlay.isChecked();
    }
}
