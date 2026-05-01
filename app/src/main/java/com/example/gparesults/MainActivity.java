package com.example.gparesults;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

public class MainActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
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
        subjectSpinner = findViewById(R.id.subjectSpinner);

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

        // ১. সাবজেক্ট লিস্ট তৈরি
        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 20; i++) {
            String bnNum = String.valueOf(i)
                    .replace('0','০').replace('1','১').replace('2','২')
                    .replace('3','৩').replace('4','৪').replace('5','৫')
                    .replace('6','৬').replace('7','৭').replace('8','৮')
                    .replace('9','৯');
            subList.add(bnNum + " টি বিষয়ের জন্য");
        }

        // ২. কাস্টম অ্যাডাপ্টার
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

        // ৩. স্পিনারে অ্যাডাপ্টার সেট করা
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

            // যদি ৪ বা ৫ সাবজেক্ট হয়, তবে সরাসরি যাবে
            if (position <= 1) {
                Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                intent.putExtra("SUB_COUNT", subjects);
                startActivity(intent);
            }
            // যদি ৬ বা তার বেশি হয়, তবে অ্যাপ্রুভড চেক করবে
            else {
                if (isApproved) {
                    Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                    intent.putExtra("SUB_COUNT", subjects);
                    startActivity(intent);
                } else {
                    showPaymentDialog();
                    subjectSpinner.setSelection(1);
                }
            }
        });
    }

    // রেজিস্ট্রেশন ডায়ালগ
    private void showRegistrationDialog(DatabaseReference dbRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("আপনার নাম লিখুন");
        builder.setCancelable(false);
        final EditText input = new EditText(MainActivity.this);
        builder.setView(input);
        builder.setPositiveButton("সেভ করুন", (dialog, which) -> {
            String userName = input.getText().toString();
            if (!userName.isEmpty()) {
                dbRef.child("name").setValue(userName);
                dbRef.child("access").setValue("pending");
                dbRef.child("model").setValue(Build.MODEL);
            } else {
                Toast.makeText(MainActivity.this, "নাম লেখা বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });
        builder.show();
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
