package com.example.gparesults;

public class CalculatorUtils {

    // ১. গ্রেড পয়েন্ট হিসেব করার মেথড
    public static double getGradePoint(int marks) {
        if (marks >= 80) return 5.0;
        if (marks >= 70) return 4.0;
        if (marks >= 60) return 3.5;
        if (marks >= 50) return 3.0;
        if (marks >= 40) return 2.0;
        if (marks >= 33) return 1.0;
        return 0.0;
    }

    // ২. লেটার গ্রেড হিসেব করার মেথড
    public static String getLetterGrade(double gpa) {
        if (gpa >= 5.0) return "A+";
        if (gpa >= 4.0) return "A";
        if (gpa >= 3.5) return "A-";
        if (gpa >= 3.0) return "B";
        if (gpa >= 2.0) return "C";
        if (gpa >= 1.0) return "D";
        return "F";
    }

    // ৩. বাল্ক ডাটার জন্য সম্পূর্ণ ক্যালকুলেশন মেথড
    public static ResultSummary calculateResult(String marksRaw, int subjectCount) {
        if (marksRaw == null || marksRaw.isEmpty()) return new ResultSummary(0, 0.0, "F");

        String[] marksArray = marksRaw.split(",");
        int total = 0;
        double totalGP = 0;
        boolean isFail = false;
        int actualSubCount = 0;

        for (String m : marksArray) {
            String markStr = m.trim();
            if (!markStr.equals("×") && !markStr.isEmpty()) {
                int mark = Integer.parseInt(markStr);
                total += mark;
                double gp = getGradePoint(mark);
                if (gp == 0.0) isFail = true; // কোনো একটিতে ফেল করলে পুরো রেজাল্ট ফেল
                totalGP += gp;
                actualSubCount++;
            } else {
                isFail = true; // অনুপস্থিত থাকলেও ফেল হিসেবে গণ্য হবে (আপনার লজিক অনুযায়ী)
            }
        }

        // যদি বিষয় সংখ্যা মিল না থাকে বা কোনোটিতে ফেল থাকে
        if (isFail || actualSubCount < subjectCount) {
            return new ResultSummary(total, 0.0, "F");
        }

        double finalGPA = totalGP / subjectCount;
        return new ResultSummary(total, finalGPA, getLetterGrade(finalGPA));
    }

    // রেজাল্ট জমা রাখার জন্য একটি ছোট ক্লাস
    public static class ResultSummary {
        public int total;
        public double gpa;
        public String grade;

        public ResultSummary(int total, double gpa, String grade) {
            this.total = total;
            this.gpa = gpa;
            this.grade = grade;
        }
    }
}
