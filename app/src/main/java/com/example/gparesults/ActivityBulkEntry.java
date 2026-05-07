package com.example.gparesults;

import android.content.Intent;
import android.database.Cursor;
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
    private Spinner spClass;
    private Spinner spSubjectSelector;
    private boolean isDataChanged = false;

    private ImageView ivNamePlus, ivNameMinus;
    private RecyclerView rvBulkEntry;
    private Button btnSubmitMarks;

    private int subjectCount;
    private List<ResultModel> studentList = new ArrayList<>();
    private DBHelper dbHelper;
    private BulkEntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bulk_entry);

        // সিস্টেম বার ইনসেটস লজিক যুক্ত করা হলো
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.balk_entry), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        setupClassSpinner();
        setupSubjectCountSpinner();
        setupNameToggleLogic();

        setupSubjectSelector();

        btnSubmitMarks.setOnClickListener(v -> saveBulkData());

        loadStudentsFromDB();
    }



    private void initViews() {
        spClass = findViewById(R.id.classSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        ivNamePlus = findViewById(R.id.ivNamePlus);
        ivNameMinus = findViewById(R.id.ivNameMinus);
        spSubjectSelector = findViewById(R.id.spSubjectSelector);
        rvBulkEntry = findViewById(R.id.rvBulkEntry);
        btnSubmitMarks = findViewById(R.id.btnSubmitMarks);

        dbHelper = new DBHelper(this);
        rvBulkEntry.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BulkEntryAdapter(studentList, () -> isDataChanged = true);
        rvBulkEntry.setAdapter(adapter);
    }

    private void setupClassSpinner() {
        List<String> classList = new ArrayList<>();
        // এখানে আপনার SetupActivity-র মতো হুবহু একই লিস্ট থাকতে হবে
        String[] classesArray = {
                "নার্সারি", "প্রথম শ্রেণি", "দ্বিতীয় শ্রেণি", "তৃতীয় শ্রেণি", "চতুর্থ শ্রেণি",
                "পঞ্চম শ্রেণি", "ষষ্ঠ শ্রেণি", "সপ্তম শ্রেণি", "অষ্টম শ্রেণি", "নবম শ্রেণি",
                "দশম শ্রেণি", "একাদশ শ্রেণি", "দ্বাদশ শ্রেণি", "ত্রয়োদশ শ্রেণি", "চতুর্দশ শ্রেণি",
                "পঞ্চদশ শ্রেণি", "ষোড়শ শ্রেণি", "সপ্তদশ শ্রেণি", "অষ্টাদশ শ্রেণি", "উনবিংশ শ্রেণি", "বিংশ শ্রেণি"
        };

        for (String c : classesArray) {
            classList.add(c);
        }

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

        // ইনটেন্ট থেকে আসা ক্লাস অনুযায়ী অটোমেটিক সিলেকশন সেট করা
        String incomingClass = getIntent().getStringExtra("CLASS_NAME");
        if (incomingClass != null) {
            int pos = classList.indexOf(incomingClass);
            if (pos >= 0) {
                spClass.setSelection(pos);
            }
        }
    }


    private void setupSubjectCountSpinner() {
        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i)
                    .replace('0','০').replace('1','১').replace('2','২')
                    .replace('3','৩').replace('4','৪').replace('5','৫')
                    .replace('6','৬').replace('7','৭').replace('8','৮')
                    .replace('9','৯');
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
        subjectSpinner.setAdapter(subAdapter);

        int incomingSubCount = getIntent().getIntExtra("SUB_COUNT", 4);
        subjectSpinner.setSelection(incomingSubCount - 4);

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectCount = position + 4;
                loadStudentsFromDB();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ... (বাকি মেথডগুলো অপরিবর্তিত থাকবে)
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

    private void loadStudentsFromDB() {
        if (dbHelper == null || spClass == null) return;

        // ইনটেন্ট নয়, সরাসরি স্পিনার থেকে বর্তমান মানগুলো নেওয়া হচ্ছে
        String className = spClass.getSelectedItem() != null ? spClass.getSelectedItem().toString() : getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";

        // subjectCount অলরেডি গ্লোবাল ভেরিয়েবলে আপডেট হচ্ছে
        studentList.clear();
        Cursor cursor = dbHelper.getDataBySubjectCount(className, subjectCount);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ResultModel model = new ResultModel();
                model.setRoll(cursor.getInt(0));
                model.setName(cursor.getString(1));
                model.setMarks(cursor.getString(3));
                studentList.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        isDataChanged = false;
    }


    private void showSubjectPopup() {
        Toast.makeText(this, "বিষয় নির্বাচন করুন", Toast.LENGTH_SHORT).show();
    }

    private void saveBulkData() {
        if (!isDataChanged) {
            Toast.makeText(this, "আপনি কোনো নতুন ডাটা পরিবর্তন করেননি!", Toast.LENGTH_SHORT).show();
            return;
        }

        String className = spClass.getSelectedItem().toString(); // স্পিনার থেকে বর্তমান ক্লাস নেওয়া

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

        Toast.makeText(this, "সফলভাবে জমা হয়েছে!", Toast.LENGTH_SHORT).show();
        isDataChanged = false;

        Intent intent = new Intent(ActivityBulkEntry.this, MadrasaActivity.class);
        intent.putExtra("CLASS_NAME", className);
        intent.putExtra("SUB_COUNT", subjectCount);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setupSubjectSelector() {
        subjectCount = getIntent().getIntExtra("SUB_COUNT", 4);

        // SubjectHelper থেকে আপনার সেই ৯টি নাম নিয়ে আসা
        List<String> allSubjects = SubjectHelper.getSubjectList(subjectCount);

        // শুধু অসম্পূর্ণ বিষয়গুলো ফিল্টার করা
        List<String> incompleteSubjects = getOnlyIncompleteSubjects();

        // যদি কোনো কারণে ইনকমপ্লিট লিস্ট খালি থাকে, তবে ইউজারকে সব দেখাবে (যাতে অ্যাপ খালি না থাকে)
        List<String> finalDisplayList = new ArrayList<>();
        finalDisplayList.add("বিষয়"); // শুরুতে "বিষয়" থাকবে
        if (!incompleteSubjects.isEmpty()) {
            finalDisplayList.addAll(incompleteSubjects);
        } else {
            finalDisplayList.addAll(allSubjects); // সব সাবজেক্ট দেখাবে যদি ইনকমপ্লিট কিছু না থাকে
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, finalDisplayList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);

                // রেংকিং অ্যাক্টিভিটির ডাইনামিক ফন্ট লজিক
                String text = tv.getText().toString();
                boolean isEnglish = text.matches(".*[a-zA-Z].*");
                try {
                    android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat.getFont(getContext(),
                            isEnglish ? R.font.timesnewromanregular : R.font.sutonnymjregular);
                    tv.setTypeface(tf);
                } catch (Exception e) {
                    com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, text);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.WHITE);

                // ড্রপডাউনের জন্যও একই লজিক
                String text = tv.getText().toString();
                boolean isEnglish = text.matches(".*[a-zA-Z].*");
                try {
                    android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat.getFont(getContext(),
                            isEnglish ? R.font.timesnewromanregular : R.font.sutonnymjregular);
                    tv.setTypeface(tf);
                } catch (Exception e) {
                    com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, text);
                }
                return v;
            }
        };


        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubjectSelector.setAdapter(subAdapter);


        spSubjectSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedSubject = finalDisplayList.get(position);
                    int originalIndex = allSubjects.indexOf(selectedSubject);

                    // স্ক্রল করার আগে নিশ্চিত হওয়া যে লিস্টে ডাটা আছে
                    if (!studentList.isEmpty()) {
                        int incompletePosition = findFirstIncompleteStudent(originalIndex);
                        if (incompletePosition != -1) {
                            // UI রেন্ডার হওয়ার সময় দিতে ৩০০ মিলিসেকেন্ড ডিলে
                            rvBulkEntry.postDelayed(() -> {
                                rvBulkEntry.scrollToPosition(incompletePosition);
                                adapter.setYellowSignal(incompletePosition);
                            }, 300);
                        }
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

    }


    // এই মেথডটি চেক করবে কোন কোন বিষয়ের কাজ এখনো বাকি আছে
    private List<String> getOnlyIncompleteSubjects() {
        List<String> allSubjects = SubjectHelper.getSubjectList(subjectCount);
        List<String> remainingSubjects = new ArrayList<>();

        for (int subIndex = 0; subIndex < allSubjects.size(); subIndex++) {
            boolean isSubjectDone = true;
            for (ResultModel student : studentList) {
                String marks = student.getMarks();
                String[] mArray = marks != null ? marks.split(",") : new String[0];
                // যদি একটি রোলও খালি পাওয়া যায়, তার মানে সাবজেক্টটি অসম্পূর্ণ
                if (subIndex >= mArray.length || mArray[subIndex].trim().equals("×") || mArray[subIndex].trim().isEmpty()) {
                    isSubjectDone = false;
                    break;
                }
            }
            if (!isSubjectDone) {
                remainingSubjects.add(allSubjects.get(subIndex));
            }
        }
        return remainingSubjects;
    }




    private int findFirstIncompleteStudent(int subjectIndex) {
        for (int i = 0; i < studentList.size(); i++) {
            String marks = studentList.get(i).getMarks();
            String[] marksArray = marks != null ? marks.split(",") : new String[0];

            // বর্তমান সাবজেক্ট ইনডেক্সে নম্বর ফাঁকা বা '×' থাকলে সেই পজিশন রিটার্ন করবে
            if (subjectIndex >= marksArray.length || marksArray[subjectIndex].trim().equals("×") || marksArray[subjectIndex].trim().isEmpty()) {
                return i;
            }
        }
        return -1; // সব রোল পূরণ করা থাকলে
    }





}
