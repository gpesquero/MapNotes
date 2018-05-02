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

public class BookMarkDialogFragment extends DialogFragment implements View.OnClickListener {

    Button mButtonOk=null;
    Button mButtonCancel=null;

    TextView mTextViewLatValue=null;
    TextView mTextViewLonValue=null;

    EditText mEditTextDescription=null;

    public void DialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyFragmentStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_bookmark, container, false);

        getDialog().setTitle("Bookmark");

        mButtonOk=v.findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(this);

        mButtonCancel=v.findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(this);

        float lat=getArguments().getFloat(getActivity().getString(R.string.key_lat));
        float lon=getArguments().getFloat(getActivity().getString(R.string.key_lon));

        mTextViewLatValue=v.findViewById(R.id.textViewLatValue);
        mTextViewLatValue.setText(Float.toString(lat));

        mTextViewLonValue=v.findViewById(R.id.textViewLonValue);
        mTextViewLonValue.setText(Float.toString(lon));

        mEditTextDescription=v.findViewById(R.id.editTextDescription);
        mEditTextDescription.setText("Bookmark Text");
        mEditTextDescription.selectAll();
        mEditTextDescription.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return v;
    }


    @Override
    public void onClick(View view) {

        if (view==mButtonOk) {

            this.dismiss();
        }
        else if (view==mButtonCancel) {

            this.dismiss();
        }
    }
}
