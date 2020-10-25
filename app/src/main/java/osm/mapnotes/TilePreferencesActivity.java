package osm.mapnotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class TilePreferencesActivity extends AppCompatActivity
        implements View.OnClickListener, OnClickListener {

    RadioGroup mRadioGroupTileSource;

    Button mButtonClearTileCache;

    static int RESULT_CLEAR_TILE_CACHE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_preferences);

        mRadioGroupTileSource=findViewById(R.id.radioGroupTileSource);
        RadioButton radioButton=(RadioButton)mRadioGroupTileSource.getChildAt(
                MainActivity.mPreferences.mTileSource+1);
        radioButton.setChecked(true);

        mButtonClearTileCache=findViewById(R.id.buttonClearTileCache);
        mButtonClearTileCache.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        int id=mRadioGroupTileSource.getCheckedRadioButtonId();

        if (id<0) {

            MainActivity.mPreferences.mTileSource=MyPreferences.TILE_SOURCE_DEFAULT;
        }
        else {

            RadioButton radioButton=findViewById(id);
            MainActivity.mPreferences.mTileSource=mRadioGroupTileSource.indexOfChild(radioButton)-1;
        }
    }

    @Override
    public void onClick(View view) {

        if (view == mButtonClearTileCache) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.are_you_sure_that_you_want_to_clear_tile_cache)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setPositiveButton(R.string.clear, this).show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which==DialogInterface.BUTTON_POSITIVE) {

            setResult(RESULT_CLEAR_TILE_CACHE);

            finish();
        }
    }
}