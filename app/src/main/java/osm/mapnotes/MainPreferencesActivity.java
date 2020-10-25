package osm.mapnotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainPreferencesActivity extends AppCompatActivity
        implements View.OnClickListener {

    MapNotesApplication mApp;

    TextView mTextViewVersion;

    Button mButtonTilePreferences;

    CheckBox mCheckBoxShowKeepRightErrors;

    CheckBox mCheckBoxShowDebugOverlay;

    TextView mTextViewInternalDataPath;
    TextView mTextViewExternalDataPath;
    TextView mTextViewMarkerDatabasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_preferences);

        mApp = (MapNotesApplication) getApplication();

        mTextViewVersion=findViewById(R.id.textViewVersion);
        mTextViewVersion.setText(mApp.getAppName(this)+" v"+mApp.getAppVersion());

        mButtonTilePreferences=findViewById(R.id.buttonTilePreferences);
        mButtonTilePreferences.setOnClickListener(this);

        mCheckBoxShowKeepRightErrors=findViewById(R.id.checkBoxShowErrors);
        mCheckBoxShowKeepRightErrors.setChecked(MainActivity.mPreferences.mShowKeepRightErrors);

        mCheckBoxShowDebugOverlay=findViewById(R.id.checkBoxDebug);
        mCheckBoxShowDebugOverlay.setChecked(MainActivity.mPreferences.mShowDebugOverlay);

        mTextViewInternalDataPath=findViewById(R.id.textViewInternalDataPath);;
        mTextViewInternalDataPath.setText(MainActivity.mPreferences.mInternalDataPath);

        mTextViewExternalDataPath=findViewById(R.id.textViewExternalDataPath);;
        mTextViewExternalDataPath.setText(MainActivity.mPreferences.mExternalDataPath);

        mTextViewMarkerDatabasePath=findViewById(R.id.textViewMarkerDatabasePath);
        mTextViewMarkerDatabasePath.setText(MainActivity.mPreferences.mInternalDataPath+
                MainActivity.mPreferences.mMarkerDatabaseName);

        /*
        String fullPath=MainActivity.mPreferences.mDatabaseDir+
                MainActivity.mPreferences.mDatabaseName;
        mTextViewDatabasePath.setText(fullPath);
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainActivity.mPreferences.mShowKeepRightErrors=mCheckBoxShowKeepRightErrors.isChecked();

        MainActivity.mPreferences.mShowDebugOverlay=mCheckBoxShowDebugOverlay.isChecked();
    }

    @Override
    public void onClick(View view) {

        if (view==mButtonTilePreferences) {

            Intent intent = new Intent(this, TilePreferencesActivity.class);
            startActivity(intent);
        }
    }
}
