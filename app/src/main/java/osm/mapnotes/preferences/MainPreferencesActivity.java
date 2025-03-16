package osm.mapnotes.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import osm.mapnotes.MapNotesApplication;
import osm.mapnotes.R;
import osm.mapnotes.keepright.KeepRightErrorsPreferencesActivity;

public class MainPreferencesActivity extends AppCompatActivity
  implements View.OnClickListener
{
  MapNotesApplication mApp;

  MapNotesPreferences mPreferences;

  TextView mTextViewVersion;

  Button mButtonTilePreferences;
  Button mButtonKeepRightErrorsPreferences;
  Button mButtonDebugPreferences;

  TextView mTextViewInternalDataPath;
  TextView mTextViewExternalDataPath;
  TextView mTextViewMarkerDatabasePath;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main_preferences);

    mApp = (MapNotesApplication) getApplication();

    mPreferences = mApp.getPreferences();

    mTextViewVersion=findViewById(R.id.textViewVersion);
    mTextViewVersion.setText(mApp.getAppName(this) + " v" + mApp.getAppVersion());

    mButtonTilePreferences=findViewById(R.id.buttonTilePreferences);
    mButtonTilePreferences.setOnClickListener(this);

    mButtonKeepRightErrorsPreferences=findViewById(R.id.buttonKeepRightErrorsPreferences);
    mButtonKeepRightErrorsPreferences.setOnClickListener(this);

    mButtonDebugPreferences=findViewById(R.id.buttonDebugPreferences);
    mButtonDebugPreferences.setOnClickListener(this);

    mTextViewInternalDataPath=findViewById(R.id.textViewInternalDataPath);;
    mTextViewInternalDataPath.setText(mPreferences.mInternalDataPath);

    mTextViewExternalDataPath=findViewById(R.id.textViewExternalDataPath);;
    mTextViewExternalDataPath.setText(mPreferences.mExternalDataPath);

    mTextViewMarkerDatabasePath=findViewById(R.id.textViewMarkerDatabasePath);
    mTextViewMarkerDatabasePath.setText(mPreferences.mInternalDataPath +
      mPreferences.mMarkerDatabaseName);

    /*
    String fullPath = MainActivity.mPreferences.mDatabaseDir + MainActivity.mPreferences.mDatabaseName;
    mTextViewDatabasePath.setText(fullPath);
    */
  }

  @Override
  public void onClick(View view)
  {
    if (view == mButtonTilePreferences)
    {
      Intent intent = new Intent(this, TilePreferencesActivity.class);
      startActivity(intent);
    }
    else if (view == mButtonKeepRightErrorsPreferences)
    {
      Intent intent = new Intent(this, KeepRightErrorsPreferencesActivity.class);
      startActivity(intent);
    }
    else if (view == mButtonDebugPreferences)
    {
      Intent intent = new Intent(this, DebugPreferencesActivity.class);
      startActivity(intent);
    }
  }
}
