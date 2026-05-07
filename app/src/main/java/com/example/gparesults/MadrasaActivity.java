package com.example.gparesults;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MadrasaActivity extends AppCompatActivity {

    private TableLayout mainTable;
    private DBHelper dbHelper;
    private int subjectCount;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_madrasa);

        // সিস্টেম বার প্যাডিং
        View mainView = findViewById(R.id.madrasaMainLayout);
        if (mainView != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new DBHelper(this);
        mainTable = findViewById(R.id.mainTable);

        subjectCount = getIntent().getIntExtra("SUB_COUNT", 4);
        className = getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";

        // অল ডিলিট বাটন (সংশোধিত নিরাপদ লজিক)
        findViewById(R.id.allDelete).setOnClickListener(v1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MadrasaActivity.this);
            builder.setTitle("সতর্কতা!");
            builder.setMessage(className + " শ্রেণির সব ডাটা কি মুছে ফেলতে চান?");
            builder.setCancelable(true);

            builder.setPositiveButton("ডিলিট করুন", (dialogInterface, i) -> {
                // পুরো ডাটাবেজ নয়, শুধু এই ক্লাসের ডাটা ডিলিট হবে
                dbHelper.deleteResultsByClassAndSub(className, subjectCount);
                loadResultTable();
                Toast.makeText(this, "সব ডাটা মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
            });

            builder.setNeutralButton("সেভ করুন", (dialogInterface, i) -> {
                Intent intent = new Intent(MadrasaActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
            builder.show();
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            Intent intent = new Intent(MadrasaActivity.this, RankingActivity.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            intent.putExtra("CLASS_NAME", className);
            startActivity(intent);
        });

        loadResultTable();
    }

    private void loadResultTable() {
        mainTable.removeAllViews();
        Cursor cursor = dbHelper.getDataBySubjectCount(className, subjectCount);

        // টেবিল হেডার
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));
        addCell(headerRow, "⋮", true, 50);
        addCell(headerRow, "রোল", true, 50);
        addCell(headerRow, "পরীক্ষার্থীর নাম", true, 150);

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
                final int currentRoll = cursor.getInt(0);
                final String currentName = cursor.getString(1);
                final String marksRaw = cursor.getString(3);

                // CalculatorUtils থেকে ডাটা নেওয়া
                CalculatorUtils.ResultSummary summary = CalculatorUtils.calculateResult(marksRaw, subjectCount);

                // আপনার আগের লজিক ফিরিয়ে আনা হয়েছে
                boolean isFullyAbsent = marksRaw == null || marksRaw.isEmpty() || !marksRaw.contains(",");
                // সবগুলোতে '×' আছে কি না চেক
                if(marksRaw != null && !marksRaw.isEmpty()) {
                    isFullyAbsent = !marksRaw.matches(".*\\d.*");
                }

                addMenuToRow(row, currentRoll, currentName, marksRaw);
                addCell(row, convertToBengali(currentRoll), false, 50);
                addCell(row, currentName, false, 150);

                String[] marksArray = marksRaw != null ? marksRaw.split(",") : new String[0];
                for (int i = 0; i < subjectCount; i++) {
                    String m = (i < marksArray.length) ? marksArray[i].trim() : "×";
                    addCell(row, m.equals("×") ? "×" : convertToBengali(m), false, 50);
                }

                // আপনার চাহিদানুযায়ী আউটপুট ফরম্যাট (বাংলা টেক্সট ও কালারসহ)
                if (isFullyAbsent) {
                    addCell(row, "×", false, 70);
                    addCell(row, "×", false, 70);
                    addCell(row, "×", false, 50);
                } else if (summary.grade.equals("F")) {
                    addCellWithColor(row, "ফেল", Color.RED, 70);
                    addCellWithColor(row, "Fail", Color.RED, 70);
                    addCellWithColor(row, "F", Color.RED, 50);
                } else {
                    addCell(row, convertToBengali(summary.total), false, 70);
                    addCell(row, String.format("%.2f", summary.gpa), false, 70);
                    addCell(row, summary.grade, false, 50);
                }

                addCell(row, "-", false, 70);
                mainTable.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    // মেনু, বাংলা কনভার্ট এবং সেল তৈরির মেথডগুলো অপরিবর্তিত... (আগের মতোই থাকবে)
    private void addMenuToRow(TableRow row, int roll, String name, String marks) {
        TextView tvMenu = createStyledTextView("⋮", false, 50, Color.BLACK);
        tvMenu.setOnClickListener(v -> {
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.edit_delete_dialog);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            }
            dialog.findViewById(R.id.btnEdit).setOnClickListener(v1 -> {
                Intent intent = new Intent(this, InputDataActivity.class);
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("CLASS_NAME", className);
                intent.putExtra("ROLL", roll);
                intent.putExtra("NAME", name);
                intent.putExtra("MARKS", marks);
                intent.putExtra("SUB_COUNT", subjectCount);
                startActivity(intent);
                dialog.dismiss();
                finish();
            });
            dialog.findViewById(R.id.btnDelete).setOnClickListener(v1 -> {
                dbHelper.deleteDataByRoll(roll, className, subjectCount);
                loadResultTable();
                dialog.dismiss();
            });
            dialog.show();
        });
        row.addView(tvMenu);
    }

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
