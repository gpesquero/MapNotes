package osm.mapnotes.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import osm.mapnotes.MapNotesApplication;
import osm.mapnotes.MarkerDatabase;
import osm.mapnotes.MarkerDatabase.MarkerData;
import osm.mapnotes.R;
import osm.mapnotes.keepright.KeepRightErrorsPreferencesActivity;
import osm.mapnotes.osm.OsmFileWriter;
import osm.mapnotes.osm.OsmNode;
import osm.mapnotes.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainPreferencesActivity extends AppCompatActivity
  implements View.OnClickListener
{
  MapNotesApplication mApp;

  MapNotesPreferences mPreferences;

  TextView mTextViewVersion;

  Button mButtonTilePreferences;
  Button mButtonKeepRightErrorsPreferences;
  Button mButtonDebugPreferences;
  Button mButtonExportMarkers;

  TextView mTextViewDataPath;
  TextView mTextViewMarkerDatabasePath;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main_preferences);

    mApp = (MapNotesApplication) getApplication();

    mPreferences = mApp.getPreferences();

    mTextViewVersion = findViewById(R.id.textViewVersion);
    mTextViewVersion.setText(mApp.getAppName(this) + " v" + mApp.getAppVersion());

    mButtonTilePreferences = findViewById(R.id.buttonTilePreferences);
    mButtonTilePreferences.setOnClickListener(this);

    mButtonKeepRightErrorsPreferences = findViewById(R.id.buttonKeepRightErrorsPreferences);
    mButtonKeepRightErrorsPreferences.setOnClickListener(this);

    mButtonDebugPreferences = findViewById(R.id.buttonDebugPreferences);
    mButtonDebugPreferences.setOnClickListener(this);

    mButtonExportMarkers = findViewById(R.id.buttonExportMarkers);
    mButtonExportMarkers.setOnClickListener(this);

    mTextViewDataPath = findViewById(R.id.textViewDataPath);
    mTextViewDataPath.setText(mPreferences.mDataPath);

    mTextViewMarkerDatabasePath = findViewById(R.id.textViewMarkerDatabasePath);
    mTextViewMarkerDatabasePath.setText(mPreferences.mDataPath +
      mPreferences.mMarkerDatabaseName);
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
    else if (view == mButtonExportMarkers)
    {
      String toastString;

      MarkerDatabase markerDatabase = mApp.getMarkerDatabase();

      if (markerDatabase == null || !markerDatabase.isOpen())
        toastString = getString(R.string.error_marker_database_is_not_open);
      else
      {
        List<MarkerData> markersData = markerDatabase.getMarkersData();

        if (markersData == null)
          toastString = getString(R.string.error_getting_map_markers);
        else
        {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
          String fileName = "export_" + LocalDateTime.now().format(formatter) + ".osm";

          OsmFileWriter exportFile = new OsmFileWriter();
          if (!exportFile.createNewFile(mPreferences.mDataPath + fileName))
          {
            toastString = getString(R.string.error_creating_export_file_zz, fileName) +
                          "(" + exportFile.getErrorString() + ")";
          }
          else
          {
            // Add nodes.
            for (MarkerData data : markersData)
            {
              OsmNode node = new OsmNode(data.mId, data.mTimeStamp, data.mLat, data.mLon);
              node.addTag("name", StringUtils.flatString(data.mName));
              exportFile.addNode(node);
            }

            // Write file.
            if (!exportFile.writeFile())
            {
              toastString = getString(R.string.error_writing_to_export_file_zz, fileName) +
                            "(" + exportFile.getErrorString() + ")";
            }
            else if (!exportFile.close())
            {
              toastString = getString(R.string.error_closing_export_file_zz, fileName) +
                            "(" + exportFile.getErrorString() + ")";
            }
            else
            {
              toastString = getString(R.string.exported_xx_map_markers_to_file_zz,
                                      markersData.size(), fileName);
            }
          }
        }
      }

      Toast.makeText(this, toastString, Toast.LENGTH_LONG).show();
    }
  }
}
