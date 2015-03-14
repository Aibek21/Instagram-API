package ibecsystems.kz.instagram;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by aibek on 14.03.15.
 */
public class MyDialogFragment extends DialogFragment {


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .create();
    }
}

