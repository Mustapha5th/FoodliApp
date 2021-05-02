package com.example.foodliapp.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.foodliapp.Model.Favorites;
import com.example.foodliapp.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {
    private static final String DB_NAME = "foodsdeliveryDB.db";
    private static final int DB_VER=1;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCart(String userPhone){
        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","ProductName", "ProductID", "Quantity", "Price", "Discount", "Image"};
        String sqlTable = "OrderDetail";

        queryBuilder.setTables(sqlTable);
        Cursor cursor = queryBuilder.query(database, sqlSelect,"UserPhone=?",new String[]{userPhone},null,null,null);
        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                result.add(new Order(

                        cursor.getString(cursor.getColumnIndex("UserPhone")),
                        cursor.getString(cursor.getColumnIndex("ProductID")),
                        cursor.getString(cursor.getColumnIndex("ProductName")),
                       cursor.getString(cursor.getColumnIndex("Quantity")),
                       cursor.getString(cursor.getColumnIndex("Price")),
                        cursor.getString(cursor.getColumnIndex("Discount")),
                        cursor.getString(cursor.getColumnIndex("Image"))
            ));
            }while (cursor.moveToNext());
        }
        return result;

    }
    public void addToCart(Order order){
        SQLiteDatabase database = getReadableDatabase();

        // deleted query
        String query = String.format("INSERT OR REPLACE INTO OrderDetail(UserPhone,ProductID,ProductName,Quantity,Price,Discount,Image) VALUES('%s','%s','%s','%s','%s','%s','%s');",

//        String query = String.format("INSERT INTO OrderDetail(UserPhone,ProductID,ProductName,Quantity,Price,Discount,Image) VALUES('%s','%s','%s','%s','%s','%s','%s');",
        order.getUserPhone(),
        order.getProductId(),
        order.getProductName(),
        order.getQuantity(),
        order.getPrice(),
        order.getDiscount(),
        order.getImage());
        database.execSQL(query);
    }
    public void cleanCart(String userPhone){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone='%s'", userPhone);
        database.execSQL(query);
    }
    // Favorites
    public void addToFavorites(Favorites food){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(foodId,foodName,foodPrice,foodMenuId,foodImage,foodDiscount,foodDescription, userPhone) VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                food.getFoodId(),
                food.getFoodName(),
                food.getFoodPrice(),
                food.getFoodMenuId(),
                food.getFoodImage(),
                food.getFoodDiscount(),
                food.getFoodDescription(),
                food.getUserPhone());
        db.execSQL(query);
    }
    public void deleteFromFavorites(String foodId, String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE foodId='%s' and userPhone = '%s'", foodId, userPhone);
        db.execSQL(query);
    }
    public boolean isFavorites(String foodId, String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE foodId='%s' and userPhone='%s'", foodId, userPhone);
       Cursor cursor = db.rawQuery(query,null);
       if (cursor.getCount() <= 0){
           cursor.close();
           return false;
       }
       cursor.close();
       return true;
    }
    public int getCount(String userPhone){
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail WHERE UserPhone='%s", userPhone);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                count = cursor.getInt(0);
            }while (cursor.moveToNext());
        }
        return count;
    }
    public boolean checkFoodExists(String foodId, String userPhone){
        boolean flag = false;
        SQLiteDatabase database = getReadableDatabase();
       Cursor cursor = null;
       String SQLQuery =  String.format("SELECT * FROM OrderDetail WHERE UserPhone ='%s' AND ProductID='%s'", userPhone,foodId);
       cursor = database.rawQuery(SQLQuery,null);

        flag = cursor.getCount() > 0;
       cursor.close();

       return flag;
    }
    public void deleteFromCart(String productId, String phone) {
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone='%s' AND ProductID='%s'", phone, productId);
        database.execSQL(query);
    }
    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity='%s' WHERE UserPhone = '%s' AND ProductID='%s", order.getQuantity(),order.getUserPhone(),order.getProductId());
        db.execSQL(query);
    }
    public void increaseCart(String userPhone, String foodId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity = Quantity+1 WHERE UserPhone = '%s' AND ProductID='%s'", userPhone,foodId);
        db.execSQL(query);
    }

    public List<Favorites> getAllFavorites(String userPhone){
        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"foodId","foodName", "foodPrice", "foodMenuId", "foodImage", "foodDiscount", "foodDescription","userPhone"};
        String sqlTable = "Favorites";

        queryBuilder.setTables(sqlTable);
        Cursor cursor = queryBuilder.query(database, sqlSelect,"userPhone=?",new String[]{userPhone},null,null,null);
        final List<Favorites> result = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                result.add(new Favorites(

                        cursor.getString(cursor.getColumnIndex("foodId")),
                        cursor.getString(cursor.getColumnIndex("foodName")),
                        cursor.getString(cursor.getColumnIndex("foodPrice")),
                        cursor.getString(cursor.getColumnIndex("foodMenuId")),
                        cursor.getString(cursor.getColumnIndex("foodImage")),
                        cursor.getString(cursor.getColumnIndex("foodDiscount")),
                        cursor.getString(cursor.getColumnIndex("foodDescription")),
                        cursor.getString(cursor.getColumnIndex("userPhone"))
                ));
            }while (cursor.moveToNext());
        }
        return result;

    }
}
