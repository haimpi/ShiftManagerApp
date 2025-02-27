package com.example.shiftmanagerapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shiftmanagerapp.R;
import com.example.shiftmanagerapp.models.Shift;
import java.util.List;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder> {
    private List<Shift> shiftList;
    private OnShiftActionListener listener;

    public interface OnShiftActionListener {
        void onApply(Shift shift);
    }

    public ShiftAdapter(List<Shift> shiftList, OnShiftActionListener listener) {
        this.shiftList = shiftList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shift, parent, false);
        return new ShiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        Shift shift = shiftList.get(position);
        Log.d("ShiftAdapter", "Binding shift at position " + position + ": " +
                (shift != null ? "date=" + shift.getDate() + ", type=" + shift.getShift_type() : "null"));
        holder.bind(shift);
    }

    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    class ShiftViewHolder extends RecyclerView.ViewHolder {
        TextView txtShiftInfo;
        Button btnApply;

        ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShiftInfo = itemView.findViewById(R.id.txtShiftInfo);
            btnApply = itemView.findViewById(R.id.btnApply);
        }

        void bind(Shift shift) {
            if (shift != null) {
                String info = String.format("%s - %s\nWaiters: %d\nBartenders: %d\nShift Managers: %d\nCooks: %d",
                        shift.getDate() != null ? shift.getDate() : "N/A",
                        shift.getShift_type() != null ? shift.getShift_type() : "N/A",
                        shift.getWaiters(), shift.getBarmen(),
                        shift.getAdmins(), shift.getCooks());
                txtShiftInfo.setText(info);
                btnApply.setOnClickListener(v -> {
                    if (listener != null && shift != null) {
                        Log.d("ShiftAdapter", "Apply clicked for shift: " + shift.getDate() + " - " + shift.getShift_type());
                        listener.onApply(shift);
                    }
                });
            } else {
                txtShiftInfo.setText("Invalid Shift Data");
                btnApply.setVisibility(View.GONE);
                Log.w("ShiftAdapter", "Shift is null in bind");
            }
        }
    }
}