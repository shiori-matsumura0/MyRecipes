package com.example.practice4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DetailActivity extends AppCompatActivity {

    private String recipeName;
    private DatabaseHelper _helper;
    private boolean isFavorite;
    private TextView tvDetailCreatedAt;
    private TextView tvDetailUpdatedAt;
    private TextView tvDetailCookingTime;
    private TextView tvDetailIngredients;
    private TextView tvDetailRecipe;
    private TextView tvDetailRecipeName;
    private TextView tvDetailCategory;
    private MaterialCardView cardDetailImage;
    private ImageView ivDetailImage;
    private MenuItem menuFavorite;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("レシピ詳細");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        recipeName = intent.getStringExtra("recipeName");

        tvDetailRecipeName = findViewById(R.id.tvDetailRecipeName);
        tvDetailRecipeName.setText(recipeName);

        _helper = new DatabaseHelper(DetailActivity.this);

        tvDetailCreatedAt = findViewById(R.id.tvDetailCreatedAt);
        tvDetailUpdatedAt = findViewById(R.id.tvDetailUpdatedAt);
        tvDetailCookingTime = findViewById(R.id.tvDetailCookingTime);
        tvDetailIngredients = findViewById(R.id.tvDetailIngredients);
        tvDetailRecipe = findViewById(R.id.tvDetailRecipe);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);

        cardDetailImage = findViewById(R.id.cardDetailImage);
        ivDetailImage = findViewById(R.id.ivDetailImage);

        loadRecipeDetail();
        loadFavoriteState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_detail, menu);
        menuFavorite = menu.findItem(R.id.menu_detail_favorite);
        updateFavoriteIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == R.id.menu_detail_favorite) {
            onFavoriteClicked();
            return true;
        }
        else if (itemId == R.id.menu_detail_edit) {
            Intent intent = new Intent(DetailActivity.this, EditActivity.class);
            intent.putExtra("recipeName", tvDetailRecipeName.getText().toString());
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menuListOptionDelete) {
            DeleteConfirmDialogFragment dialogFragment = new DeleteConfirmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "DeleteConfirmDialogFragment");
            return true;
        }
        else if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String transformDatetime(Long datetime) {
        Date date = new Date(datetime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        String formatted = sdf.format(date);
        return formatted;
    }

    public void onFavoriteClicked() {
        toggleFavorite();
        updateFavoriteIcon();
    }


    private void loadFavoriteState() {
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT is_favorite FROM recipe WHERE name = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{recipeName});

        if (cursor.moveToFirst()) {
            isFavorite = cursor.getInt(0) == 1;
        }

        cursor.close();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;

        SQLiteDatabase db = _helper.getWritableDatabase();
        String sqlUpdate = "UPDATE recipe SET is_favorite = ?, updated_at = ? WHERE name = ?";
        SQLiteStatement stmt = db.compileStatement(sqlUpdate);
        stmt.bindLong(1, isFavorite ? 1:0);
        stmt.bindLong(2, System.currentTimeMillis());
        stmt.bindString(3, recipeName);
        stmt.executeUpdateDelete();

        tvDetailUpdatedAt.setText(transformDatetime(System.currentTimeMillis()));
    }

    private void updateFavoriteIcon() {
        if (menuFavorite == null) {
            return;
        }
        if (isFavorite) {
            menuFavorite.setIcon(R.drawable.ic_baseline_favorite_24);
        } else {
            menuFavorite.setIcon(R.drawable.ic_baseline_favorite_border_24);
        }
    }

    @Override
    protected void onDestroy() {
        _helper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipeDetail();
    }

    private void loadRecipeDetail(){
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT * FROM recipe WHERE name = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{recipeName});
        if(cursor.moveToFirst()){
            int idxCreated = cursor.getColumnIndex("created_at");
            Long createdAt = cursor.getLong(idxCreated);
            tvDetailCreatedAt.setText(transformDatetime(createdAt));

            int idxUpdated = cursor.getColumnIndex("updated_at");
            Long updatedAt = cursor.getLong(idxUpdated);

            if (updatedAt == 0) {
                tvDetailUpdatedAt.setText("0000-00-00 00:00");
            } else {
                tvDetailUpdatedAt.setText(transformDatetime(updatedAt));
            }

            int idxCookingTime = cursor.getColumnIndex("cooking_time");
            String cookingTime = cursor.getString(idxCookingTime);
            tvDetailCookingTime.setText(cookingTime+"分");

            int idxIngredients = cursor.getColumnIndex("ingredients");
            String ingredients = cursor.getString(idxIngredients);
            tvDetailIngredients.setText(ingredients);

            int idxRecipe = cursor.getColumnIndex("recipe");
            String recipe = cursor.getString(idxRecipe);

            String formatted = recipe.replaceAll("(?m)^(\\d+\\.)", "$1 ");
            SpannableString spannable = new SpannableString(formatted);

            // ぶら下げインデント（数字の分だけ余白を作る）
            int margin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp
            spannable.setSpan(
                    new LeadingMarginSpan.Standard(0, margin),
                    0,
                    spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            tvDetailRecipe.setText(spannable);

            int idxCategory = cursor.getColumnIndex("category");
            String category = cursor.getString(idxCategory);
            tvDetailCategory.setText(category);

            int idxImage = cursor.getColumnIndex("image_uri");
            String image = cursor.getString(idxImage);

            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) cardDetailImage.getLayoutParams();

            if (image != null && !image.isEmpty() && !"null".equals(image)) {
                ivDetailImage.setImageURI(Uri.parse(image));
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                ivDetailImage.setImageResource(R.drawable.no_image);
                params.height = 0;
            }
            cardDetailImage.setLayoutParams(params);
        }
        cursor.close();
    }

    public void deleteRecipe(){
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sqlDelete = "DELETE FROM recipe WHERE name = ?";
        SQLiteStatement stmt = db.compileStatement(sqlDelete);
        stmt.bindString(1, recipeName);
        stmt.executeUpdateDelete();
    }
}