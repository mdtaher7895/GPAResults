package com.example.gparesults;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BulkEntryAdapter extends RecyclerView.Adapter<BulkEntryAdapter.ViewHolder> {

    private List<ResultModel> studentList;
    private boolean isNameVisible = false; // শুরুতে নাম হাইড থাকবে

    public BulkEntryAdapter(List<ResultModel> studentList) {
        this.studentList = studentList;
    }

    // নাম দেখানো বা লুকানোর মেথড (আপনার টগল লজিকের জন্য)
    public void setNameVisible(boolean visible) {
        this.isNameVisible = visible;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // এখানে আপনার সেই নতুন XML ডিজাইনটি লোড হবে (row_bulk_entry)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bulk_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultModel student = studentList.get(position);
        holder.tvRoll.setText(String.valueOf(student.getRoll()));
        holder.etName.setText(student.getName());

        // টগল লজিক অনুযায়ী নাম দেখানো বা লুকানো
        if (isNameVisible) {
            holder.etName.setVisibility(View.VISIBLE);
        } else {
            holder.etName.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoll;
        EditText etName, etMarks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoll = itemView.findViewById(R.id.tvRollRow);
            etName = itemView.findViewById(R.id.etNameRow);
            etMarks = itemView.findViewById(R.id.etMarksRow);
        }
    }
}
