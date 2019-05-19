package osm.mapnotes;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MarkerDialogFragment extends AppCompatDialogFragment implements View.OnClickListener,
        DialogInterface.OnClickListener {

    // Controls

    TextView mTextViewTimeStamp=null;

    TextView mTextViewLatValue=null;
    TextView mTextViewLonValue=null;

    EditText mEditTextName=null;

    ImageButton mImageButtonDelete=null;
    ImageButton mImageButtonOk=null;

    // Constants
    public static String KEY_IS_NEW_MARKER="key_is_new_marker";
    public static String KEY_LON="key_lon";
    public static String KEY_LAT="key_lat";
    public static String KEY_NAME="key_name";
    public static String KEY_TIME_STAMP="key_time_stamp";

    // Variables

    boolean mIsNewMarker=true;
    public double mLon=0.0;
    public double mLat=0.0;
    public String mName="";
    public String mTimeStamp="";

    Context mContext=null;
    OnMarkerDialogListener mListener=null;

    // Container Activity must implement this interface
    public interface OnMarkerDialogListener {

        void onNewMarker(Bundle markerDataBundle);
        void onDeleteMarker(String markerTimeStamp);
        void onUpdateMarker(Bundle markerDataBundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext=context;

        try {
            mListener = (OnMarkerDialogListener)context;
        }
        catch (final ClassCastException e) {

            throw new ClassCastException(context.toString() +
                    " must implement OnBookMarkDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyFragmentStyle);

        if (savedInstanceState!=null) {

            mIsNewMarker = savedInstanceState.getBoolean(KEY_IS_NEW_MARKER);
            mLon = savedInstanceState.getDouble(KEY_LON);
            mLat = savedInstanceState.getDouble(KEY_LAT);
            mName = savedInstanceState.getString(KEY_NAME);
            mTimeStamp = savedInstanceState.getString(KEY_TIME_STAMP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_marker, container, false);

        getDialog().setTitle(R.string.marker);

        mImageButtonOk=v.findViewById(R.id.imageButtonOk);
        mImageButtonOk.setOnClickListener(this);

        mImageButtonDelete=v.findViewById(R.id.imageButtonDelete);
        mImageButtonDelete.setOnClickListener(this);

        mImageButtonDelete.setVisibility(mIsNewMarker? View.INVISIBLE : View.VISIBLE);

        mTextViewTimeStamp=v.findViewById(R.id.textViewTimeStamp);
        mTextViewTimeStamp.setText(mTimeStamp);

        mTextViewLatValue=v.findViewById(R.id.textViewLatValue);
        mTextViewLatValue.setText(String.format("%01.6f\u00B0", mLat)+" ("+
                        Util.formatLatitude(mLat)+")");

        mTextViewLonValue=v.findViewById(R.id.textViewLonValue);
        mTextViewLonValue.setText(String.format("%01.6f\u00B0", mLon)+" ("+
                        Util.formatLongitude(mLon)+")");

        mEditTextName=v.findViewById(R.id.editTextName);
        mEditTextName.setText(mName);
        mEditTextName.selectAll();
        mEditTextName.requestFocus();

        return v;
    }

    @Override
    public void onClick(View view) {

        if (view==mImageButtonOk) {

            mName=mEditTextName.getText().toString();

            Bundle markerDataBundle=new Bundle();

            markerDataBundle.putDouble(KEY_LON, mLon);
            markerDataBundle.putDouble(KEY_LAT, mLat);
            markerDataBundle.putString(KEY_NAME, mName);
            markerDataBundle.putString(KEY_TIME_STAMP, mTimeStamp);

            if (mListener==null) {

                Toast.makeText(mContext, "ERROR: mListener==null", Toast.LENGTH_LONG).show();
            }

            if (mIsNewMarker) {

                mListener.onNewMarker(markerDataBundle);
            }
            else {

                mListener.onUpdateMarker(markerDataBundle);
            }

            this.dismiss();
        }
        else if (view==mImageButtonDelete) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setMessage(R.string.are_you_sure_that_you_want_to_delete_marker)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setPositiveButton(R.string.delete, this).show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which==DialogInterface.BUTTON_POSITIVE) {

            mListener.onDeleteMarker(mTimeStamp);

            this.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {

        outState.putBoolean(KEY_IS_NEW_MARKER, mIsNewMarker);
        outState.putDouble(KEY_LON, mLon);
        outState.putDouble(KEY_LAT, mLat);
        outState.putString(KEY_NAME, mName);
        outState.putString(KEY_TIME_STAMP, mTimeStamp);
    }
}
