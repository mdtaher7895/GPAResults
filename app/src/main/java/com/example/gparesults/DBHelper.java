package com.example.gparesults;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ResultDB";
    // ভার্সন ৩ থেকে বাড়িয়ে ৪ করা হলো কলাম পরিবর্তনের জন্য
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_NAME = "students";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_TOTAL = "total";
    private static final String COL_GPA = "gpa";
    private static final String COL_GRADE = "grade";
    private static final String COL_POSITION = "position";
    private static final String COL_MARKS = "marks";
    private static final String COL_SUB_COUNT = "sub_count";
    // নতুন কলাম: শ্রেণির নাম রাখার জন্য
    private static final String COL_CLASS_NAME = "class_name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // নতুন কলামসহ টেবিল তৈরি
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER, " +
                COL_NAME + " TEXT, " +
                COL_CLASS_NAME + " TEXT, " + // শ্রেণির নাম কলাম
                COL_MARKS + " TEXT, " +
                COL_TOTAL + " INTEGER, " +
                COL_GPA + " REAL, " +
                COL_GRADE + " TEXT, " +
                COL_SUB_COUNT + " INTEGER, " +
                COL_POSITION + " INTEGER DEFAULT 0, " +
                "PRIMARY KEY (" + COL_ID + ", " + COL_CLASS_NAME + ", " + COL_SUB_COUNT + "))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ইনসার্ট মেথডে class_name যোগ করা হয়েছে
    public boolean insertOrUpdateResult(int roll, String name, String className, String allMarks, int total, double gpa, String grade, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, roll);
        values.put(COL_NAME, name);
        values.put(COL_CLASS_NAME, className); // ডাটা সেভ
        values.put(COL_MARKS, allMarks);
        values.put(COL_TOTAL, total);
        values.put(COL_GPA, gpa);
        values.put(COL_GRADE, grade);
        values.put(COL_SUB_COUNT, subCount);

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // ডাটা খুঁজে বের করার সময় এখন ক্লাস নেম এবং সাবজেক্ট কাউন্ট দুটিই লাগবে
    public Cursor getDataBySubjectCount(String className, int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=? ORDER BY " + COL_ID + " ASC",
                new String[]{className, String.valueOf(subCount)});
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public void deleteDataByRoll(int roll, String className, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + "=? AND " + COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=?",
                new String[]{String.valueOf(roll), className, String.valueOf(subCount)});
    }

    // ১. ইউনিক ফাইল লিস্ট পাওয়ার জন্য (শ্রেণি এবং সাবজেক্ট সংখ্যার তালিকা)
    public Cursor getUniqueSavedFiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        // DISTINCT ব্যবহার করা হয়েছে যাতে একই ক্লাসের নাম বারবার না আসে
        return db.rawQuery("SELECT DISTINCT " + COL_CLASS_NAME + ", " + COL_SUB_COUNT +
                " FROM " + TABLE_NAME + " ORDER BY " + COL_CLASS_NAME + " ASC", null);
    }


    // ২. নির্দিষ্ট শ্রেণি এবং সাবজেক্টের সকল ডাটা একবারে ডিলিট করার জন্য
    public void deleteResultsByClassAndSub(String className, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=?",
                new String[]{className, String.valueOf(subCount)});
        db.close();
    }

    // ৩. নির্দিষ্ট শ্রেণি ও সাবজেক্টে ডাটা আছে কি না তা চেক করার স্মার্ট মেথড
    public boolean isDataExists(String className, int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_NAME +
                " WHERE " + COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{className, String.valueOf(subCount)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


}

