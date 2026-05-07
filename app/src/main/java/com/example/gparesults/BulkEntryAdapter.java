package com.example.gparesults;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BulkEntryAdapter extends RecyclerView.Adapter<BulkEntryAdapter.ViewHolder> {

    private List<ResultModel> studentList;
    private boolean isNameVisible = false;
    private int yellowSignalPosition = -1; // ইয়েলো সিগন্যালের পজিশন রাখার জন্য

    public interface OnDataChangeListener {
        void onDataChanged();
    }
    private OnDataChangeListener mListener;

    public BulkEntryAdapter(List<ResultModel> studentList, OnDataChangeListener listener) {
        this.studentList = studentList;
        this.mListener = listener;
    }

    public BulkEntryAdapter(List<ResultModel> studentList) {
        this.studentList = studentList;
    }

    // আপনার ইয়েলো সিগন্যাল সেট করার নতুন মেথড
    public void setYellowSignal(int position) {
        this.yellowSignalPosition = position;
        notifyDataSetChanged();
    }

    public void setNameVisible(boolean visible) {
        this.isNameVisible = visible;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bulk_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultModel student = studentList.get(position);

        // ১. টেক্সট ওয়াচার রিমুভ করা
        if (holder.marksWatcher != null) holder.etMarks.removeTextChangedListener(holder.marksWatcher);
        if (holder.nameWatcher != null) holder.etName.removeTextChangedListener(holder.nameWatcher);



        holder.tvRoll.setBackgroundColor(Color.LTGRAY);
        holder.etName.setBackgroundColor(Color.LTGRAY);
        holder.etMarks.setBackgroundColor(Color.LTGRAY);



        holder.tvRoll.setTextColor(Color.BLACK);
        holder.etName.setTextColor(Color.BLACK);
        holder.etMarks.setTextColor(Color.BLACK);




        holder.tvRoll.setText(String.valueOf(student.getRoll()));
        holder.etName.setText(student.getName());
        holder.etName.setVisibility(isNameVisible ? View.VISIBLE : View.GONE);




        // নম্বর দেখানোর জায়গায় ফিল্টারিং লজিক
        String marks = student.getMarks();
        if (marks == null || marks.trim().equals("×") || marks.contains("×")) {
            holder.etMarks.setText(""); // কোনো চিহ্ন থাকলে ঘর খালি দেখাবে
        } else {
            holder.etMarks.setText(marks); // শুধু আসল নম্বর দেখাবে
        }

        // টাচ করার সাথে সাথে পুরনো ডিজিট মুছে ফেলার লজিক
        holder.etMarks.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.etMarks.setText("");
            }
        });




        // ৩. ইয়েলো সিগন্যাল ও ফোকাস লজিক (কিবোর্ড সমস্যা সমাধানের জন্য সংশোধিত)
        holder.etMarks.setBackgroundResource(R.drawable.cell_border); // সবসময় বর্ডার থাকবে
        if (position == yellowSignalPosition) {
            holder.etMarks.getBackground().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_ATOP);

            // কিবোর্ড যেন সংখ্যা (Decimal) মোডেই থাকে তা জোরপূর্বক নিশ্চিত করা
            holder.etMarks.setRawInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

            holder.etMarks.requestFocus();
        } else {
            holder.etMarks.getBackground().clearColorFilter();
        }



        // ৪. নম্বর পরিবর্তনের লিসেনার (যেখানে আপনার কাঙ্ক্ষিত লাইনটি যুক্ত হয়েছে)
        holder.marksWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentPos = holder.getAdapterPosition();

                // ১. টাইপ শুরু করলে হলুদ সিগন্যাল বন্ধ (রিফ্রেশ ছাড়া)
                if (s.length() > 0 && currentPos == yellowSignalPosition) {
                    yellowSignalPosition = -1;
                }

                // ২. অটো-জাম্প এবং শেষ রোলে কিবোর্ড লুকানোর লজিক
                String input = s.toString();
                if ((input.length() == 2 && !input.equals("10")) || input.length() == 3) {
                    if (currentPos == studentList.size() - 1) {
                        // শেষ ছাত্র হলে কিবোর্ড পালাবে
                        holder.etMarks.clearFocus();
                        hideKeyboard(holder.itemView);
                        setYellowSignal(-1);
                    } else {
                        // না হলে পরের ঘরে যাবে
                        focusNextRow(currentPos, holder.itemView);
                    }
                }
            }


            @Override
            public void afterTextChanged(Editable s) {
                // ৩. ডাটা সেভ করা (আপনার আগের মেথড)
                student.setMarks(s.toString());
                if (mListener != null) mListener.onDataChanged();
            }
        };




        holder.etMarks.addTextChangedListener(holder.marksWatcher);

        // ৫. নাম পরিবর্তনের লিসেনার
        holder.nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                student.setName(s.toString());
                if (mListener != null) mListener.onDataChanged();
            }
        };
        holder.etName.addTextChangedListener(holder.nameWatcher);
    }


    // অটো-জাম্প এবং কিবোর্ড লুকানোর লজিক
    private void focusNextRow(int currentPosition, View itemView) {
        if (currentPosition < getItemCount() - 1) {
            setYellowSignal(currentPosition + 1);
        } else {
            // সর্বশেষ ছাত্রের কাজ শেষ হলে কিবোর্ড নামিয়ে ফেলা
            setYellowSignal(-1);
            hideKeyboard(itemView);
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoll;
        EditText etName, etMarks;
        TextWatcher marksWatcher;
        TextWatcher nameWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoll = itemView.findViewById(R.id.tvRollRow);
            etName = itemView.findViewById(R.id.etNameRow);
            etMarks = itemView.findViewById(R.id.etMarksRow);


        }
    }
}
