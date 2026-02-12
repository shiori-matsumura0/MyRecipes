package com.example.practice4;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SearchDialogFragment extends DialogFragment {

    private EditText etSearchKeyword;
    private Spinner spSearchCategory;
    private CheckBox cbSearchFavorite;
    private EditText etSearchCookingTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_search, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("検索");
        builder.setView(dialogView);

        etSearchKeyword = dialogView.findViewById(R.id.etSearchKeyword);
        spSearchCategory = dialogView.findViewById(R.id.spSearchCategory);
        spSearchCategory.setSelection(0);
        cbSearchFavorite = dialogView.findViewById(R.id.cbSearchFavorite);
        etSearchCookingTime = dialogView.findViewById(R.id.etSearchCookingTime);

        Button btSearchCancel = dialogView.findViewById(R.id.btSearchCancel);
        btSearchCancel.setOnClickListener(new CancelClickListener());

        Button btSearchSearch = dialogView.findViewById(R.id.btSearchSearch);
        btSearchSearch.setOnClickListener(new SearchClickListener());

        AlertDialog dialog = builder.create();
        return dialog;
    }

    private class SearchClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            String keyword = etSearchKeyword.getText().toString();
            String category = spSearchCategory.getSelectedItem().toString();
            String cookingTime = etSearchCookingTime.getText().toString();
            Boolean favorite = cbSearchFavorite.isChecked();


            Intent intent = new Intent(getActivity(), MainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("keyword", keyword);
            bundle.putString("category", category);
            bundle.putString("cookingTime", cookingTime);
            bundle.putBoolean("favorite", favorite);

            intent.putExtras(bundle);
            //既存の MainActivity を再利用
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            dismiss();

        }
    }

    private class CancelClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            dismiss();
        }
    }

}
