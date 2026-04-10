package com.example.miniproject1004.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject1004.R;
import com.example.miniproject1004.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.tvMovieTitle.setText(ticket.getMovieTitle());
        holder.tvTheater.setText(ticket.getTheaterName());
        holder.tvSeat.setText("Seat: " + ticket.getSeatNumber());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", ticket.getPrice()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDateTime.setText(sdf.format(ticket.getShowTimeDate()));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvTheater, tvDateTime, tvSeat, tvPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvHistoryMovieTitle);
            tvTheater = itemView.findViewById(R.id.tvHistoryTheater);
            tvDateTime = itemView.findViewById(R.id.tvHistoryDateTime);
            tvSeat = itemView.findViewById(R.id.tvHistorySeat);
            tvPrice = itemView.findViewById(R.id.tvHistoryPrice);
        }
    }
}