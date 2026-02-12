package com.example.practice4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<Map<String, Object>> _recipeList;
    private DatabaseHelper _helper;
    private String currentKeyword = null;
    private String currentCategory = null;
    private Long currentTime = null;
    private boolean currentOnlyFavorite = false;
    private TextView tvSearchCondition;
    private LinearLayout layoutSearchCondition;
    private ImageButton ibClearCondition;
    private String currentSort = "updated_desc";
    private RecyclerListAdapter _adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutSearchCondition = findViewById(R.id.layoutSearchCondition);
        tvSearchCondition = findViewById(R.id.tvSearchCondition);
        ibClearCondition = findViewById(R.id.ibClearCondition);

        ibClearCondition.setOnClickListener(new ClearButtonClickListener());

        _helper = new DatabaseHelper(MainActivity.this);

        RecyclerView rvRecipe = findViewById(R.id.rvRecipe);
        LinearLayoutManager layout = new LinearLayoutManager(MainActivity.this);
        rvRecipe.setLayoutManager(layout);

        _recipeList = new ArrayList<>();
        _adapter = new RecyclerListAdapter(_recipeList);
        rvRecipe.setAdapter(_adapter);

        DividerItemDecoration decorator = new DividerItemDecoration(MainActivity.this, layout.getOrientation());
        rvRecipe.addItemDecoration(decorator);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new FabAddClickListener());

        loadRecipeList(currentKeyword, currentCategory, currentTime, currentOnlyFavorite);
        updateSearchConditionView();
    }

    private class RecyclerListViewHolder extends RecyclerView.ViewHolder{
        public TextView _tvRecipeNameRow;
        public ImageView _ivImageRow;

        public RecyclerListViewHolder(View itemView){
            super(itemView);
            _tvRecipeNameRow = itemView.findViewById(R.id.tvRecipeNameRow);
            _ivImageRow = itemView.findViewById(R.id.ivImageRow);
        }
    }

    private class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListViewHolder> {
        private List<Map<String, Object>> _listData;

        public RecyclerListAdapter(List<Map<String, Object>> listData) {
            _listData = listData;
        }

        @Override
        public RecyclerListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.row, parent, false);
            view.setOnClickListener(new ItemClickListener());
            RecyclerListViewHolder holder = new RecyclerListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerListViewHolder holder, int position) {
            Map<String, Object> item = _listData.get(position);
            String recipeName = (String) item.get("name");
            holder._tvRecipeNameRow.setText(recipeName);

            String imageUriStr = (String) item.get("imageUri");
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                holder._ivImageRow.setImageURI(android.net.Uri.parse(imageUriStr));
                holder._ivImageRow.setVisibility(View.VISIBLE);
            } else {
                holder._ivImageRow.setImageResource(R.drawable.no_image);
                holder._ivImageRow.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return _listData.size();
        }
    }

        private class ItemClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
           TextView tvRecipeName = view.findViewById(R.id.tvRecipeNameRow);
            String recipeName = tvRecipeName.getText().toString();
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("recipeName", recipeName);
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        currentKeyword = intent.getStringExtra("keyword");
        currentCategory = intent.getStringExtra("category");
        currentOnlyFavorite = intent.getBooleanExtra("favorite", false);

        String cookingTime = intent.getStringExtra("cookingTime");
        currentTime = (cookingTime != null && !cookingTime.isEmpty())
                ? Long.parseLong(cookingTime)
                : null;

        loadRecipeList(currentKeyword, currentCategory, currentTime, currentOnlyFavorite);
        updateSearchConditionView();
    }

    private void updateSearchConditionView() {
        List<String> conditions = new ArrayList<>();

        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            conditions.add(currentKeyword);
        }
        if (currentCategory != null && !currentCategory.equals("未選択")) {
            conditions.add(currentCategory);
        }
        if (currentTime != null) {
            conditions.add(currentTime + "分以内");
        }
        if (currentOnlyFavorite) {
            conditions.add("お気に入り");
        }

        if (conditions.isEmpty()) {
            layoutSearchCondition.setVisibility(View.GONE);
        } else {
            tvSearchCondition.setText("検索条件: " + String.join(" / ", conditions));
            layoutSearchCondition.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy(){
        _helper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipeList(currentKeyword, currentCategory, currentTime, currentOnlyFavorite);
        updateSearchConditionView();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            SearchDialogFragment dialogFragment = new SearchDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "SearchDialogFragment");
            return true;
        }

        if (itemId == R.id.action_sort) {
            View anchor = findViewById(R.id.action_sort);
            showSortPopup(anchor);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);

        popup.getMenu().add("更新日（新しい順）");
        popup.getMenu().add("更新日（古い順）");
        popup.getMenu().add("作成日（新しい順）");
        popup.getMenu().add("作成日（古い順）");

        popup.setOnMenuItemClickListener(item -> {
        String title = item.getTitle().toString();
        changeSort(title);
        return true;
    });

    popup.show();
    }

    private void changeSort(String title) {
        switch (title) {
            case "更新日（新しい順）":
                currentSort = "updated_desc";
                break;
            case "更新日（古い順）":
                currentSort = "updated_asc";
                break;
            case "作成日（新しい順）":
                currentSort = "created_desc";
                break;
            case "作成日（古い順）":
                currentSort = "created_asc";
                break;
        }

        loadRecipeList(currentKeyword, currentCategory, currentTime, currentOnlyFavorite); // 一覧再読み込み
    }

    private class FabAddClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            startActivity(intent);
        }
    }

    private class ClearButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            currentKeyword = null;
            currentCategory = null;
            currentTime = null;
            currentOnlyFavorite = false;

            loadRecipeList(null, null, null, false);
            updateSearchConditionView();
        }
    }

    private void loadRecipeList(String keyword, String category, Long time, boolean onlyFavorite) {
        _recipeList.clear();

        SQLiteDatabase db = _helper.getReadableDatabase();

        StringBuilder sql = new StringBuilder(
                "SELECT name, image_uri FROM recipe WHERE 1=1"
        );
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND name LIKE ?");
            args.add("%" + keyword + "%");
        }

        if (category != null && !category.equals("未選択")) {
            sql.append(" AND category = ?");
            args.add(category);
        }

        if (time != null) {
            sql.append(" AND cooking_time <= ?");
            args.add(String.valueOf(time));
        }

        if (onlyFavorite) {
            sql.append(" AND is_favorite = 1");
        }

        switch (currentSort) {
            case "created_asc":
                sql.append(" ORDER BY created_at ASC");
                break;
            case "created_desc":
                sql.append(" ORDER BY created_at DESC");
                break;
            case "name_asc":
                sql.append(" ORDER BY name COLLATE NOCASE ASC");
                break;
            default:
                sql.append(" ORDER BY updated_at DESC");
                break;
        }

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

        int idxName = cursor.getColumnIndex("name");
        int idxImage = cursor.getColumnIndex("image_uri");

        while (cursor.moveToNext()) {
            Map<String, Object> recipe = new HashMap<>();
            recipe.put("name", cursor.getString(idxName));
            recipe.put("imageUri", cursor.getString(idxImage));
            _recipeList.add(recipe);
        }

        cursor.close();
        _adapter.notifyDataSetChanged();
    }


}