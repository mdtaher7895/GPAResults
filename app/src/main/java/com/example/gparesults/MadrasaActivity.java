package com.example.gparesults;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
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

        findViewById(R.id.btnBackToInput).setOnClickListener(v -> finish());

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            Intent intent = new Intent(MadrasaActivity.this, RankingActivity.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            startActivity(intent);
        });

        loadResultTable();
    }

    private void loadResultTable() {
        mainTable.removeAllViews();
        Cursor cursor = dbHelper.getDataBySubjectCount(subjectCount);

        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));
        addCell(headerRow, "⋮", true, 50);
        addCell(headerRow, "রোল", true, 80);
        addCell(headerRow, "পরীক্ষার্থীর নাম", true, 250);

        // হেডার সাবজেক্ট সংখ্যা বাংলায়
        for (int i = 1; i <= subjectCount; i++) {
            addCell(headerRow, "বি " + convertToBengali(i), true, 80);
        }

        addCell(headerRow, "সর্বমোট", true, 100);
        addCell(headerRow, "জিপিএ", true, 100);
        addCell(headerRow, "গ্রেড", true, 80);
        addCell(headerRow, "অবস্থান", true, 80);
        mainTable.addView(headerRow);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TableRow row = new TableRow(this);
                final int currentRoll = cursor.getInt(0);
                final String currentName = cursor.getString(1);
                final String marksRaw = cursor.getString(2);
                final int totalMarks = cursor.getInt(3);
                final double gpa = cursor.getDouble(4);
                final String grade = cursor.getString(5);

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

                // থ্রি-ডট মেনু
                TextView tvMenu = createStyledTextView("⋮", false, 50, Color.BLACK);
                tvMenu.setOnClickListener(v -> {
                    Intent intent = new Intent(MadrasaActivity.this, InputDataActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("ROLL", currentRoll);
                    intent.putExtra("NAME", currentName);
                    intent.putExtra("MARKS", marksRaw);
                    intent.putExtra("SUB_COUNT", subjectCount);
                    startActivity(intent);
                    finish();
                });
                row.addView(tvMenu);

                // রোল (বাংলায়)
                addCell(row, convertToBengali(currentRoll), false, 80);
                // নাম
                addCell(row, currentName, false, 250);

                // বিষয়ভিত্তিক নম্বর (বাংলায়)
                for (String m : marksArray) {
                    String displayMark = m.trim().equals("×") ? "×" : convertToBengali(m.trim());
                    addCell(row, displayMark, false, 80);
                }

                if (isFullyAbsent) {
                    addCell(row, "×", false, 100);
                    addCell(row, "×", false, 100);
                    addCell(row, "×", false, 80);
                    addCell(row, "×", false, 80);
                } else if (isFails) {
                    addCellWithColor(row, "ফেল", Color.RED, 100);
                    addCellWithColor(row, "Fail", Color.RED, 100); // ইংরেজিতে
                    addCellWithColor(row, "F", Color.RED, 80);    // ইংরেজিতে
                    addCell(row, "০", false, 80);                  // বাংলায়
                } else {
                    // সর্বমোট (বাংলায়)
                    addCell(row, convertToBengali(totalMarks), false, 100);
                    // জিপিএ (ইংরেজিতে)
                    addCell(row, String.format("%.2f", gpa), false, 100);
                    // গ্রেড (ইংরেজিতে)
                    addCell(row, grade, false, 80);
                    // অবস্থান (বাংলায়)
                    addCell(row, "-", false, 80);
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
