package com.example.gparesults;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MadrasaActivity extends AppCompatActivity {

    private TableLayout mainTable;
    private DBHelper dbHelper;
    private int subjectCount;
    private String className; // এই লাইনটি যোগ করুন


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_madrasa);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.madrasaMainLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dbHelper = new DBHelper(this);
        mainTable = findViewById(R.id.mainTable);
        subjectCount = getIntent().getIntExtra("SUB_COUNT", 4);

        className = getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";


        findViewById(R.id.allDelete).setOnClickListener(v1 -> {
            // নিশ্চিতকরণ বক্স (AlertDialog) তৈরি
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MadrasaActivity.this);
            builder.setTitle("সতর্কতা!");
            builder.setMessage("আপনি কি এই ফাইলের সম্পূর্ণ পরীক্ষার্থীদের ডাটা মুছে ফেলতে চান?");
            builder.setCancelable(true); // বক্সের বাইরে ক্লিক করলে বন্ধ হবে না

            // ১. "ডিলিট করুন" বাটন (সব ডাটা মুছে ফেলার লজিক)
            builder.setPositiveButton("ডিলিট করুন", (dialogInterface, i) -> {
                dbHelper.clearAllData(); // ডাটাবেজের সব ডাটা ক্লিন করার মেথড
                loadResultTable();       // টেবিল রিফ্রেশ করা
                android.widget.Toast.makeText(MadrasaActivity.this, "সব ডাটা মুছে ফেলা হয়েছে", android.widget.Toast.LENGTH_SHORT).show();
            });

            // ২. "সেভ করুন" বাটন (ফাইল সেভ করে মূল মেনুতে ফেরার লজিক)
            builder.setNeutralButton("সেভ করুন", (dialogInterface, i) -> {
                android.widget.Toast.makeText(MadrasaActivity.this, "ফাইলটি সফলভাবে সেভ করা হয়েছে!", android.widget.Toast.LENGTH_LONG).show();

                // মেইন মেনুতে (MainActivity) ফিরে যাওয়ার ইনটেন্ট
                Intent intent = new Intent(MadrasaActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // আগের সব অ্যাক্টিভিটি ক্লিন করা
                startActivity(intent);
                finish(); // বর্তমান পেজ বন্ধ করা
            });


            builder.show();
        });



        findViewById(R.id.btnNext).setOnClickListener(v -> {
            Intent intent = new Intent(MadrasaActivity.this, RankingActivity.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            intent.putExtra("CLASS_NAME", className); // এই লাইনটি অবশ্যই যোগ করতে হবে
            startActivity(intent);
        });


        loadResultTable();
    }

    private void loadResultTable() {
        mainTable.removeAllViews();
        Cursor cursor = dbHelper.getDataBySubjectCount(className, subjectCount);


        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));
        addCell(headerRow, "⋮", true, 50);
        addCell(headerRow, "রোল", true, 50);
        addCell(headerRow, "পরীক্ষার্থীর নাম", true, 150);

        // হেডার সাবজেক্ট সংখ্যা বাংলায়
        for (int i = 1; i <= subjectCount; i++) {
            addCell(headerRow, "বি" + convertToBengali(i), true, 50);
        }

        addCell(headerRow, "সর্বমোট", true, 70);
        addCell(headerRow, "জিপিএ", true, 70);
        addCell(headerRow, "গ্রেড", true, 50);
        addCell(headerRow, "অবস্থান", true, 70);
        mainTable.addView(headerRow);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                TableRow row = new TableRow(this);

                // এখানে ইনডেক্সগুলো খেয়াল করুন (শ্রেণিকে আমরা ডাটাবেজ থেকে নিচ্ছি কিন্তু টেবিলে দেখাচ্ছি না)
                final int currentRoll = cursor.getInt(0);       // রোল (Index 0)
                final String currentName = cursor.getString(1);  // নাম (Index 1)
                // Index 2 এ আছে শ্রেণির নাম (className), যা আমরা স্কিপ করছি কারণ এটি টেবিলে দরকার নেই।
                final String marksRaw = cursor.getString(3);    // নম্বর (Index 3) - আগে এটি ২ ছিল
                final int totalMarks = cursor.getInt(4);        // মোট (Index 4) - আগে এটি ৩ ছিল
                final double gpa = cursor.getDouble(5);         // জিপিএ (Index 5) - আগে এটি ৪ ছিল
                final String grade = cursor.getString(6);       // গ্রেড (Index 6) - আগে এটি ৫ ছিল

                // অনুপস্থিতি ও ফেল লজিক
                boolean isFullyAbsent = true;
                String[] marksArray = marksRaw != null ? marksRaw.split(",") : new String[0];
                for (String m : marksArray) {
                    if (!m.trim().equals("×")) {
                        isFullyAbsent = false;
                        break;
                    }
                }
                boolean hasCross = marksRaw != null && marksRaw.contains("×");
                boolean isFails = hasCross || (grade != null && grade.equals("F"));

                // থ্রি-ডট মেনু এবং এডিট/ডিলিট লজিক
                TextView tvMenu = createStyledTextView("⋮", false, 50, Color.BLACK);
                tvMenu.setOnClickListener(v -> {
                    android.app.Dialog dialog = new android.app.Dialog(MadrasaActivity.this);
                    dialog.setContentView(R.layout.edit_delete_dialog);

                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                    }

                    android.widget.Button btnEdit = dialog.findViewById(R.id.btnEdit);
                    android.widget.Button btnDelete = dialog.findViewById(R.id.btnDelete);

                    btnEdit.setOnClickListener(v1 -> {
                        Intent intent = new Intent(MadrasaActivity.this, InputDataActivity.class);
                        intent.putExtra("EDIT_MODE", true);
                        intent.putExtra("CLASS_NAME", className);
                        intent.putExtra("ROLL", currentRoll);
                        intent.putExtra("NAME", currentName);
                        intent.putExtra("MARKS", marksRaw);
                        intent.putExtra("SUB_COUNT", subjectCount);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    });

                    btnDelete.setOnClickListener(v1 -> {
                        dbHelper.deleteDataByRoll(currentRoll, className, subjectCount);
                        loadResultTable();
                        dialog.dismiss();
                    });
                    dialog.show();
                });

                row.addView(tvMenu);

                // টেবিলের কলামে ডাটা সাজানো
                addCell(row, convertToBengali(currentRoll), false, 50); // ১. রোল
                addCell(row, currentName, false, 150);                  // ২. নাম

                // ৩. বিষয়ভিত্তিক নম্বরগুলো সিরিয়াল অনুযায়ী বসবে
                for (String m : marksArray) {
                    String displayMark = m.trim().equals("×") ? "×" : convertToBengali(m.trim());
                    addCell(row, displayMark, false, 50);
                }

                // ৪. সর্বমোট, জিপিএ এবং গ্রেড (সঠিক কলামে)
                if (isFullyAbsent) {
                    addCell(row, "×", false, 70);
                    addCell(row, "×", false, 70);
                    addCell(row, "×", false, 50);
                    addCell(row, "×", false, 70);
                } else if (isFails) {
                    addCellWithColor(row, "ফেল", Color.RED, 70);
                    addCellWithColor(row, "Fail", Color.RED, 70);
                    addCellWithColor(row, "F", Color.RED, 50);
                    addCell(row, "০", false, 70);
                } else {
                    addCell(row, convertToBengali(totalMarks), false, 70);
                    addCell(row, String.format("%.2f", gpa), false, 70);
                    addCell(row, grade, false, 50);
                    addCell(row, "-", false, 70);
                }

                mainTable.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }

    }


    // যেকোনো অবজেক্টকে বাংলায় রূপান্তর করার মেথড
    private String convertToBengali(Object input) {
        String str = String.valueOf(input);
        return str.replace('0', '০').replace('1', '১')
                .replace('2', '২').replace('3', '৩')
                .replace('4', '৪').replace('5', '৫')
                .replace('6', '৬').replace('7', '৭')
                .replace('8', '৮').replace('9', '৯');
    }

    private void addCell(TableRow row, String text, boolean isHeader, int widthDp) {
        row.addView(createStyledTextView(text, isHeader, widthDp, Color.BLACK));
    }

    private void addCellWithColor(TableRow row, String text, int color, int widthDp) {
        row.addView(createStyledTextView(text, false, widthDp, color));
    }

    private TextView createStyledTextView(String text, boolean isHeader, int widthDp, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(10, 15, 10, 15);
        tv.setGravity(Gravity.CENTER);
        float scale = getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * scale);
        tv.setLayoutParams(new TableRow.LayoutParams(widthPx, TableRow.LayoutParams.MATCH_PARENT));
        tv.setBackgroundResource(android.R.drawable.editbox_background);
        tv.setTextColor(textColor);
        if (isHeader) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }
}
