package com.example.practice4;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class BackConfirmDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("確認");
        builder.setMessage("入力内容が破棄されますが、よろしいですか？");
        builder.setPositiveButton("はい", new DialogButtonClickListener());
        builder.setNegativeButton("いいえ", new DialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public class DialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which){
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
