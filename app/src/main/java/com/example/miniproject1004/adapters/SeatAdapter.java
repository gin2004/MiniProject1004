package com.example.miniproject1004.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject1004.R;
import com.example.miniproject1004.models.Seat;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private List<Seat> seatList;
    private OnSeatClickListener listener;

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat);
    }

    public SeatAdapter(List<Seat> seatList, OnSeatClickListener listener) {
        this.seatList = seatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);
        holder.tvSeatId.setText(seat.getId());

        if (seat.isBooked()) {
            holder.itemView.setBackgroundResource(R.color.seatBooked);
            holder.tvSeatId.setTextColor(Color.GRAY);
            holder.itemView.setEnabled(false);
        } else if (seat.isSelected()) {
            holder.itemView.setBackgroundResource(R.color.seatSelected);
            holder.tvSeatId.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(R.color.seatAvailable);
            holder.tvSeatId.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!seat.isBooked()) {
                seat.setSelected(!seat.isSelected());
                notifyItemChanged(position);
                listener.onSeatClick(seat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeatId;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatId = itemView.findViewById(R.id.tvSeatId);
        }
    }
}