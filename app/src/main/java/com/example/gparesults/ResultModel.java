package com.example.gparesults;

public class ResultModel {
    private int roll;
    private String name;
    private String marks;
    private int total;
    private double gpa;
    private String grade;
    private int subCount;

    // ১. খালি কনস্ট্রাক্টর (এটি যুক্ত করা হয়েছে, এরর দূর করতে এটিই সবচেয়ে জরুরি)
    public ResultModel() {
    }

    // ২. পুরনো কনস্ট্রাক্টরটি নিচে রাখা হলো (যাতে অন্য কোথাও সমস্যা না হয়)
    public ResultModel(int roll, String name, String marks, int total, double gpa, String grade, int subCount) {
        this.roll = roll;
        this.name = name;
        this.marks = marks;
        this.total = total;
        this.gpa = gpa;
        this.grade = grade;
        this.subCount = subCount;
    }

    // ৩. গেটার মেথডগুলো
    public int getRoll() { return roll; }
    public String getName() { return name; }
    public String getMarks() { return marks; }
    public int getTotal() { return total; }
    public double getGpa() { return gpa; }
    public String getGrade() { return grade; }
    public int getSubCount() { return subCount; }

    // ৪. সেটার মেথডগুলো (এগুলো যুক্ত করা হয়েছে যাতে ডাটাবেজ থেকে ডাটা সেট করা যায়)
    public void setRoll(int roll) { this.roll = roll; }
    public void setName(String name) { this.name = name; }
    public void setMarks(String marks) { this.marks = marks; }
    public void setTotal(int total) { this.total = total; }
    public void setGpa(double gpa) { this.gpa = gpa; }
    public void setGrade(String grade) { this.grade = grade; }
    public void setSubCount(int subCount) { this.subCount = subCount; }
}
