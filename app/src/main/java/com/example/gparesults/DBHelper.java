package com.example.gparesults;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ResultDB";
    // ১. ভার্সন ২ থেকে বাড়িয়ে ৩ করা হয়েছে যাতে পুরনো ডাটাবেজ ডিলিট হয়ে নতুন করে তৈরি হয়
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "students";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_TOTAL = "total";
    private static final String COL_GPA = "gpa";
    private static final String COL_GRADE = "grade";
    private static final String COL_POSITION = "position";
    private static final String COL_MARKS = "marks";
    private static final String COL_SUB_COUNT = "sub_count";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER, " +
                COL_NAME + " TEXT, " +
                COL_MARKS + " TEXT, " +
                COL_TOTAL + " INTEGER, " +
                COL_GPA + " REAL, " +
                COL_GRADE + " TEXT, " +
                COL_SUB_COUNT + " INTEGER, " +
                COL_POSITION + " INTEGER DEFAULT 0, " +
                "PRIMARY KEY (" + COL_ID + ", " + COL_SUB_COUNT + "))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // পুরনো টেবিল মুছে নতুন করে তৈরি করবে
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertOrUpdateResult(int roll, String name, String allMarks, int total, double gpa, String grade, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, roll);
        values.put(COL_NAME, name);
        values.put(COL_MARKS, allMarks);
        values.put(COL_TOTAL, total);
        values.put(COL_GPA, gpa);
        values.put(COL_GRADE, grade);
        values.put(COL_SUB_COUNT, subCount);

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public Cursor getDataBySubjectCount(int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_SUB_COUNT + "=? ORDER BY " + COL_ID + " ASC", new String[]{String.valueOf(subCount)});
    }

    public Cursor getRankingData(int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_SUB_COUNT +
                "=? ORDER BY " + COL_TOTAL + " DESC, " + COL_GPA + " DESC", new String[]{String.valueOf(subCount)});
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
