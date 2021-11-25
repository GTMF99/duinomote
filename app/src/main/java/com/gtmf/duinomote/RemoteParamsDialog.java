package com.gtmf.duinomote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.AutoCompleteTextView;


public class RemoteParamsDialog extends AppCompatDialogFragment {
    private TextInputEditText mRemoteName;
    private AutoCompleteTextView mCategory;
    private RemoteParamsDialogListener mListener;
    private boolean mClickedButton = false;

    public boolean getClickedButton() {
        return this.mClickedButton;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(getActivity());

        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View mView = mInflater.inflate(R.layout.dialog_main_remoteparams, null);

        mCategory = mView.findViewById(R.id.dialog_main_dropdown);
        String categoriesArr[] = getResources().getStringArray(R.array.categories);
        mCategory.setAdapter(new ArrayAdapter(mView.getContext(), R.layout.dropdown_item, categoriesArr));

        mBuilder.setView(mView)
                .setTitle("Remote Params")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mClickedButton = false;
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mClickedButton = true;
                        String remoteName = mRemoteName.getText().toString();
                        String category = mCategory.getText().toString();
                        mListener.saveRemoteToList(RemoteParamsDialog.this, remoteName, category);
                        dialogInterface.dismiss();
                    }
                });

        mRemoteName = mView.findViewById(R.id.dialog_main_textinput);

        return mBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (RemoteParamsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ButtonParamsDialogListener");
        }
    }

    public interface RemoteParamsDialogListener {
        void saveRemoteToList(RemoteParamsDialog dialog, String name, String category);
    }

}