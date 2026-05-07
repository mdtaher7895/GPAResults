package com.example.gparesults;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ResultDB";
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
    private static final String COL_CLASS_NAME = "class_name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER, " +
                COL_NAME + " TEXT, " +
                COL_CLASS_NAME + " TEXT, " +
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

    // পেমেন্ট সিকিউরিটির জন্য পরীক্ষার্থী গণনার মেথড
    public int getExamineeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean insertOrUpdateResult(int roll, String name, String className, String allMarks, int total, double gpa, String grade, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, roll);
        values.put(COL_NAME, name);
        values.put(COL_CLASS_NAME, className);
        values.put(COL_MARKS, allMarks);
        values.put(COL_TOTAL, total);
        values.put(COL_GPA, gpa);
        values.put(COL_GRADE, grade);
        values.put(COL_SUB_COUNT, subCount);

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }


    // নির্দিষ্ট শিক্ষার্থী খুঁজে পাওয়ার মেথড
    public Cursor getStudentData(int roll, String className, int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_NAME + ", " + COL_MARKS +
                        " FROM " + TABLE_NAME +
                        " WHERE " + COL_ID + "=? AND " + COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=?",
                new String[]{String.valueOf(roll), className, String.valueOf(subCount)});
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

    public Cursor getUniqueSavedFiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT " + COL_CLASS_NAME + ", " + COL_SUB_COUNT +
                " FROM " + TABLE_NAME + " ORDER BY " + COL_CLASS_NAME + " ASC", null);
    }

    public void deleteResultsByClassAndSub(String className, int subCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=?",
                new String[]{className, String.valueOf(subCount)});
        db.close();
    }

    public boolean isDataExists(String className, int subCount) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_NAME +
                " WHERE " + COL_CLASS_NAME + "=? AND " + COL_SUB_COUNT + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{className, String.valueOf(subCount)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // নির্দিষ্ট সাবজেক্টের ডাটা অসম্পূর্ণ কি না তা চেক করার মেথড
    public boolean isSubjectIncomplete(String className, int subCount, int subjectIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = getDataBySubjectCount(className, subCount);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String marks = cursor.getString(cursor.getColumnIndexOrThrow("marks"));
                String[] mArray = marks != null ? marks.split(",") : new String[0];
                if (subjectIndex >= mArray.length || mArray[subjectIndex].trim().equals("×") || mArray[subjectIndex].trim().isEmpty()) {
                    cursor.close();
                    return true; // অসম্পূর্ণ ঘর পাওয়া গেছে
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return false; // সব ঘর পূর্ণ
    }




}
