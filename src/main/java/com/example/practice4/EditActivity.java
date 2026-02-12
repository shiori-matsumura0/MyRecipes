package com.example.practice4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private TextView tvEditName;
    private EditText etEditIngredients;
    private EditText etEditRecipe;
    private EditText etEditCookingTime;
    private Spinner spEditCategory;
    private DatabaseHelper _helper;
    private String recipeName;
    private ImageView ivEditImage;
    private Uri _imageUri;
    private Button btEditSelectImage;

    ActivityResultLauncher<Intent> _cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallbackFromCamera());

    ActivityResultLauncher<Intent> _galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallbackFromGallery());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setTitle("レシピ編集");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recipeName = getIntent().getStringExtra("recipeName");
        tvEditName = findViewById(R.id.tvEditName);
        tvEditName.setText(recipeName);

        etEditIngredients = findViewById(R.id.etEditIngredients);
        etEditRecipe = findViewById(R.id.etEditRecipe);
        etEditCookingTime = findViewById(R.id.etEditCookingTime);
        ivEditImage = findViewById(R.id.ivEditImage);
        btEditSelectImage = findViewById(R.id.btEditSelectImage);
        spEditCategory = findViewById(R.id.spEditCategory);

        btEditSelectImage.setOnClickListener(new SelectImageClickListener());

        _helper = new DatabaseHelper(EditActivity.this);
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT * FROM recipe WHERE name = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{recipeName});
        if(cursor.moveToFirst()) {
            int idxIngredients = cursor.getColumnIndex("ingredients");
            String ingredients = cursor.getString(idxIngredients);
            etEditIngredients.setText(ingredients);

            int idxRecipe = cursor.getColumnIndex("recipe");
            String recipe = cursor.getString(idxRecipe);
            etEditRecipe.setText(recipe);

            int idxCategory = cursor.getColumnIndex("category");
            String category = cursor.getString(idxCategory);

            // Spinnerに設定されているAdapterから、その文字列が何番目にあるか探す
            ArrayAdapter adapter = (ArrayAdapter) spEditCategory.getAdapter();
            int position = adapter.getPosition(category);
            // 見つかった位置（Index）をSpinnerにセットする

            if (position >= 0) {
                spEditCategory.setSelection(position);
            }

            int idxCookingTime = cursor.getColumnIndex("cooking_time");
            String cookingTime = cursor.getString(idxCookingTime);
            etEditCookingTime.setText(cookingTime);

            int idxImage = cursor.getColumnIndex("image_uri");
            String imagePath = cursor.getString(idxImage);
            if (imagePath != null && !imagePath.isEmpty()) {
                _imageUri = Uri.parse(imagePath);
                ivEditImage.setImageURI(Uri.parse(imagePath));
            } else {
                ivEditImage.setImageResource(R.drawable.no_image);
            }
        }
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_edit, menu);
        return true;
    }

    public void updateRecipe(){
        String ingredients = etEditIngredients.getText().toString();
        String recipe = etEditRecipe.getText().toString();
        String timeStr = etEditCookingTime.getText().toString();
        int time = Integer.parseInt(timeStr);
        String selectedCategory = spEditCategory.getSelectedItem().toString();
        String imagePath = null;
        if (_imageUri != null) {
            imagePath = _imageUri.toString();
        }

        SQLiteDatabase db = _helper.getWritableDatabase();

        String sqlUpdate = "UPDATE recipe SET ingredients=?, recipe=?, cooking_time=?, updated_at=?, " +
                "category=?, image_uri=? WHERE name=?";
        SQLiteStatement stmt = db.compileStatement(sqlUpdate);
        stmt.bindString(1, ingredients);
        stmt.bindString(2, recipe);
        stmt.bindLong(3, time);
        stmt.bindLong(4, System.currentTimeMillis());
        stmt.bindString(5, selectedCategory);
        if (imagePath != null) {
            stmt.bindString(6, imagePath);
        } else {
            stmt.bindNull(6);
        }
        stmt.bindString(7, recipeName);

        stmt.executeUpdateDelete();

        Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            showConfirmDialog();
            return true;
        }else if(itemId == R.id.menu_edit_save){
            UpdateConfirmDialogFragment dialogFragment = new UpdateConfirmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "UpdateConfirmDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageChoicePopup(View anchor) {
        PopupMenu popup = new PopupMenu(EditActivity.this, anchor);
        popup.getMenu().add("写真を撮る");
        popup.getMenu().add("ギャラリーから選択");
        if (_imageUri != null) {
            popup.getMenu().add("画像をクリア");
        }

        popup.setOnMenuItemClickListener(new ImageChoiceMenuClickListener());
        popup.show();
    }

    private void showConfirmDialog() {
        BackConfirmDialogFragment dialogFragment = new BackConfirmDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "BackConfirmDialog Fragment");
    }

    private class ImageChoiceMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String title = item.getTitle().toString();
            if (title.equals("写真を撮る")) {
                takePhoto();
            } else if (title.equals("ギャラリーから選択")) {
                chooseFromGallery();
            } else if (title.equals("画像をクリア")) {
                clearImage();
            }
            return true;
        }
    }

    private void takePhoto() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN);
        Date now = new Date(System.currentTimeMillis());
        String nowString = dataFormat.format(now);
        String fileName = "IMG_" + nowString + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        ContentResolver resolver = getContentResolver();
        _imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (_imageUri == null) {
            Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
        _cameraLauncher.launch(intent);
    }

    private class ActivityResultCallbackFromCamera implements ActivityResultCallback<ActivityResult> {
        @Override
        public void onActivityResult(ActivityResult result){
            if(result.getResultCode() == RESULT_OK){
                ivEditImage.setImageURI(_imageUri);
            }
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        _galleryLauncher.launch(intent);
    }

    private class ActivityResultCallbackFromGallery
            implements ActivityResultCallback<ActivityResult> {

        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {

                    _imageUri = data.getData();

                    // ★ 永続権限を取得（ここだけ）
                    getContentResolver().takePersistableUriPermission(
                            _imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                    ivEditImage.setImageURI(_imageUri);
                }
            }
        }
    }

    private void clearImage() {
        _imageUri = null;
        ivEditImage.setImageResource(R.drawable.no_image);
    }

    private class SelectImageClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showImageChoicePopup(view);
        }
    }

}