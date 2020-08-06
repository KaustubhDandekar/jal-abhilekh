package com.neeri.wbis.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.neeri.wbis.tools.RecordObject;

import java.util.ArrayList;

import static com.neeri.wbis.database.DataBaseConstants.*;

public class DatabaseHelper extends SQLiteOpenHelper{

    public DatabaseHelper(Context context){
        super(context,"TileDB",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqdb){
        sqdb.execSQL("create table IF NOT EXISTS records("+
                R_ID+" integer primary key AUTOINCREMENT NOT NULL, "+
                TITLE+" text, "+
                DESCRIPTION+" integer, " +
                LATITUDE+" real, " +
                LONGITUDE+" real, " +
                U_NAME+" text, " +
                U_CONTACT+" text, " +
                U_EMAIL+" text)");

        sqdb.execSQL("create table IF NOT EXISTS images("+
                R_ID+" integer primary key, " +
                IMAGE+" text" +
                ");");

        sqdb.execSQL("CREATE table IF NOT EXISTS profile(" +
                "u_id integer primary key, " +
                U_NAME+" text, " +
                U_CONTACT+" text, " +
                U_EMAIL+" text);");

        sqdb.execSQL("CREATE table IF NOT EXISTS circulars(" +
                "c_id integer primary key, " +
                CIRCULAR_NAME+" text, " +
                CIRCULAR_SIZE+" text);");

        sqdb.execSQL("INSERT INTO profile(u_id ,"+U_NAME+", "+U_CONTACT+", "+U_EMAIL+") VALUES(1 ,'', '', '');");

    }
    @Override
    public void onUpgrade(SQLiteDatabase sqdb,int oldversion,int newversion){
        sqdb.execSQL("DROP TABLE IF EXISTS score");

        onCreate(sqdb);
    }

    public boolean saveRecord(RecordObject record){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues recordContents = new ContentValues();
        recordContents.put(TITLE, record.getTitle());
        recordContents.put(DESCRIPTION, record.getDescription());
        recordContents.put(LATITUDE, record.getLatitude());
        recordContents.put(LONGITUDE, record.getLongitude());
        recordContents.put(U_NAME, record.getUName());
        recordContents.put(U_CONTACT, record.getUContact());
        recordContents.put(U_EMAIL, record.getUEmail());
//        recordContents.put(R_ID, record.getTitle());

        long r_id = db.insert("records", null, recordContents);

        ContentValues recordImagesContent = new ContentValues();
        recordImagesContent.put(R_ID, r_id);
        ArrayList<String> images = record.getImages();
        for (String image : images) {
            recordImagesContent.put(IMAGE, image);
            db.insert("images", null, recordImagesContent);
        }

        return true;
    }

    public Cursor getDraftInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select records."+R_ID+", records."+TITLE+", records."+DESCRIPTION
                +" from records ",null );
        return res;
    }

    public Cursor[] getRecordDetails(String record_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data =  db.rawQuery( "select * from records WHERE records."+R_ID+" = ?", new String[]{record_id});
        Cursor images =  db.rawQuery("SELECT "+IMAGE+" FROM images WHERE "+R_ID+" = ?", new String[]{record_id});

        return new Cursor[]{data, images};
    }

    public boolean deleteRecord(String record_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try{
            db.delete("records", R_ID+" = ?", new String[]{record_id});
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean updateUserInfo(String uname, String uemail, String ucontact){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contents = new ContentValues();
        contents.put(U_NAME, uname);
        contents.put(U_EMAIL, uemail);
        contents.put(U_CONTACT, ucontact);

        try{
            db.update("profile", contents, "u_id = ?", new String[]{"1"});
            return true;
        }catch (Exception e){
            Log.e("DATABASE UPDATE",e.getMessage());
            return false;
        }
    }

    public Cursor getUserInfo(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT "+U_NAME+","+U_EMAIL+","+U_CONTACT+" FROM profile WHERE u_id = 1",null );
        return res;
    }

    public boolean saveCircularSize(String name, String size){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues recordContents = new ContentValues();
        recordContents.put(CIRCULAR_NAME, name);
        recordContents.put(CIRCULAR_SIZE, size);
        db.insert("circulars", null, recordContents);
        return true;
    }

    public long getCircularSize(String filename){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT "+CIRCULAR_SIZE+" FROM circulars WHERE "+CIRCULAR_NAME+" = ?",new String[]{filename});
        res.moveToNext();
        return Long.parseLong(res.getString(res.getColumnIndex(CIRCULAR_SIZE)));
    }

}