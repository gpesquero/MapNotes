package osm.mapnotes;

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
