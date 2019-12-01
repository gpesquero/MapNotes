package osm.mapnotes;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class GoToDialogFragment extends AppCompatDialogFragment
    implements View.OnClickListener {

    Button mButtonGoToOldest;
    Button mButtonGoToNewest;

    Context mContext=null;
    OnGoToDialogListener mListener=null;

    // Container Activity must implement this interface
    public interface OnGoToDialogListener {

        void onGoToOldestMarker();
        void onGoToNewestMarker();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext=context;

        try {
            mListener = (OnGoToDialogListener)context;
        }
        catch (final ClassCastException e) {

            throw new ClassCastException(context.toString() +
                    " must implement OnGoToDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyFragmentStyle);

        /*
        if (savedInstanceState!=null) {

            mIsNewMarker = savedInstanceState.getBoolean(KEY_IS_NEW_MARKER);
            mLon = savedInstanceState.getDouble(KEY_LON);
            mLat = savedInstanceState.getDouble(KEY_LAT);
            mName = savedInstanceState.getString(KEY_NAME);
            mTimeStamp = savedInstanceState.getString(KEY_TIME_STAMP);
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_goto, container, false);

        getDialog().setTitle(R.string.go_to_);

        mButtonGoToOldest=v.findViewById(R.id.buttonGoToOldest);
        mButtonGoToOldest.setOnClickListener(this);

        mButtonGoToNewest=v.findViewById(R.id.buttonGoToNewest);
        mButtonGoToNewest.setOnClickListener(this);

        /*
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
        */

        return v;
    }

    @Override
    public void onClick(View view) {

        if (mListener==null) {

            Toast.makeText(mContext, "ERROR: mListener==null", Toast.LENGTH_LONG).show();

            return;
        }

        if (view==mButtonGoToOldest) {

            mListener.onGoToOldestMarker();

        }
        else if (view==mButtonGoToNewest) {

            mListener.onGoToNewestMarker();
        }

        this.dismiss();
    }
}
