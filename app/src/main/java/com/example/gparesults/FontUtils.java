package com.example.gparesults;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;

public class FontUtils {

    public static void applyCustomFont(Context context, TextView textView, String text) {
        if (text == null) return;

        // ১. বাংলা অক্ষর (Unicode) চেক করার লজিক
        boolean isBangla = text.matches(".*[\\u0980-\\u09FF].*");

        try {
            if (isBangla) {
                // বাংলার জন্য SutonnyMJ সেট করা হচ্ছে
                Typeface banglaFont = ResourcesCompat.getFont(context, R.font.sutonnymjregular);
                textView.setTypeface(banglaFont);
            } else {
                // ইংরেজি বা সংখ্যার জন্য Times New Roman সেট করা হচ্ছে
                Typeface englishFont = ResourcesCompat.getFont(context, R.font.timesnewromanregular);
                textView.setTypeface(englishFont);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        textView.setText(text);
    }

}
