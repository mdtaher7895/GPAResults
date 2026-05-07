package com.example.gparesults;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private EditText etStartRoll, etEndRoll;
    private boolean isUserInteracted = false;
    private String className;
    private Button btnGenerateRolls;
    private DBHelper dbHelper;
    private int subjectCount;
    private Spinner spSubjectCount;
    private Spinner spClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);

        View root = findViewById(R.id.setupRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new DBHelper(this);
        etStartRoll = findViewById(R.id.etStartRoll);
        etEndRoll = findViewById(R.id.etEndRoll);
        btnGenerateRolls = findViewById(R.id.btnGenerateRolls);
        spSubjectCount = findViewById(R.id.subjectSpinner);
        spClass = findViewById(R.id.classSpinner);

        // ১. ইনটেন্ট থেকে আসা ডাটা রিসিভ করা (সবার আগে)
        int incomingCount = getIntent().getIntExtra("SUB_COUNT", 4);
        String incomingClass = getIntent().getStringExtra("CLASS_NAME");
        if (incomingClass == null) incomingClass = "নার্সারি";
        className = incomingClass;
        subjectCount = incomingCount;

        // ২. শ্রেণির স্পিনার সেটআপ
        List<String> classList = new ArrayList<>();
        String[] classesArray = {
                "নার্সারি", "প্রথম শ্রেণি", "দ্বিতীয় শ্রেণি", "তৃতীয় শ্রেণি", "চতুর্থ শ্রেণি",
                "পঞ্চম শ্রেণি", "ষষ্ঠ শ্রেণি", "সপ্তম শ্রেণি", "অষ্টম শ্রেণি", "নবম শ্রেণি",
                "দশম শ্রেণি", "একাদশ শ্রেণি", "দ্বাদশ শ্রেণি", "ত্রয়োদশ শ্রেণি", "চতুর্দশ শ্রেণি",
                "পঞ্চদশ শ্রেণি", "ষোড়শ শ্রেণি", "সপ্তদশ শ্রেণি", "অষ্টাদশ শ্রেণি", "উনবিংশ শ্রেণি", "বিংশ শ্রেণি"
        };
        for (String c : classesArray) { classList.add(c); }

        ArrayAdapter<String> classAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, classList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, tv.getText().toString());
                return v;
            }
        };

        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClass.setAdapter(classAdapter);

        // ক্লাস পজিশন সেট করা
        for (int i = 0; i < classesArray.length; i++) {
            if (classesArray[i].equals(className)) {
                spClass.setSelection(i);
                break;
            }
        }

        spClass.setOnTouchListener((v, event) -> { isUserInteracted = true; return false; });
        spClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                className = classesArray[position];
                if (isUserInteracted) { checkDataAndAutoJump(); }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ৩. সাবজেক্ট স্পিনার সেটআপ
        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i).replace('0','০').replace('1','১').replace('2','২').replace('3','৩').replace('4','৪').replace('5','৫').replace('6','৬').replace('7','৭').replace('8','৮').replace('9','৯');
            subList.add(bnNum + " টি বিষয়ের জন্য");
        }

        ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subList) {
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, tv.getText().toString());
                return v;
            }
        };
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubjectCount.setAdapter(subAdapter);

        // সাবজেক্ট পজিশন সেট করা
        spSubjectCount.setSelection(subjectCount - 4);

        spSubjectCount.setOnTouchListener((v, event) -> { isUserInteracted = true; return false; });
        spSubjectCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectCount = position + 4;
                if (isUserInteracted) { checkDataAndAutoJump(); }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnGenerateRolls.setOnClickListener(v -> generateBulkData());
        showWelcomePopup();
    }

    private void generateBulkData() {
        String startStr = etStartRoll.getText().toString().trim();
        String endStr = etEndRoll.getText().toString().trim();
        if (TextUtils.isEmpty(startStr) || TextUtils.isEmpty(endStr)) {
            Toast.makeText(this, "অনুগ্রহ করে শুরু এবং শেষ রোল দিন", Toast.LENGTH_SHORT).show();
            return;
        }
        int startRoll = Integer.parseInt(startStr);
        int endRoll = Integer.parseInt(endStr);
        if (startRoll > endRoll) {
            Toast.makeText(this, "শুরুর রোল শেষের চেয়ে বড় হতে পারে না", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            for (int i = startRoll; i <= endRoll; i++) {
                String emptyMarks = generateEmptyMarks(subjectCount);
                dbHelper.insertOrUpdateResult(i, "শিক্ষার্থীর নাম", className, emptyMarks, 0, 0.0, "F", subjectCount);
            }
            Toast.makeText(this, "সফলভাবে " + (endRoll - startRoll + 1) + " টি রোল তৈরি হয়েছে", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SetupActivity.this, ActivityBulkEntry.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            intent.putExtra("CLASS_NAME", className);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "ত্রুটি: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateEmptyMarks(int count) {
        return "";
    }




    private void checkDataAndAutoJump() {
        String currentClass = (className != null) ? className : "নার্সারি";
        if (dbHelper.isDataExists(currentClass, subjectCount)) {
            Intent intent = new Intent(SetupActivity.this, ActivityBulkEntry.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            intent.putExtra("CLASS_NAME", currentClass);
            startActivity(intent);
        }
        isUserInteracted = false;
    }

    private void showWelcomePopup() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // পপ-আপের মেসেজ সেট করা
        builder.setMessage("পূর্বের সেভ করা ফাইল দেখতে চাইলে ড্রপডাউন থেকে আপনার কাঙ্ক্ষিত শ্রেণি এবং বিষয় সংখ্যাটি পুনরায় ক্লিক করে নির্বাচন করুন।");

        // স্ক্রিনের বাইরে ক্লিক করলে যেন পপ-আপ চলে যায়
        builder.setCancelable(true);

        // একটি বাটন দেওয়া হলো যাতে ইউজার বুঝে ওকে করতে পারে (ঐচ্ছিক)
        builder.setPositiveButton("বুঝেছি", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


}
