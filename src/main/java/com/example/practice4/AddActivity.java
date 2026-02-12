package com.example.practice4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    private DatabaseHelper _helper;
    private EditText etAddName;
    private EditText etAddIngredients;
    private EditText etAddRecipe;
    private EditText etAddCookingTime;
    private String selectedCategory;
    private Button btAddSelectImage;
    private ImageView ivAddImage;
    private Uri _imageUri;

    ActivityResultLauncher<Intent> _cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallbackFromCamera());

    ActivityResultLauncher<Intent> _galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallbackFromGallery());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        setTitle("レシピ追加");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        _helper = new DatabaseHelper(AddActivity.this);

        etAddName = findViewById(R.id.etAddName);
        etAddIngredients = findViewById(R.id.etAddIngredients);
        etAddRecipe = findViewById(R.id.etAddRecipe);
        etAddCookingTime = findViewById(R.id.etAddCookingTime);
        ivAddImage = findViewById(R.id.ivAddImage);

        btAddSelectImage = findViewById(R.id.btAddSelectImage);
        btAddSelectImage.setOnClickListener(new SelectImageClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_add, menu);
        return true;
    }

    @Override
    protected void onDestroy(){
        _helper.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (hasInput()) {
                showConfirmDialog();
            } else {
                finish();
            }
            return true;
        } else if(itemId == R.id.menu_add_save){
            String name = etAddName.getText().toString();
            String ingredients = etAddIngredients.getText().toString();
            String recipe = etAddRecipe.getText().toString();
            Spinner spAddCategory = findViewById(R.id.spAddCategory);
            selectedCategory = spAddCategory.getSelectedItem().toString();
            String timeStr = etAddCookingTime.getText().toString();
            int time = Integer.parseInt(timeStr);
            String imagePath = null;
            if (_imageUri != null) {
                imagePath = _imageUri.toString();
            }

            SQLiteDatabase db = _helper.getWritableDatabase();

            String sqlInsert = "INSERT INTO recipe (name, ingredients, recipe, cooking_time, is_favorite, created_at, category, image_uri)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement stmt = db.compileStatement(sqlInsert);
            stmt.bindString(1, name);
            stmt.bindString(2, ingredients);
            stmt.bindString(3, recipe);
            stmt.bindLong(4, time);
            stmt.bindLong(5, 0);
            stmt.bindLong(6, System.currentTimeMillis());
            stmt.bindString(7, selectedCategory);
            if (imagePath != null) {
                stmt.bindString(8, imagePath);
            } else {
                stmt.bindNull(8);
            }

            stmt.executeInsert();
            etAddName.setText("");
            etAddIngredients.setText("");
            etAddRecipe.setText("");
            etAddCookingTime.setText("");
            spAddCategory.setSelection(0);
            _imageUri = null;
            ivAddImage.setImageResource(R.drawable.no_image);

            Toast.makeText(AddActivity.this, "追加しました。", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasInput() {
        return !etAddName.getText().toString().trim().isEmpty()
                || !etAddIngredients.getText().toString().trim().isEmpty()
                || !etAddRecipe.getText().toString().trim().isEmpty()
                || !etAddCookingTime.getText().toString().trim().isEmpty()
                || _imageUri != null;
    }

    @Override
    public void onBackPressed() {
        if (hasInput()) {
            showConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showConfirmDialog() {
        BackConfirmDialogFragment dialogFragment = new BackConfirmDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "BackConfirmDialog Fragment");
    }

    private void showImageChoicePopup(View anchor) {
        PopupMenu popup = new PopupMenu(AddActivity.this, anchor);
        popup.getMenu().add("写真を撮る");
        popup.getMenu().add("ギャラリーから選択");
        if (_imageUri != null) {
            popup.getMenu().add("画像をクリア");
        }

        popup.setOnMenuItemClickListener(new ImageChoiceMenuClickListener());
        popup.show();
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

    private class ActivityResultCallbackFromCamera implements ActivityResultCallback<ActivityResult>{
        @Override
        public void onActivityResult(ActivityResult result){
            if(result.getResultCode() == RESULT_OK){
                ivAddImage.setImageURI(_imageUri);
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

                    ivAddImage.setImageURI(_imageUri);
                }
            }
        }
    }

    private void clearImage() {
        _imageUri = null;
        ivAddImage.setImageResource(R.drawable.no_image);
    }

    private class SelectImageClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showImageChoicePopup(view);
        }
    }
}

