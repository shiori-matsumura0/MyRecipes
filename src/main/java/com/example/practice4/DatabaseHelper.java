package com.example.practice4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "recipebook.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE recipe (");
        sb.append("_id INTEGER PRIMARY KEY,");
        sb.append("name TEXT,");            //レシピ名
        sb.append("is_favorite INTEGER,");  //お気に入り
        sb.append("ingredients TEXT,");     //材料
        sb.append("recipe TEXT,");          //レシピ
        sb.append("memo TEXT,");            //メモ
        sb.append("category TEXT,");        //カテゴリー
        sb.append("cooking_time INTEGER,"); //調理時間（分単位）
        sb.append("image_uri TEXT,");       //画像パス
        sb.append("created_at INTEGER,");   //登録日時
        sb.append("updated_at INTEGER,");   //更新日時
//        sb.append("category TEXT,");        //カテゴリー
        sb.append("main_ingredient TEXT,"); //カテゴリー
        sb.append("dish_type TEXT,");       //カテゴリー
        sb.append("image_path TEXT");       //写真
        sb.append(");");
        String sql = sb.toString();

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}
}
