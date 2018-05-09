package osm.mapnotes;


import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.osmdroid.views.overlay.Marker;

public class MarkerDialogFragment extends DialogFragment implements View.OnClickListener {

    Button mButtonOk=null;
    Button mButtonCancel=null;

    TextView mTextViewLatValue=null;
    TextView mTextViewLonValue=null;

    EditText mEditTextName=null;

    boolean mIsNewMarker=true;

    // Container Activity must implement this interface
    public interface OnMarkerDialogListener {

        public void onNewMarker(Marker marker);
    }

    OnMarkerDialogListener mListener=null;

    Marker mMarker=null;

    public void BookMarkDialogFragment(Marker marker) {

        mMarker=null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnMarkerDialogListener)context;
        }
        catch (final ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement OnBookMarkDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyFragmentStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_marker, container, false);

        getDialog().setTitle(R.string.marker);

        mButtonOk=v.findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(this);

        mButtonCancel=v.findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(this);

        //double lat=getArguments().getDouble(getActivity().getString(R.string.key_lat));
        //double lon=getArguments().getDouble(getActivity().getString(R.string.key_lon));

        double lat=mMarker.getPosition().getLatitude();
        double lon=mMarker.getPosition().getLongitude();

        mTextViewLatValue=v.findViewById(R.id.textViewLatValue);
        mTextViewLatValue.setText(String.format("%01.6f", lat));

        mTextViewLonValue=v.findViewById(R.id.textViewLonValue);
        mTextViewLonValue.setText(String.format("%01.6f", lon));

        mEditTextName=v.findViewById(R.id.editTextName);
        mEditTextName.setText(mMarker.getTitle());
        mEditTextName.selectAll();
        mEditTextName.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return v;
    }


    @Override
    public void onClick(View view) {

        if (view==mButtonOk) {

            mMarker.setTitle(mEditTextName.getText().toString());

            if (mIsNewMarker) {

                mListener.onNewMarker(mMarker);
            }

            this.dismiss();
        }
        else if (view==mButtonCancel) {

            this.dismiss();
        }
    }

    public void setMarker(Marker marker) {

        mMarker=marker;
    }

    public void setNewMarker(boolean isNewMarker) {

        mIsNewMarker=isNewMarker;
    }
}
