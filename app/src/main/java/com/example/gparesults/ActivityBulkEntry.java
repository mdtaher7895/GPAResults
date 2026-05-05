package com.example.gparesults;
import android.content.Intent; // যুক্ত করা হয়েছে
import android.database.Cursor; // যুক্ত করা হয়েছে
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityBulkEntry extends AppCompatActivity {

    private Spinner subjectSpinner;
    private boolean isDataChanged = false; // শুরুতে এটি ফলস থাকবে

    private ImageView ivNamePlus, ivNameMinus, ivSubjectSelector;
    private RecyclerView rvBulkEntry;
    private Button btnSubmitMarks;

    private int subjectCount;
    private String selectedSubjectName = "বাংলা"; // ডিফল্ট বিষয়

    private List<ResultModel> studentList = new ArrayList<>();
    private DBHelper dbHelper;
    private BulkEntryAdapter adapter;


    private String[] predefinedSubjects = {
            "আরবী", "বাংলা", "ইংরেজি", "গণিত",
            "হাদিস ও আস: হুসনা", "কালিমা মাসায়িল",
            "আদ: সালাত আদ: মাসনূনা", "কুরআন ও তাজবীদ",
            "প: পরিবেশ ও সাধারণ জ্ঞান"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ১. EdgeToEdge এনাবল করা
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bulk_entry);

        // ২. আপনার দেওয়া আইডি 'balk_entry' অনুযায়ী সিস্টেম বার সেটআপ
        View layoutRoot = findViewById(R.id.balk_entry);
        if (layoutRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(layoutRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // ৩. ভিউ ইনিশিয়ালাইজেশন
        initViews();

        // ৪. লজিক সেটআপ
        setupSubjectCountSpinner();
        setupNameToggleLogic();

        btnSubmitMarks.setOnClickListener(v -> saveBulkData());
        ivSubjectSelector.setOnClickListener(v -> showSubjectPopup());

        // ৫. ডাটাবেজ থেকে ডাটা লোড করা
        loadStudentsFromDB();
    }

    private void initViews() {
        subjectSpinner = findViewById(R.id.subjectSpinner);
        ivNamePlus = findViewById(R.id.ivNamePlus);
        ivNameMinus = findViewById(R.id.ivNameMinus);
        ivSubjectSelector = findViewById(R.id.ivSubjectSelector);
        rvBulkEntry = findViewById(R.id.rvBulkEntry);
        btnSubmitMarks = findViewById(R.id.btnSubmitMarks);

        dbHelper = new DBHelper(this);
        rvBulkEntry.setLayoutManager(new LinearLayoutManager(this));

        // অ্যাডাপ্টার সেটআপ
        adapter = new BulkEntryAdapter(studentList);
        rvBulkEntry.setAdapter(adapter);
    }

    private void loadStudentsFromDB() {
        if (dbHelper == null) return;

        subjectCount = getIntent().getIntExtra("SUB_COUNT", 4);
        String className = getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";

        studentList.clear();

        // ডাটাবেজ থেকে ডাটা রিড করা
        Cursor cursor = dbHelper.getDataBySubjectCount(className, subjectCount);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ResultModel model = new ResultModel();
                // আপনার ছবির এরর মেটাতে এই মেথডগুলো ResultModel-এ থাকতে হবে
                model.setRoll(cursor.getInt(0));
                model.setName(cursor.getString(1));
                model.setMarks(cursor.getString(3));
                studentList.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }



    private void setupSubjectCountSpinner() {
        List<String> subList = new ArrayList<>();

        // ১. সংখ্যাগুলোকে বাংলায় রূপান্তর করে লিস্ট তৈরি
        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i)
                    .replace('0','০').replace('1','১').replace('2','২')
                    .replace('3','৩').replace('4','৪').replace('5','৫')
                    .replace('6','৬').replace('7','৭').replace('8','৮')
                    .replace('9','৯');
            subList.add(bnNum + " টি বিষয়ের জন্য");
        }

        // ২. সাবজেক্ট স্পিনারের জন্য সুন্দর ডিজাইন এবং কাস্টম ফন্ট যুক্ত অ্যাডাপ্টার
        ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subList) {
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK); // লেখা কালো হবে
                com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, tv.getText().toString());
                return v;
            }
        };

        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subAdapter);

        // ৩. ইনটেন্ট থেকে আসা সাবজেক্ট সংখ্যা অনুযায়ী সিলেকশন সেট করা
        int incomingSubCount = getIntent().getIntExtra("SUB_COUNT", 6);
        subjectSpinner.setSelection(incomingSubCount - 4);

        // ৪. আইটেম সিলেক্ট লজিক
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectCount = position + 4;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void setupNameToggleLogic() {
        ivNamePlus.setOnClickListener(v -> {
            ivNamePlus.setVisibility(View.GONE);
            ivNameMinus.setVisibility(View.VISIBLE);
            toggleNameColumn(true);
        });

        ivNameMinus.setOnClickListener(v -> {
            ivNameMinus.setVisibility(View.GONE);
            ivNamePlus.setVisibility(View.VISIBLE);
            toggleNameColumn(false);
        });
    }

    private void toggleNameColumn(boolean show) {
        if (adapter != null) {
            adapter.setNameVisible(show);
            adapter.notifyDataSetChanged();
        }
    }

    private void showSubjectPopup() {
        Toast.makeText(this, "বিষয় নির্বাচন করুন (লজিক প্রসেসিং...)", Toast.LENGTH_SHORT).show();
    }

    private void saveBulkData() {
        if (!isDataChanged) {
            Toast.makeText(this, "আপনি কোনো নতুন ডাটা পরিবর্তন করেননি!", Toast.LENGTH_SHORT).show();
            return;
        }

        String className = getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";

        for (ResultModel student : studentList) {
            dbHelper.insertOrUpdateResult(
                    student.getRoll(),
                    student.getName(),
                    className,
                    student.getMarks(),
                    0, 0.0, "F",
                    subjectCount
            );
        }

        Toast.makeText(this, "নতুন পরিবর্তনগুলো সফলভাবে জমা হয়েছে!", Toast.LENGTH_LONG).show();
        isDataChanged = false;

        // সরাসরি মাদ্রাসা অ্যাক্টিভিটিতে নিয়ে যাওয়া এবং প্রয়োজনীয় তথ্য পাঠানো
        Intent intent = new Intent(ActivityBulkEntry.this, MadrasaActivity.class);

        // এই নিচের দুটি লাইন খুবই গুরুত্বপূর্ণ, যাতে MadrasaActivity সঠিক ডাটা লোড করতে পারে
        intent.putExtra("CLASS_NAME", className);
        intent.putExtra("SUB_COUNT", subjectCount);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }




}
