package osm.mapnotes;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.views.overlay.Marker;

public class MarkerDialogFragment extends DialogFragment implements View.OnClickListener,
        DialogInterface.OnClickListener {

    Button mButtonOk=null;
    Button mButtonCancel=null;

    TextView mTextViewTimeStamp=null;

    TextView mTextViewLatValue=null;
    TextView mTextViewLonValue=null;

    EditText mEditTextName=null;

    ImageButton mImageButtonDelete=null;

    boolean mIsNewMarker=true;

    Context mContext=null;

    // Container Activity must implement this interface
    public interface OnMarkerDialogListener {

        public void onNewMarker(MyMarker marker);
        public void onDeleteMarker(MyMarker marker);
    }

    OnMarkerDialogListener mListener=null;

    MyMarker mMarker=null;

    public void BookMarkDialogFragment(Marker marker) {

        mMarker=null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext=context;

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

        mImageButtonDelete=v.findViewById(R.id.imageButtonDelete);
        mImageButtonDelete.setOnClickListener(this);

        mImageButtonDelete.setVisibility(mIsNewMarker? View.INVISIBLE : View.VISIBLE);

        mTextViewTimeStamp=v.findViewById(R.id.textViewTimeStamp);
        mTextViewTimeStamp.setText(mMarker.getId());

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

        //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);

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

            //mListener.onHideSoftInput();
        }
        else if (view==mButtonCancel) {

            this.dismiss();

            //mListener.onHideSoftInput();

            //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        else if (view==mImageButtonDelete) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setMessage(R.string.are_you_sure).setNegativeButton(android.R.string.cancel, this)
                    .setPositiveButton(R.string.delete, this).show();
        }
    }

    public void setMarker(MyMarker marker) {

        mMarker=marker;
    }

    public void setNewMarker(boolean isNewMarker) {

        mIsNewMarker=isNewMarker;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which==DialogInterface.BUTTON_POSITIVE) {

            mListener.onDeleteMarker(mMarker);

            this.dismiss();
        }
    }
}
