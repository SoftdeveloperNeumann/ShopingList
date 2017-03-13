package com.example.softdeveloper.shopinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by softdeveloper on 28.02.2017.
 */

public class ShoppingMemoDataSource {

    private static final String LOG_TAG = ShoppingMemoDataSource.class.getSimpleName();
    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;
    private String[] columns = {
            ShoppingMemoDbHelper.COLUMN_ID,
            ShoppingMemoDbHelper.COLUMN_PRODUCT,
            ShoppingMemoDbHelper.COLUMN_QUANTITY,
            ShoppingMemoDbHelper.COLUMN_CHECKED
    };


    public ShoppingMemoDataSource(Context context){
        Log.d(LOG_TAG, "Der DbHelper wird erstellt");
        dbHelper= new ShoppingMemoDbHelper(context);
    }

    public ShoppingMemo createShopingMemo(String product, int quantity){
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,quantity);
        values.put(ShoppingMemoDbHelper.COLUMN_CHECKED,0);

        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,null,values);
        // SELECT id,product,quantity FROM shopping_list WHERE id = insertID
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + insertId,null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        Log.d(LOG_TAG,shoppingMemo.toString());
        cursor.close();
        return shoppingMemo;

    }

    public ShoppingMemo cursorToShoppingMemo(Cursor cursor){
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);
        int idChecked = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_CHECKED);

        String product = cursor.getString(idProduct);
        int quantity = cursor.getInt(idQuantity);
        long id = cursor.getLong(idIndex);
        int intValueChecked = cursor.getInt(idChecked);
        boolean isChecked = intValueChecked != 0;

        return new ShoppingMemo(id,product,quantity,isChecked);
    }

    public List<ShoppingMemo> getAllShoppingMemos(){
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                null,null,null,null,null);
        ShoppingMemo shoppingMemo;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            shoppingMemo = cursorToShoppingMemo(cursor);
            Log.d(LOG_TAG ,"get All: " + shoppingMemo.toString());
            shoppingMemoList.add(shoppingMemo);
            cursor.moveToNext();
        }
        cursor.close();
        return shoppingMemoList;
    }

    public ShoppingMemo updateShoppingMemo(long id, String newProduct, int newQuantity,boolean isChecked){
        int intValueChecked = (isChecked)?1:0;

        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,newProduct);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,newQuantity);
        values.put(ShoppingMemoDbHelper.COLUMN_CHECKED,intValueChecked);

        database.update(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,values,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,null);
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST, columns,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();
        return shoppingMemo;
    }

    public void deleteShoppingMemo(ShoppingMemo shoppingMemo){
        long id = shoppingMemo.getId();

        database.delete(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,ShoppingMemoDbHelper.COLUMN_ID +
        "=" + id ,null);
        Log.d(LOG_TAG,"Eintrag gelöscht! ID: " + id + "Inhalt: " + shoppingMemo.toString());
    }
    public void open(){
        Log.d(LOG_TAG, "Referenz auf DB wird angefragt");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "DB- Referenz erhalten, Pfad: " + database.getPath());
    }

    public void close(){
        dbHelper.close();
        Log.d(LOG_TAG, "DB mit hilfe des DbHelpers geschlossen" );
    }
}
