package com.example.gparesults;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SavedListActivity extends AppCompatActivity {

    private TableLayout listTable;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_list);

        // আপনার দেওয়া সিস্টেমবার কোড
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.savedListMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ইনিশিয়ালাইজেশন
        listTable = findViewById(R.id.listTable);
        dbHelper = new DBHelper(this);

        // পিছনে যাওয়ার বাটন লজিক
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ডাটাবেজ থেকে সেভ করা ফাইলগুলোর লিস্ট লোড করা
        loadSavedFiles();
    }

    private void loadSavedFiles() {
        listTable.removeAllViews();

        // DBHelper থেকে ইউনিক ফাইলগুলো নিয়ে আসা
        Cursor cursor = dbHelper.getUniqueSavedFiles();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String className = cursor.getString(0); // শ্রেণি
                int subCount = cursor.getInt(1);       // বিষয় সংখ্যা

                addListRow(className, subCount);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            showEmptyMessage();
        }
    }

    private void addListRow(String className, int subCount) {
        TableRow row = new TableRow(this);
        // মার্জিন দেওয়ার জন্য LayoutParams ব্যবহার
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 15);
        row.setLayoutParams(rowParams);
        row.setPadding(20, 30, 20, 30);
        row.setBackgroundResource(R.drawable.table_border_item);
        row.setGravity(Gravity.CENTER_VERTICAL);

        // ১. বাম পাশে ডিলিট আইকন
        ImageView imgDelete = new ImageView(this);
        imgDelete.setImageResource(android.R.drawable.ic_menu_delete);
        imgDelete.setColorFilter(Color.RED);
        TableRow.LayoutParams imgParams = new TableRow.LayoutParams(80, 80);
        imgDelete.setLayoutParams(imgParams);

        // ডিলিট আইকনে ক্লিক করলে সতর্কবার্তা (AlertDialog)
        imgDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("সতর্কবার্তা!")
                    .setMessage("আপনি কি নিশ্চিত যে " + className + " শ্রেণির (" + convertToBengali(subCount) + "টি বিষয়) এই ফাইলটি চিরতরে ডিলিট করতে চান?")
                    .setPositiveButton("হ্যাঁ", (dialog, which) -> {
                        dbHelper.deleteResultsByClassAndSub(className, subCount);
                        loadSavedFiles(); // লিস্ট রিফ্রেশ করা
                        Toast.makeText(this, "ফাইলটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("না", null)
                    .show();
        });

        // ২. টাইটেল (শ্রেণি ও বিষয় সংখ্যা)
        TextView tvTitle = new TextView(this);
        String titleText = "শ্রেণি: " + className + " (" + convertToBengali(subCount) + "টি বিষয়)";
        tvTitle.setText(titleText);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTextSize(18);
        tvTitle.setPadding(35, 0, 10, 0);
        tvTitle.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

        // ৩. পুরো লাইনে ক্লিক করলে মাদ্রাসা অ্যাক্টিভিটি ওপেন হবে
        row.setOnClickListener(v -> {
            Intent intent = new Intent(SavedListActivity.this, MadrasaActivity.class);
            intent.putExtra("CLASS_NAME", className);
            intent.putExtra("SUB_COUNT", subCount);
            startActivity(intent);
        });

        row.addView(imgDelete);
        row.addView(tvTitle);
        listTable.addView(row);
    }

    private void showEmptyMessage() {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("কোনো সেভ করা ফাইল পাওয়া যায়নি।");
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(0, 100, 0, 0);
        tvEmpty.setTextSize(16);
        listTable.addView(tvEmpty);
    }

    private String convertToBengali(int input) {
        String s = String.valueOf(input);
        return s.replace('0','০').replace('1','১').replace('2','২').replace('3','৩')
                .replace('4','৪').replace('5','৫').replace('6','৬').replace('7','৭')
                .replace('8','৮').replace('9','৯');
    }
}
