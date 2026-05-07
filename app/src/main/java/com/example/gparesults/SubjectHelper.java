package com.example.gparesults;

import java.util.ArrayList;
import java.util.List;

public class SubjectHelper {

    // সাবজেক্টের নামগুলো একটি স্ট্যাটিক মেথডে রাখা হলো যাতে অবজেক্ট ছাড়াই কল করা যায়
    public static List<String> getSubjectList(int subjectCount) {
        List<String> list = new ArrayList<>();

        // আপনার দেওয়া সেই ৯টি নাম ঠিক যেভাবে আপনি চেয়েছিলেন
        String[] subjectNames = {
                "  আরবী  ", "  বাংলা  ", "  ইংরেজি  ", "  গণিত  ",
                "হাদিস শরীফ ও\nআস: হুসনা", "কালিমা ও\nমাসায়িল",
                "আদ: সালাত ও\nআদ: মাসসূনা", "কুরআন ও\nতাজবীদ",
                "প: সমাজ বিজ্ঞান\nও সাধারণ জ্ঞান"
        };

        for (int i = 0; i < subjectCount; i++) {
            if (i < subjectNames.length) {
                list.add(subjectNames[i]);
            } else {
                // যদি বিষয় সংখ্যা ৯ এর বেশি হয়, তবে অটোমেটিক নাম তৈরি হবে
                list.add("বিষয় " + (i + 1));
            }
        }
        return list;
    }
}
