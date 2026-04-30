package com.example.gparesults;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnMadrasa = findViewById(R.id.btnMadrasa);
        subjectSpinner = findViewById(R.id.subjectSpinner);

        // --- ফায়ারবেস কন্ট্রোল শুরু ---
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(deviceId);

        btnMadrasa.setEnabled(false); // শুরুতে বাটন লক

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("access").getValue(String.class);
                    String expiryDate = snapshot.child("expiry").getValue(String.class);

                    // বর্তমান তারিখ নেওয়া (yyyy-MM-dd ফরম্যাটে)
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    if ("approved".equals(status)) {
                        // মেয়াদ চেক: যদি মেয়াদ সেট করা না থাকে অথবা আজকের তারিখ মেয়াদের সমান বা কম হয়
                        if (expiryDate == null || today.compareTo(expiryDate) <= 0) {
                            btnMadrasa.setEnabled(true);
                        } else {
                            btnMadrasa.setEnabled(false);
                            Toast.makeText(MainActivity.this, "আপনার মেয়ার শেষ হয়ে গেছে (" + expiryDate + ")", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        btnMadrasa.setEnabled(false);
                        Toast.makeText(MainActivity.this, "আপনার এক্সেস পেন্ডিং আছে", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
                            // ডিফল্ট কোনো মেয়াদ না থাকলে আনলিমিটেড থাকবে, আপনি পিসি থেকে পরে বসাতে পারবেন
                        } else {
                            Toast.makeText(MainActivity.this, "নাম লেখা বাধ্যতামূলক", Toast.LENGTH_SHORT).show();
                            recreate();
                        }
                    });
                    builder.show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        // --- ফায়ারবেস কন্ট্রোল শেষ ---

        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 15; i++) subList.add(i + " টি বিষয়");

        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subList);
        subjectSpinner.setAdapter(subAdapter);

        btnMadrasa.setOnClickListener(v -> {
            int subjects = subjectSpinner.getSelectedItemPosition() + 4;
            Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
            intent.putExtra("SUB_COUNT", subjects);
            startActivity(intent);
        });
    }
}
