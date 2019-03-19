package osm.mapnotes;

import android.os.Bundle;
import android.widget.CheckBox;

import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PreferencesActivity extends AppCompatActivity {

    CheckBox mCheckBoxShowDebugOverlay;

    RadioGroup mRadioGroupTileSource;

    TextView mTextViewDatabasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        mCheckBoxShowDebugOverlay=findViewById(R.id.checkBoxDebug);
        mCheckBoxShowDebugOverlay.setChecked(MainActivity.mPreferences.mShowDebugOverlay);

        mRadioGroupTileSource=findViewById(R.id.radioGroupTileSource);
        RadioButton radioButton=(RadioButton)mRadioGroupTileSource.getChildAt(
                MainActivity.mPreferences.mTileSource+1);
        radioButton.setChecked(true);

        mTextViewDatabasePath=findViewById(R.id.textViewDatabasePath);

        String fullPath=MainActivity.mPreferences.mDatabaseDir+
                MainActivity.mPreferences.mDatabaseName;
        mTextViewDatabasePath.setText(fullPath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainActivity.mPreferences.mShowDebugOverlay=mCheckBoxShowDebugOverlay.isChecked();

        int id=mRadioGroupTileSource.getCheckedRadioButtonId();

        if (id<0) {
            MainActivity.mPreferences.mTileSource=MyPreferences.TILE_SOURCE_DEFAULT;
        }
        else {

            RadioButton radioButton=findViewById(id);
            MainActivity.mPreferences.mTileSource=mRadioGroupTileSource.indexOfChild(radioButton)-1;
        }
    }
}
