package com.example.gparesults;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private Spinner classSpinner; // নতুন স্পিনার

    private boolean isApproved = false; // ইউজারের স্ট্যাটাস ট্র্যাক করার জন্য

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // সিস্টেম বার ইনসেট সেট করা
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnMadrasa = findViewById(R.id.btnMadrasa);
        Button btnSaved = findViewById(R.id.btnSaved);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        classSpinner = findViewById(R.id.classSpinner);


        // --- ফায়ারবেস কন্ট্রোল শুরু ---
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(deviceId);

        // বাটনটি সব সময় এনাবল থাকবে কারণ ৪-৫ সাবজেক্ট ফ্রি
        btnMadrasa.setEnabled(true);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("access").getValue(String.class);
                    String expiryDate = snapshot.child("expiry").getValue(String.class);
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    if ("approved".equals(status)) {
                        if (expiryDate == null || today.compareTo(expiryDate) <= 0) {
                            isApproved = true; // অ্যাপ্রুভড হলে ট্রু হবে
                        } else {
                            isApproved = false;
                            Toast.makeText(MainActivity.this, "মেয়াদ শেষ: " + expiryDate, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        isApproved = false;
                    }
                } else {
                    showRegistrationDialog(dbRef);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        // --- ফায়ারবেস কন্ট্রোল শেষ ---

        // ১. সাবজেক্ট লিস্ট তৈরি (সবার আগে এটি থাকবে)
        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i)
                    .replace('0','০').replace('1','১').replace('2','২')
                    .replace('3','৩').replace('4','৪').replace('5','৫')
                    .replace('6','৬').replace('7','৭').replace('8','৮')
                    .replace('9','৯');
            subList.add(bnNum + " টি বিষয়ের জন্য");
        }

        // ২. শ্রেণির লিস্ট তৈরি
        List<String> classList = new ArrayList<>();
        String[] classes = {
                "নার্সারি", "প্রথম শ্রেণি", "দ্বিতীয় শ্রেণি", "তৃতীয় শ্রেণি", "চতুর্থ শ্রেণি",
                "পঞ্চম শ্রেণি", "ষষ্ঠ শ্রেণি", "সপ্তম শ্রেণি", "অষ্টম শ্রেণি", "নবম শ্রেণি",
                "দশম শ্রেণি", "একাদশ শ্রেণি", "দ্বাদশ শ্রেণি", "ত্রয়োদশ শ্রেণি", "চতুর্দশ শ্রেণি",
                "পঞ্চদশ শ্রেণি", "ষোড়শ শ্রেণি", "সপ্তদশ শ্রেণি", "অষ্টাদশ শ্রেণি", "উনবিংশ শ্রেণি", "বিংশ শ্রেণি"
        };
        for (String c : classes) { classList.add(c); }

        // ৩. শ্রেণির স্পিনারের জন্য অ্যাডাপ্টার
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
        classSpinner.setAdapter(classAdapter);

        // ৪. সাবজেক্ট স্পিনারের জন্য অ্যাডাপ্টার (এখানে এখন আর subList এরর আসবে না)
        ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subList) {
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


        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subAdapter);

        subjectSpinner.setSelection(0); // ডিফল্ট ৪টি বিষয়

        // ৪. স্পিনারের লজিক (পেইড/ফ্রি পপ-আপ)
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // পজিশন ২ বা তার বেশি মানে ৬ থেকে ২০টি বিষয়
                if (position > 1 && !isApproved) {
                    showPaymentDialog();
                    subjectSpinner.setSelection(1); // ৫টি বিষয়ে ফেরত আনা
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ৫. বাটন ক্লিক লজিক (নিখুঁত সিকিউরিটি চেক)
        btnMadrasa.setOnClickListener(v -> {
            int position = subjectSpinner.getSelectedItemPosition();
            int subjects = position + 4;
            String selectedClass = classSpinner.getSelectedItem().toString(); // এই লাইনটি যোগ করুন

            Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
            intent.putExtra("SUB_COUNT", subjects);
            intent.putExtra("CLASS_NAME", selectedClass); // এই লাইনটি ডাটা পাঠাবে

            if (position <= 1 || isApproved) {
                startActivity(intent);
            } else {
                showPaymentDialog();
                subjectSpinner.setSelection(1);
            }
        });





        btnSaved.setOnClickListener(v -> {
            // সেভ করা ফাইলগুলো দেখার জন্য টোস্ট মেসেজ (আপাতত)
            Toast.makeText(MainActivity.this, "সেভ করা ফাইল দেখার সুবিধা শীঘ্রই আসছে!", Toast.LENGTH_SHORT).show();

            // ভবিষ্যৎ লজিক এখানে লিখবেন (যেমন অন্য কোনো অ্যাক্টিভিটিতে যাওয়া)
            Intent intent = new Intent(MainActivity.this, SavedListActivity.class);
            startActivity(intent);
        });





    }

    // রেজিস্ট্রেশন ডায়ালগ
    private boolean isPhoneWarningShown = false; // এটি মেথডের বাইরে বা উপরে ঘোষণা করতে পারেন

    private void showRegistrationDialog(DatabaseReference dbRef) {
        isPhoneWarningShown = false; // প্রতিবার ডায়ালগ ওপেন হলে এটি রিসেট হবে

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("রেজিস্ট্রেশন করুন");
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etName = new EditText(this);
        etName.setHint("আপনার পূর্ণ নাম বা\n(প্রতিষ্ঠানের নাম)");
        layout.addView(etName);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("ফোন নম্বর");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        builder.setView(layout);
        builder.setPositiveButton("সেভ করুন", null);

        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        alertDialog.show();
        etName.requestFocus();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String userName = etName.getText().toString().trim();
            String userPhone = etPhone.getText().toString().trim();

            // ১. নামের শর্ত (সবসময় বাধ্যতামূলক)
            String[] words = userName.split("\\s+");
            boolean isBengali = userName.matches("^[\\u0980-\\u09FF\\s]+$");

            if (userName.isEmpty()) {
                Toast.makeText(MainActivity.this, "নাম লেখা বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isBengali || words.length < 2) {
                Toast.makeText(MainActivity.this, "নাম অবশ্যই বাংলায় এবং কমপক্ষে ২ শব্দের হতে হবে", Toast.LENGTH_SHORT).show();
                return;
            }

            // ২. ফোন নম্বরের বিশেষ লজিক
            if (userPhone.isEmpty() && !isPhoneWarningShown) {
                // প্রথমবার খালি থাকলে সতর্কবার্তা দেবে
                Toast.makeText(MainActivity.this, "আপনি ফোন নম্বর দিন।", Toast.LENGTH_LONG).show();
                isPhoneWarningShown = true; // একবার সতর্ক করা হয়েছে
                return;
            }

            // ৩. সব ঠিক থাকলে বা দ্বিতীয়বার ক্লিক করলে ডাটা সেভ
            dbRef.child("name").setValue(userName);
            dbRef.child("phone").setValue(userPhone.isEmpty() ? "Not Provided" : userPhone);
            dbRef.child("access").setValue("pending");
            dbRef.child("model").setValue(android.os.Build.MODEL);

            Toast.makeText(MainActivity.this, "তথ্য জমা হয়েছে, অনুমোদনের অপেক্ষা করুন", Toast.LENGTH_LONG).show();
            alertDialog.dismiss();
            new android.os.Handler().postDelayed(this::recreate, 1000);
        });
    }




    // পেমেন্ট ডায়ালগ
    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("পেইড ভার্সন প্রয়োজন");
        String message = "৬ থেকে ২০টি বিষয় ব্যবহার করতে হলে আপনাকে পেইড মেম্বার হতে হবে।\n\n" +
                "পেমেন্ট করতে যোগাযোগ করুন:\n" +
                "বিকাশ/নগদ: 01983422095\n\n" +
                "টাকা পাঠানোর পর আপনার আইডি সক্রিয় করে দেওয়া হবে।";
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("ঠিক আছে", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
