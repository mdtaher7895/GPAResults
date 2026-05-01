package com.example.gparesults;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private TableLayout rankingTable, gradeChartTable, summeryTable;
    private DBHelper dbHelper;
    private int subjectCount;
    private TextView tvTotalStudents, tvPassRate;
    private EditText etClassName;
    private LinearLayout mainContent;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ranking);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dbHelper = new DBHelper(this);
        rankingTable = findViewById(R.id.rankingTable);
        gradeChartTable = findViewById(R.id.gradeChartTable);
        summeryTable = findViewById(R.id.summeryTable);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvPassRate = findViewById(R.id.tvPassRate);
        etClassName = findViewById(R.id.etClassName);
        mainContent = findViewById(R.id.main_content);

        // --- নতুন লজিক শুরু ---
        View rootLayout = findViewById(R.id.rootLayout);
        android.os.Handler handler = new android.os.Handler();

        // ৫ সেকেন্ড পর কারসার বন্ধ করার রানাবল
        Runnable autoHideCursor = () -> {
            View current = getCurrentFocus();
            if (current instanceof EditText) {
                current.clearFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
            }
        };

        // বাইরে টাচ করলে কারসার লুকানো
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handler.removeCallbacks(autoHideCursor); // টাচ করলে আগের টাইমার বাতিল
                View focusedView = getCurrentFocus();
                if (focusedView instanceof EditText) {
                    focusedView.clearFocus();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return false;
        });

        // ৫ সেকেন্ড টাইমার লজিক (যে কোনো এডিট টেক্সটে টাচ করলে টাইমার শুরু হবে)
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                handler.removeCallbacks(autoHideCursor);
                handler.postDelayed(autoHideCursor, 5000); // ৫০০০ মিলিসেকেন্ড = ৫ সেকেন্ড
            } else {
                handler.removeCallbacks(autoHideCursor);
            }
        };

        // আপনার এডিট টেক্সটগুলোতে এই টাইমারটি সেট করা (অন্যান্য গুলোর জন্যও এটি কাজ করবে)
        etClassName.setOnFocusChangeListener(focusListener);
        findViewById(R.id.etInstitutionName).setOnFocusChangeListener(focusListener);
        findViewById(R.id.etInstiutionName).setOnFocusChangeListener(focusListener);
        findViewById(R.id.etIniutionName).setOnFocusChangeListener(focusListener);
        // --- নতুন লজিক শেষ ---

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        subjectCount = getIntent().getIntExtra("SUB_COUNT", 4);

        setupGradeChart(gradeChartTable);
        loadRankingTable();

        Button btnDownloadPDF = findViewById(R.id.btnDownloadPDF);
        if (btnDownloadPDF != null) {
            btnDownloadPDF.setOnClickListener(v -> generatePDFFromView(mainContent));
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    // ৩. সমাধান: মিসিং ScaleListener ক্লাস যুক্ত করা হলো
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
            mainContent.setScaleX(scaleFactor);
            mainContent.setScaleY(scaleFactor);
            mainContent.setPivotX(0);
            mainContent.setPivotY(0);
            return true;
        }
    }

    private void generatePDFFromView(View view) {
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);

        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = etClassName.getText().toString() + "_Result";

        printManager.print(jobName, new PrintDocumentAdapter() {
            PdfDocument pdfDocument;

            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                pdfDocument = new PdfDocument();
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("result.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                // ১. এ৪ ল্যান্ডস্কেপ পেজের স্ট্যান্ডার্ড উইডথ (পিক্সেল হিসেবে প্রায় ৮৪২ পিক্সেল)
                int pdfPageWidth = 842;
                int viewWidth = view.getWidth();

                // ২. স্কেলিং ফ্যাক্টর বের করা (ভিউ বড় হলে ছোট করবে, ছোট হলে বড় করবে)
                float scale = (float) pdfPageWidth / (float) viewWidth;

                int pageNumber = 1;
                int lastBreakY = 0;
                int totalRows = rankingTable.getChildCount();

                float density = getResources().getDisplayMetrics().density;
                int topMarginPx = (int) (10 * density);
                int headerColor = Color.parseColor("#D1D1D1");

                for (int i = 0; i < totalRows; i++) {
                    View row = rankingTable.getChildAt(i);
                    boolean isHeader = false;

                    if (row instanceof TableRow) {
                        android.graphics.drawable.Drawable background = row.getBackground();
                        if (background instanceof android.graphics.drawable.ColorDrawable) {
                            if (((android.graphics.drawable.ColorDrawable) background).getColor() == headerColor) {
                                if (i > 0) isHeader = true;
                            }
                        }
                    }

                    boolean isLastRow = (i == totalRows - 1);

                    if (isHeader || isLastRow) {
                        int endY;
                        if (isLastRow) {
                            endY = (int) (rankingTable.getY() + row.getBottom() + 50);
                        } else {
                            endY = (int) (rankingTable.getY() + row.getTop());
                        }

                        int pageHeight = endY - lastBreakY;

                        if (pageHeight > 0) {
                            // স্কেলিং অনুযায়ী পেজ হাইট অ্যাডজাস্ট করা
                            int finalPageHeight = (int) ((pageNumber > 1 ? (pageHeight + topMarginPx) : pageHeight) * scale);

                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pdfPageWidth, finalPageHeight, pageNumber).create();
                            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                            Canvas canvas = page.getCanvas();
                            canvas.save();

                            // ৩. স্কেলিং লজিক প্রয়োগ (পুরো ভিউকে অটো-ফিট করবে)
                            canvas.scale(scale, scale);

                            // ৪. আপনার বিদ্যমান ট্রান্সলেট লজিক
                            if (pageNumber > 1) {
                                canvas.translate(0, -lastBreakY + topMarginPx);
                            } else {
                                canvas.translate(0, -lastBreakY);
                            }

                            view.draw(canvas);
                            canvas.restore();
                            pdfDocument.finishPage(page);

                            lastBreakY = endY;
                            pageNumber++;
                        }
                    }
                }

                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                } finally {
                    pdfDocument.close();
                    pdfDocument = null;
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, attributes);
    }








    private void setupSummeryTable(TableLayout summeryTable, int gpa5, int a, int aMinus, int b, int c, int d, int f) {
        summeryTable.removeAllViews();

        // ডাটা সেট (টাইটেল ছাড়া এবং A+ সহ)
        String[][] data = {
                {"A+", convertToBengali(gpa5) + " জন"},
                {"A", convertToBengali(a) + " জন"},
                {"A-", convertToBengali(aMinus) + " জন"},
                {"B", convertToBengali(b) + " জন"},
                {"C", convertToBengali(c) + " জন"},
                {"D", convertToBengali(d) + " জন"},
                {"F (ফেল)", convertToBengali(f) + " জন"}
        };

        // লুপের মাধ্যমে টেবিল তৈরি
        for (int i = 0; i < data.length; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < data[i].length; j++) {
                TextView tv = new TextView(this);
                tv.setText(data[i][j]);

                FontUtils.applyCustomFont(this, tv, data[i][j]);

                // লাইনের উচ্চতা/স্পেস কমাতে ২ এবং ৮ প্যাডিং ব্যবহার করা হয়েছে
                tv.setPadding(35, 0, 35, 0);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(15);
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundResource(R.drawable.table_border_header);


                row.addView(tv);
            }
            summeryTable.addView(row);
        }
    }



    private void setupGradeChart(TableLayout gradeChartTable) {
        gradeChartTable.removeAllViews();
        String[][] gradeData = {
                {"গ্রেড নির্ণয়", "", ""},
                {"৮০ - ১০০", "A+", "৫.০০"},
                {"৭০ - ৭৯", "A", "৪.০০"},
                {"৬০ - ৬৯", "A-", "৩.৫০"},
                {"৫০ - ৫৯", "B", "৩.০০"},
                {"৪০ - ৪৯", "C", "২.০০"},
                {"৩৩ - ৩৯", "D", "১.০০"},
                {"০০ - ৩২", "F", "০.৯৯"}
        };

        for (int i = 0; i < gradeData.length; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < gradeData[i].length; j++) {
                TextView tv = new TextView(this);
                tv.setText(gradeData[i][j]);

                // সাধারণ ফন্ট অ্যাপ্লাই
                FontUtils.applyCustomFont(this, tv, gradeData[i][j]);

                tv.setPadding(25, 4, 25, 4);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(15);
                tv.setTextColor(Color.BLACK);

                // কাস্টম বর্ডার সেট করা
                tv.setBackgroundResource(R.drawable.table_border_header);

                if (i == 0 && j == 0) {
                    TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.span = 3;
                    tv.setLayoutParams(params);

                    // এখানে আগে বোল্ড সেট করে তারপর আবার ফন্ট অ্যাপ্লাই করা হয়েছে
                    // যাতে বোল্ড করার সময় ফন্টটি হারিয়ে না যায়
                    tv.setTypeface(null, Typeface.BOLD);
                    FontUtils.applyCustomFont(this, tv, gradeData[i][j]);

                    row.addView(tv);
                    break;
                } else {
                    row.addView(tv);
                }
            }
            gradeChartTable.addView(row);
        }
    }



    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void loadRankingTable() {
        rankingTable.removeAllViews();
        rankingTable.setStretchAllColumns(true);
        Cursor cursor = dbHelper.getDataBySubjectCount(subjectCount);
        List<StudentModel> rankingList = new ArrayList<>();
        int g5=0, gA=0, gAm=0, gB=0, gC=0, gD=0, gF=0;

        String[] subjectNames = {
                "  আরবী  ", "  বাংলা  ", "  ইংরেজি  ", "  গণিত  ",
                "হাদিস শরীফ ও\nআস: হুসনা", "কালিমা ও\nমাসায়িল",
                "আদ: সালাত ও\nআদ:মাসনোনা", "কুরআন ও\nতাজবীদ",
                "প: সমাজ বিজ্ঞান\nও সাধারণ জ্ঞান"
        };

        // --- ১. হেডার তৈরির জন্য Runnable (এখানে ডাইনামিক -২ করা হয়েছে) ---
        Runnable addHeaderToTable = () -> {
            TableRow headerRow = new TableRow(this);
            headerRow.setBackgroundColor(Color.parseColor("#D1D1D1"));
            addCell(headerRow, " রোল ", true, -2);
            addCell(headerRow, " ছাত্রদের নাম ", true, -2);
            for (int i = 0; i < subjectCount; i++) {
                if (i < subjectNames.length) {
                    addCell(headerRow, subjectNames[i], true, -2);
                } else {
                    addCell(headerRow, "বি " + convertToBengali(i + 1), true, -2);
                }
            }
            addCell(headerRow, "সর্বমোট", true, -2);
            addCell(headerRow, " জিপিএ ", true, -2);
            addCell(headerRow, " গ্রেড ", true, -2);
            addCell(headerRow, "অবস্থান", true, -2);
            addCell(headerRow, "কার্য দিবস", true, -2);
            rankingTable.addView(headerRow);
        };

        if (cursor != null && cursor.moveToFirst()) {
            // ... (এখানে আপনার ক্যালকুলেশন লজিকগুলো আগের মতোই আছে) ...
            int totalExaminees = 0, passCount = 0;
            do {
                String marks = cursor.getString(2);
                String grade = cursor.getString(5);
                double gpa = cursor.getDouble(4);
                boolean isFullyAbsent = true;
                for (String m : marks.split(",")) {
                    if (!m.trim().equals("×")) { isFullyAbsent = false; break; }
                }
                boolean hasCross = marks.contains("×");
                boolean isFails = hasCross || grade.equals("F");

                StudentModel s = new StudentModel(cursor.getInt(0), cursor.getString(1), marks, cursor.getInt(3), gpa, grade);
                s.isFullyAbsent = isFullyAbsent;
                s.isFails = isFails;
                rankingList.add(s);

                if (!isFullyAbsent) {
                    totalExaminees++;
                    if (isFails) gF++;
                    else {
                        if (grade.equals("A+")) g5++; else if (grade.equals("A")) gA++; else if (grade.equals("A-")) gAm++;
                        else if (grade.equals("B")) gB++; else if (grade.equals("C")) gC++; else if (grade.equals("D")) gD++;
                        passCount++;
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();

            FontUtils.applyCustomFont(this, tvTotalStudents, convertToBengali(totalExaminees) + " জন");
            if (totalExaminees > 0) FontUtils.applyCustomFont(this, tvPassRate, convertToBengali(String.format("%.2f", ((double)passCount/totalExaminees)*100)) + "%");
            setupSummeryTable(summeryTable, g5, gA, gAm, gB, gC, gD, gF);

            addHeaderToTable.run();

            int studentInCurrentPage = 0;
            int currentPageLimit = 18;

            for (int i = 0; i < rankingList.size(); i++) {
                if (studentInCurrentPage == currentPageLimit) {
                    // (পেজ ব্রেকিং লজিক আগের মতোই থাকবে)
                    View line = new View(this);
                    line.setBackgroundColor(Color.BLACK);
                    rankingTable.addView(line, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 2));

                    View spacerView = new View(this);
                    spacerView.setBackgroundColor(Color.WHITE);
                    TableLayout.LayoutParams spacerParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 150);
                    spacerParams.setMargins(0, 0, -10, 0);
                    rankingTable.addView(spacerView, spacerParams);

                    addHeaderToTable.run();
                    studentInCurrentPage = 0;
                    currentPageLimit = 26;
                }

                StudentModel s = rankingList.get(i);
                TableRow row = new TableRow(this);

                // --- ডাটা রো-তে ডাইনামিক -২ পরিবর্তন ---
                addCell(row, convertToBengali(s.roll), false, -2);
                addCell(row, s.name, false, -2);

                for (String m : s.marks.split(",")) {
                    addCell(row, m.trim().equals("×") ? "×" : convertToBengali(m.trim()), false, -2);
                }

                if (s.isFullyAbsent) {
                    for(int j=0; j<4; j++) addCell(row, "×", false, -2);
                } else if (s.isFails) {
                    addCellWithColor(row, "ফেল", Color.RED, -2);
                    addCellWithColor(row, "Fail", Color.RED, -2);
                    addCellWithColor(row, "F", Color.RED, -2);
                    addCell(row, "০", false, -2);
                } else {
                    addCell(row, convertToBengali(s.total), false, -2);
                    addCell(row, String.format("%.2f", s.gpa), false, -2);
                    addCell(row, s.grade, false, -2);

                    int pos = calculatePosition(s, rankingList);
                    String bPos = convertToBengali(pos);
                    if (pos == 1) addCellWithBG(row, bPos, Color.parseColor("#A9A9A9"), -2);
                    else if (pos == 2) addCellWithBG(row, bPos, Color.parseColor("#C0C0C0"), -2);
                    else if (pos == 3) addCellWithBG(row, bPos, Color.parseColor("#D3D3D3"), -2);
                    else addCell(row, bPos, false, -2);
                }

                addCell(row, (i == 6) ? "পরিচালকের স্বাক্ষর" : "", false, -2);
                rankingTable.addView(row);
                studentInCurrentPage++;
            }
        }
    }






    private String convertToBengali(Object input) {
        String str = String.valueOf(input);
        return str.replace('0', '০').replace('1', '১').replace('2', '২').replace('3', '৩')
                .replace('4', '৪').replace('5', '৫').replace('6', '৬').replace('7', '৭')
                .replace('8', '৮').replace('9', '৯');
    }

    private int calculatePosition(StudentModel current, List<StudentModel> all) {
        if (current.isFails) return 0;
        int pos = 1;
        for (StudentModel other : all) {
            if (other.isFails || other.isFullyAbsent) continue;
            if (other.gpa > current.gpa) pos++;
            else if (other.gpa == current.gpa && other.total > current.total) pos++;
        }
        return pos;
    }

    static class StudentModel {
        int roll, total; String name, marks, grade; double gpa;
        boolean isFails = false, isFullyAbsent = false;
        StudentModel(int r, String n, String m, int t, double g, String gr) {
            this.roll = r; this.name = n; this.marks = m; this.total = t; this.gpa = g; this.grade = gr;
        }
    }

    private void addCell(TableRow row, String text, boolean isHeader, int widthDp) {
        row.addView(createStyledTextView(text, isHeader, widthDp, Color.BLACK));
    }

    private void addCellWithColor(TableRow row, String text, int color, int widthDp) {
        row.addView(createStyledTextView(text, false, widthDp, color));
    }

    private TextView createStyledTextView(String text, boolean isHeader, int widthDp, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);

        // ৫ডিপি প্যাডিং
        int paddingPx = (int) (5 * getResources().getDisplayMetrics().density);
        tv.setPadding(paddingPx, 2, paddingPx, 2);
        tv.setGravity(Gravity.CENTER);

        // ডাইনামিক উইডথ লজিক
        TableRow.LayoutParams params;
        if (widthDp == -2) {
            params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
        } else {
            int widthPx = (int) (widthDp * getResources().getDisplayMetrics().density);
            params = new TableRow.LayoutParams(widthPx, TableRow.LayoutParams.MATCH_PARENT);
        }
        tv.setLayoutParams(params);

        tv.setSingleLine(false);
        tv.setMaxLines(3);
        tv.setHorizontallyScrolling(false);

        // ব্যাকগ্রাউন্ড এবং কালার লজিক
        if (isHeader) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            // আমি এখানে আপনার চাওয়া হালকা গাঢ় রঙ (#D1D1D1) বসিয়ে দিয়েছি
            gd.setColor(Color.parseColor("#D1D1D1"));
            gd.setStroke(1, Color.parseColor("#444444"));
            tv.setBackground(gd);
        } else {
            tv.setBackgroundResource(R.drawable.table_border_header);
        }

        tv.setTextColor(textColor);

        // --- সংশোধন: টেক্সট সাইজ লজিক এখানে যুক্ত করা হলো ---
        if (isHeader) {
            tv.setTextSize(35); // শিরোনাম বা সাবজেক্টের নাম বড় হবে
        } else {
            tv.setTextSize(30); // ছাত্রদের রোল, নাম ও নম্বর বড় হবে
        }

        // ফন্ট সেট করা
        try {
            Typeface tf = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.sutonnymjregular);
            tv.setTypeface(tf);
        } catch (Exception e) {
            FontUtils.applyCustomFont(this, tv, text);
        }

        if (isHeader) {
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        }

        return tv;
    }









    private void addCellWithBG(TableRow row, String text, int bgColor, int widthDp) {
        TextView tv = createStyledTextView(text, true, widthDp, Color.BLACK);
        tv.setBackgroundColor(bgColor);
        row.addView(tv);
    }


}
