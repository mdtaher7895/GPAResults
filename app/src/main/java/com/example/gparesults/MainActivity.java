package com.example.gparesults; // আপনার প্যাকেজ নাম ঠিক রাখুন

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner subjectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        Button btnMadrasa = findViewById(R.id.btnMadrasa);

        // ৪ থেকে ১৫ টি সাবজেক্টের অপশন
        List<String> subList = new ArrayList<>();
        for (int i = 4; i <= 15; i++) subList.add(i + " টি বিষয়");

        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subList);
        subjectSpinner.setAdapter(subAdapter);

        btnMadrasa.setOnClickListener(v -> {
            int subjects = subjectSpinner.getSelectedItemPosition() + 4;
            // সরাসরি ইনপুট স্ক্রিনে পাঠিয়ে দেওয়া হচ্ছে
            Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
            intent.putExtra("SUB_COUNT", subjects);
            startActivity(intent);
        });
    }
}
