package ua.com.curl.web.imageAdder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Vadik on 30.10.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbShop";
    public static final String TABLE_CONTACTS = "products";
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME_PHOTO = "namePhotoProduct";
    public static final String KEY_POSITION = "position";
    public static final String KEY_ID_PRODUCT = "idProduct";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_CONTACTS + "(" + KEY_ID
                + " integer primary key, " + KEY_NAME_PHOTO + " text, " + KEY_POSITION + " integer, "+ KEY_ID_PRODUCT + " integer " + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_CONTACTS);
        onCreate(db);
    }

}
