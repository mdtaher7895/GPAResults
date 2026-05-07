package com.example.gparesults;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class InputDataActivity extends AppCompatActivity {

    private LinearLayout dynamicSubjectLayout;
    private int subjectCount;
    private List<String> subList = new ArrayList<>();

    Spinner spSubjectCount, spClass;
    private String className;
    private List<EditText> subjectInputList = new ArrayList<>();
    private EditText etRoll, etName;
    private DBHelper dbHelper;
    private boolean isFirstLoad = true;
    private TextView tvTotalMarksDisplay;

    private String[] predefinedSubjects = {
            "আরবী", "বাংলা", "ইংরেজি", "গণিত",
            "হাদিস ও\nআস: হুসনা", "কালিমা\nমাসায়িল",
            "আদ: সালাত\nআদ: মাসনূনা", "কুরআন ও\nতাজবীদ",
            "প: পরিবেশ ও\nসাধারণ জ্ঞান"
    };

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    /// /////////////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////////////
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_data);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainScrollView), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);
        etRoll = findViewById(R.id.etRoll);
        etName = findViewById(R.id.etName);

        // ১. মেইন অ্যাক্টিভিটি থেকে আসা ডাটা রিসিভ করা
        int subCount = getIntent().getIntExtra("SUB_COUNT", 4);
        String classNameFromIntent = getIntent().getStringExtra("CLASS_NAME") != null ? getIntent().getStringExtra("CLASS_NAME") : "নার্সারি";

        etRoll.setTextColor(Color.BLACK);
        etName.setTextColor(Color.BLACK);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            etRoll.setTextCursorDrawable(getResources().getDrawable(R.drawable.custom_cursor));
            etName.setTextCursorDrawable(getResources().getDrawable(R.drawable.custom_cursor));
        }

        dynamicSubjectLayout = findViewById(R.id.dynamicSubjectLayout);

        // ২. স্পিনার সেটআপ এবং সরাসরি লক করে দেওয়া (যাতে ইউজার পরিবর্তন করতে না পারে)
        spClass = findViewById(R.id.classSpinner);
        spSubjectCount = findViewById(R.id.subjectSpinner);

        // ৩. আগের পেজের ডাটা অনুযায়ী স্পিনার সেট করা
        // (বি.দ্র: আপনার অ্যাডাপ্টার সেট করার কোডটি যদি এর নিচে থাকে, তবে এই সিলেকশন কোডটুকু অ্যাডাপ্টারের নিচে সরাতে হবে)
        spSubjectCount.setSelection(subCount - 4);

        // ৪. সিকিউরিটির জন্য স্পিনার দুটি ডিজেবল করা
        spClass.setEnabled(false);
        spSubjectCount.setEnabled(false);

        /// //////////////////////////////////////////////////////////////
        /// //////////////////////////////////////////////////////////////
        /// //////////////////////////////////////////////////////////////


        String[] classes = {
                "নার্সারি", "প্রথম শ্রেণি", "দ্বিতীয় শ্রেণি", "তৃতীয় শ্রেণি", "চতুর্থ শ্রেণি",
                "পঞ্চম শ্রেণি", "ষষ্ঠ শ্রেণি", "সপ্তম শ্রেণি", "অষ্টম শ্রেণি", "নবম শ্রেণি",
                "দশম শ্রেণি", "একাদশ শ্রেণি", "দ্বাদশ শ্রেণি", "ত্রয়োদশ শ্রেণি", "চতুর্দশ শ্রেণি",
                "পঞ্চদশ শ্রেণি", "ষোড়শ শ্রেণি", "সপ্তদশ শ্রেণি", "অষ্টাদশ শ্রেণি", "উনবিংশ শ্রেণি", "বিংশ শ্রেণি"
        };

        // ১. শ্রেণির স্পিনারের জন্য সুন্দর ডিজাইন যুক্ত অ্যাডাপ্টার
        ArrayAdapter<String> classAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, classes) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK); // লেখা কালো হবে
                com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, tv.getText().toString());
                return v;
            }
        };
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClass.setAdapter(classAdapter);

        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i)
                    .replace('0','০').replace('1','১').replace('2','২')
                    .replace('3','৩').replace('4','৪').replace('5','৫')
                    .replace('6','৬').replace('7','৭').replace('8','৮')
                    .replace('9','৯');
            subList.add(bnNum + " টি বিষয়ের জন্য");
        }

        // ২. সাবজেক্ট স্পিনারের জন্য সুন্দর ডিজাইন যুক্ত অ্যাডাপ্টার
        ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK); // লেখা কালো হবে
                com.example.gparesults.FontUtils.applyCustomFont(getContext(), tv, tv.getText().toString());
                return v;
            }
        };
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubjectCount.setAdapter(subAdapter);


// ২. ডাটা রিসিভ করা (MainActivity থেকে আসা)
        className = getIntent().getStringExtra("CLASS_NAME");
        if (className == null) className = "নার্সারি";
        int incomingSubCount = getIntent().getIntExtra("SUB_COUNT", 6);

// ৩. স্পিনারে পজিশন অটো-সেট করা
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].equals(className)) {
                spClass.setSelection(i);
                break;
            }
        }
        int subPos = incomingSubCount - 4;
        if (subPos >= 0) spSubjectCount.setSelection(subPos);

        // ৪. লিসেনার সেটআপ (ইউজার পরিবর্তন করলে যা হবে)
        spClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                className = classes[position]; // গ্লোবাল ভেরিয়েবল আপডেট
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spSubjectCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectCount = position + 4;
                generateSubjectInputs(); // সাবজেক্ট ইনপুট বক্স তৈরি

                // এডিট মোড চেক
                if (getIntent().getBooleanExtra("EDIT_MODE", false) && isFirstLoad) {
                    checkEditMode();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });


        // আপনার বাকি সব লজিক (রোল, জাম্প, সেভ) এখানে আগের মতোই থাকবে...
        etRoll.setTag(false);
        etRoll.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (etRoll.getText().length() > 0) {
                    etRoll.setText("");
                    etRoll.setTag(true);
                } else {
                    etRoll.setTag(false);
                }
            }
            return false;
        });

        etRoll.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String val = s.toString();

                // ১. যদি বক্স খালি না থাকে, তবে ডাটাবেজ থেকে চেক করবে
                if (!val.isEmpty()) {
                    try {
                        int roll = Integer.parseInt(val);
                        // ডাটাবেজ থেকে এই রোল, ক্লাস এবং সাবজেক্ট কাউন্টের ডাটা খুঁজবে
                        android.database.Cursor cursor = dbHelper.getStudentData(roll, className, subjectCount);

                        if (cursor != null && cursor.moveToFirst()) {
                            // নাম খুঁজে পেলে সেট করবে
                            etName.setText(cursor.getString(0));

                            // নম্বরগুলো খুঁজে পেয়ে ইনপুট বক্সে বসিয়ে দিবে
                            String marksRaw = cursor.getString(1);
                            if (marksRaw != null && !marksRaw.isEmpty()) {
                                String[] marksArray = marksRaw.split(",");
                                for (int i = 0; i < marksArray.length && i < subjectInputList.size(); i++) {
                                    String m = marksArray[i].trim();
                                    // যদি নম্বর থাকে (× না হয়), তবেই বক্সে বসাবে
                                    if (!m.equals("×")) {
                                        subjectInputList.get(i).setText(m);
                                    } else {
                                        subjectInputList.get(i).setText(""); // খালি থাকলে খালি রাখবে
                                    }
                                }
                            }
                            cursor.close();
                        } else {
                            // যদি এই রোলের ডাটা না থাকে, তবে নাম এবং নম্বর বক্সগুলো পরিষ্কার রাখবে (ঐচ্ছিক)
                            etName.setText("");
                            for (android.widget.EditText et : subjectInputList) et.setText("");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                // ২. আপনার আগের ৩ ডিজিট লিমিট লজিক
                if (val.length() == 3 && !val.equals("100")) { s.delete(2, 3); return; }

                // ৩. আপনার আগের স্মার্ট জাম্প লজিক
                boolean isReady = (val.length() == 2 && !val.equals("10")) || (val.length() == 3 && val.equals("100"));
                if (isReady) {
                    if ((boolean) etRoll.getTag()) smartJumpFromAnywhere(-1);
                    else etName.requestFocus();
                }
            }
        });


        findViewById(R.id.btnSaveInput).setOnClickListener(v -> saveData());

        findViewById(R.id.btnViewResults).setOnClickListener(v -> {
            Intent intent = new Intent(InputDataActivity.this, MadrasaActivity.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            intent.putExtra("CLASS_NAME", className); // নিশ্চিত করুন এই লাইনটি আছে
            startActivity(intent);
        });



        findViewById(R.id.btnOneSub).setOnClickListener(v -> saveData());

        findViewById(R.id.btnOneSub).setOnClickListener(v -> {
            Intent intent = new Intent(InputDataActivity.this, SetupActivity.class);
            intent.putExtra("SUB_COUNT", subjectCount);
            startActivity(intent);
        });




    }

    private void checkEditMode() {
        // শুধুমাত্র প্রথমবার লোড হওয়ার সময় এই ব্লকে ঢুকবে
        if (getIntent().getBooleanExtra("EDIT_MODE", false) && isFirstLoad) {

            etRoll.setText(String.valueOf(getIntent().getIntExtra("ROLL", 0)));
            etName.setText(getIntent().getStringExtra("NAME"));

            int savedSubCount = getIntent().getIntExtra("SUB_COUNT", 4);
            int spinnerPosition = savedSubCount - 4;

            // যদি স্পিনার বর্তমানে সঠিক জায়গায় না থাকে, তবে সেটি সেট করো এবং এই মেথড থেকে বের হয়ে যাও
            // কারণ স্পিনার সেট হলে 'onItemSelected' আবার এই মেথডকে কল করবে
            if (spinnerPosition >= 0 && spinnerPosition != spSubjectCount.getSelectedItemPosition()) {
                spSubjectCount.setSelection(spinnerPosition);
                return;
            }

            // যখন স্পিনার সঠিক পজিশনে চলে আসবে, তখন নিচের নম্বরগুলো বসানোর কাজ শুরু হবে
            String marksRaw = getIntent().getStringExtra("MARKS");
            if (marksRaw != null) {
                String[] marksArray = marksRaw.split(",");
                dynamicSubjectLayout.post(() -> {
                    for (int i = 0; i < marksArray.length && i < subjectInputList.size(); i++) {
                        String m = marksArray[i].trim();
                        if (!m.equals("×") && !m.isEmpty()) {
                            subjectInputList.get(i).setText(m);
                        }
                    }
                });
            }

            // সব কাজ শেষ, তাই isFirstLoad কে false করে দিন যাতে ইউজার ম্যানুয়ালি স্পিনার পরিবর্তন করতে পারে
            isFirstLoad = false;
        }
    }


    private void smartJumpFromAnywhere(int currentIndex) {
        if (etName.getText().toString().isEmpty()) {
            etName.requestFocus();
            return;
        }
        int nextEmptyIndex = -1;
        for (int j = 0; j < subjectInputList.size(); j++) {
            if (subjectInputList.get(j).getText().toString().isEmpty()) {
                nextEmptyIndex = j;
                break;
            }
        }
        if (nextEmptyIndex != -1) subjectInputList.get(nextEmptyIndex).requestFocus();
        else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void generateSubjectInputs() {
        dynamicSubjectLayout.removeAllViews();
        subjectInputList.clear();

        for (int i = 0; i < subjectCount; i++) {
            final int index = i;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 15, 0, 15);

            TextView tvSubName = new TextView(this);
            String name = (i < predefinedSubjects.length) ? predefinedSubjects[i] : "Sub " + (i + 1);
            tvSubName.setText(name);
            tvSubName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
            tvSubName.setGravity(Gravity.END);
            tvSubName.setPadding(0, 0, 10, 0);
            tvSubName.setTextColor(Color.BLACK);
            tvSubName.setTextSize(18);

            // --- ডাবল টাচ এডিট লজিক শুরু (আপনার চ্যালেঞ্জ অনুযায়ী নিখুঁতভাবে যুক্ত করা) ---
            tvSubName.setOnTouchListener(new View.OnTouchListener() {
                private long lastClickTime = 0;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        long clickTime = System.currentTimeMillis();
                        if (clickTime - lastClickTime < 300) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                            builder.setTitle("বিষয় পরিবর্তন করুন");
                            final EditText input = new EditText(InputDataActivity.this);
                            input.setText(tvSubName.getText().toString());
                            builder.setView(input);
                            builder.setPositiveButton("সেভ", (dialog, which) -> tvSubName.setText(input.getText().toString()));
                            builder.setNegativeButton("বাতিল", null);
                            builder.show();
                        }
                        lastClickTime = clickTime;
                    }
                    return true;
                }
            });
            // --- ডাবল টাচ এডিট লজিক শেষ ---

            EditText et = new EditText(this);
            et.setHint("00");
            et.setTextColor(Color.BLACK);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(0, (int) (45 * getResources().getDisplayMetrics().density), 0.8f);
            et.setLayoutParams(etParams);
            et.setBackgroundResource(android.R.drawable.editbox_background);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            et.setTextSize(18);
            et.setTag(false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                et.setTextCursorDrawable(getResources().getDrawable(R.drawable.custom_cursor));
            }

            TextView tvPoint = new TextView(this);
            tvPoint.setText("Point: 0");
            tvPoint.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));
            tvPoint.setGravity(Gravity.START);
            tvPoint.setPadding(10, 0, 0, 0);
            tvPoint.setTextColor(Color.BLUE);
            tvPoint.setTextSize(18);

            et.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (et.getText().length() > 0) {
                        et.setText("");
                        et.setTag(true);
                    } else {
                        et.setTag(false);
                    }
                }
                return false;
            });

            et.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String val = s.toString();
                    if (!val.isEmpty()) {
                        try {
                            int m = Integer.parseInt(val);
                            tvPoint.setText("Point: " + calculateGP(m).replace(".0", ""));
                        } catch (Exception e) { tvPoint.setText("Point: 0"); }
                    } else { tvPoint.setText("Point: 0"); }

                    updateLiveTotal();

                    if (val.length() == 3 && !val.equals("100")) { s.delete(2, 3); return; }
                    boolean isFullValue = (val.length() == 2 && !val.equals("10")) || (val.length() == 3 && val.equals("100"));
                    if (isFullValue) {
                        moveToNext(index);
                    }
                }
            });

            row.addView(tvSubName);
            row.addView(et);
            row.addView(tvPoint);
            dynamicSubjectLayout.addView(row);
            subjectInputList.add(et);
        }

        tvTotalMarksDisplay = new TextView(this);
        tvTotalMarksDisplay.setVisibility(View.GONE);
        tvTotalMarksDisplay.setTextSize(18);
        tvTotalMarksDisplay.setPadding(0, 25, 0, 15);
        tvTotalMarksDisplay.setGravity(Gravity.CENTER);
        dynamicSubjectLayout.addView(tvTotalMarksDisplay);
    }


    @SuppressLint("SetTextI18n")
    private void updateLiveTotal() {
        int total = 0;
        boolean isAnyEmpty = false;
        boolean isAnyFail = false;

        for (EditText et : subjectInputList) {
            String val = et.getText().toString().trim();
            if (val.isEmpty()) { isAnyEmpty = true; break; }
            int m = Integer.parseInt(val);
            if (m < 33) isAnyFail = true;
            total += m;
        }

        if (!isAnyEmpty) {
            tvTotalMarksDisplay.setVisibility(View.VISIBLE);
            if (isAnyFail) {
                tvTotalMarksDisplay.setText("Total: Failed");
                tvTotalMarksDisplay.setTextColor(Color.RED);
            } else {
                tvTotalMarksDisplay.setText("Total: " + total);
                tvTotalMarksDisplay.setTextColor(Color.BLACK);
            }
        } else {
            tvTotalMarksDisplay.setVisibility(View.GONE);
        }
    }

    private String calculateGP(int marks) {
        if (marks >= 80) return "5.0";
        if (marks >= 70) return "4.0";
        if (marks >= 60) return "3.5";
        if (marks >= 50) return "3.0";
        if (marks >= 40) return "2.0";
        if (marks >= 33) return "1.0";
        return "0.0";
    }

    private void moveToNext(int currentIndex) {
        int nextEmptyIndex = -1;
        for (int i = 0; i < subjectInputList.size(); i++) {
            if (subjectInputList.get(i).getText().toString().trim().isEmpty()) {
                nextEmptyIndex = i;
                break;
            }
        }
        if (nextEmptyIndex != -1) {
            EditText nextEt = subjectInputList.get(nextEmptyIndex);
            nextEt.setTag(false);
            nextEt.requestFocus();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(subjectInputList.get(currentIndex).getWindowToken(), 0);
        }
    }

    private void saveData() {
        // ১. পেমেন্ট সিকিউরিটি চেক (২ জনের পর পেমেন্ট চাইবে)
        int currentCount = dbHelper.getExamineeCount();
        if (currentCount >= 2 && !getIntent().getBooleanExtra("IS_APPROVED", false)) {
            showPaymentDialog(); // পেমেন্ট ডায়ালগ দেখাবে
            return;
        }

        // ২. ইনপুট ভ্যালিডেশন
        String rollStr = etRoll.getText().toString().trim();
        String name = etName.getText().toString().trim();
        if (rollStr.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "রোল এবং নাম অবশ্যই দিন", Toast.LENGTH_SHORT).show();
            return;
        }

        int roll = Integer.parseInt(rollStr);
        StringBuilder marksBuilder = new StringBuilder();
        int totalMarks = 0;
        double totalGradePoints = 0;
        boolean isFailed = false;

        // ৩. মার্কস এবং জিপিএ গণনা
        for (EditText et : subjectInputList) {
            String input = et.getText().toString().trim();
            int m = 0;
            if (input.isEmpty()) {
                marksBuilder.append("×").append(",");
                isFailed = true;
            } else {
                m = Integer.parseInt(input);
                totalMarks += m;
                marksBuilder.append(m).append(",");
                if (m < 33) isFailed = true;
            }
            totalGradePoints += Double.parseDouble(calculateGP(m));
        }

        String finalGrade;
        double finalGpa = isFailed ? 0.00 : totalGradePoints / subjectCount;
        if (isFailed) finalGrade = "F";
        else if (finalGpa >= 5.00) finalGrade = "A+";
        else if (finalGpa >= 4.00) finalGrade = "A";
        else if (finalGpa >= 3.50) finalGrade = "A-";
        else if (finalGpa >= 3.00) finalGrade = "B";
        else if (finalGpa >= 2.00) finalGrade = "C";
        else if (finalGpa >= 1.00) finalGrade = "D";
        else finalGrade = "F";

        // ৪. ডাটাবেসে সেভ করা
        boolean success = dbHelper.insertOrUpdateResult(roll, name, className, marksBuilder.toString(), totalMarks, finalGpa, finalGrade, subjectCount);
        if (success) {
            Toast.makeText(this, "সফলভাবে সংরক্ষিত", Toast.LENGTH_SHORT).show();
            clearFields();
        }
    }


    private void clearFields() {
        etRoll.setText("");
        etName.setText("");
        for (EditText et : subjectInputList) {
            et.setText("");
            et.setTag(false);
        }
        tvTotalMarksDisplay.setVisibility(View.GONE);
        etRoll.requestFocus();

        etRoll.requestFocus();
// এই অংশটুকু কিবোর্ডকে ফোর্সফুলি দেখানোর জন্য সবচেয়ে ভালো
        etRoll.postDelayed(() -> {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etRoll, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100); // ১০০ মিলিসেকেন্ড অপেক্ষা করবে যাতে ভিউটি পুরোপুরি ফোকাস হওয়ার সুযোগ পায়


    }

    private void showPaymentDialog() {
        String message = "আপনার নিরবচ্ছিন্ন ব্যবহারের জন্য আমাদের এই ক্ষুদ্র প্রচেষ্টা। " +
                "২ জনের বেশি পরীক্ষার্থীর তথ্য সংরক্ষণ এবং অ্যাপের সকল ফিচার আনলক করতে " +
                "আমাদের প্রিমিয়াম মেম্বারশিপ প্রয়োজন।\n\n" +
                "💳 **যোগাযোগ (বিকাশ/নগদ):**\n" +
                "📞 01983422095\n\n" +
                "আপনার সহযোগিতায় আমরা আরও উন্নত সেবা দিতে উৎসাহিত হব। ধন্যবাদ!";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("প্রিমিয়াম অ্যাক্সেস প্রয়োজন")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("ঠিক আছে", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("পরে করব", (dialog, which) -> {
                    // ইউজার চাইলে এটা ক্যানসেল করতে পারবে না আমাদের লজিক অনুযায়ী,
                    // তবে বাটন থাকলে দেখতে সুন্দর লাগে।
                    dialog.dismiss();
                })
                .show();
    }




}
