package com.example.miniproject1004;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.miniproject1004.adapters.SeatAdapter;
import com.example.miniproject1004.models.Movie;
import com.example.miniproject1004.models.Seat;
import com.example.miniproject1004.models.Showtime;
import com.example.miniproject1004.models.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvRating, tvDescription;
    private Spinner spinnerShowtimes;
    private RecyclerView rvSeats;
    private Button btnBook;
    private Movie movie;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private List<Showtime> showtimeList = new ArrayList<>();
    private List<String> showtimeDisplayList = new ArrayList<>();
    private Map<String, String> theaterNames = new HashMap<>();
    
    private List<Seat> seatList = new ArrayList<>();
    private SeatAdapter seatAdapter;
    private List<String> selectedSeats = new ArrayList<>();
    private Showtime currentShowtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ivPoster = findViewById(R.id.ivMovieDetailPoster);
        tvTitle = findViewById(R.id.tvMovieDetailTitle);
        tvRating = findViewById(R.id.tvMovieDetailRating);
        tvDescription = findViewById(R.id.tvMovieDetailDescription);
        spinnerShowtimes = findViewById(R.id.spinnerShowtimes);
        rvSeats = findViewById(R.id.rvSeats);
        btnBook = findViewById(R.id.btnBookTicket);

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        movie = (Movie) getIntent().getSerializableExtra("movie");

        if (movie != null) {
            tvTitle.setText(movie.getTitle());
            tvRating.setText(String.valueOf(movie.getRating()));
            tvDescription.setText(movie.getDescription());
            
            if (movie.getImageUrl() != null && movie.getImageUrl().startsWith("http")) {
                Glide.with(this).load(movie.getImageUrl()).into(ivPoster);
            }

            loadTheatersAndShowtimes();
        }

        btnBook.setOnClickListener(v -> bookTicket());
        
        spinnerShowtimes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < showtimeList.size()) {
                    currentShowtime = showtimeList.get(position);
                    updateSeatsDisplay(currentShowtime.getBookedSeats());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateSeatsDisplay(List<String> bookedSeats) {
        seatList.clear();
        selectedSeats.clear();
        
        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            for (int i = 1; i <= 6; i++) {
                String seatId = row + i;
                boolean isBooked = bookedSeats != null && bookedSeats.contains(seatId);
                seatList.add(new Seat(seatId, isBooked));
            }
        }

        seatAdapter = new SeatAdapter(seatList, seat -> {
            if (seat.isSelected()) {
                selectedSeats.add(seat.getId());
            } else {
                selectedSeats.remove(seat.getId());
            }
            updateBookingButton();
        });

        rvSeats.setLayoutManager(new GridLayoutManager(this, 6));
        rvSeats.setAdapter(seatAdapter);
        updateBookingButton();
    }

    private void updateBookingButton() {
        if (selectedSeats.isEmpty()) {
            btnBook.setText("Select Seats");
            btnBook.setEnabled(false);
        } else {
            btnBook.setText("Book " + selectedSeats.size() + " Seats");
            btnBook.setEnabled(true);
        }
    }

    private void loadTheatersAndShowtimes() {
        db.collection("theaters").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                theaterNames.put(doc.getId(), doc.getString("name"));
            }
            fetchShowtimes();
        });
    }

    private void fetchShowtimes() {
        db.collection("showtimes")
                .whereEqualTo("movieId", movie.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showtimeList.clear();
                    showtimeDisplayList.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Showtime st = doc.toObject(Showtime.class);
                        if (st != null) {
                            st.setId(doc.getId());
                            showtimeList.add(st);
                            String theaterName = theaterNames.get(st.getTheaterId());
                            showtimeDisplayList.add(theaterName + " - " + sdf.format(st.getTime()));
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, showtimeDisplayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerShowtimes.setAdapter(adapter);
                    
                    if (!showtimeList.isEmpty()) {
                        currentShowtime = showtimeList.get(0);
                        updateSeatsDisplay(currentShowtime.getBookedSeats());
                    }
                });
    }

    private void bookTicket() {
        if (mAuth.getCurrentUser() == null || currentShowtime == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        
        for (String seatId : selectedSeats) {
            String ticketId = UUID.randomUUID().toString();
            Ticket ticket = new Ticket(
                    ticketId,
                    userId,
                    currentShowtime.getId(),
                    movie.getTitle(),
                    theaterNames.get(currentShowtime.getTheaterId()),
                    currentShowtime.getTime(),
                    seatId,
                    currentShowtime.getPrice()
            );
            db.collection("tickets").document(ticketId).set(ticket);
        }

        db.collection("showtimes").document(currentShowtime.getId())
                .update("bookedSeats", FieldValue.arrayUnion(selectedSeats.toArray()))
                .addOnSuccessListener(aVoid -> {
                    scheduleNotification(movie.getTitle(), theaterNames.get(currentShowtime.getTheaterId()), currentShowtime.getTime().getTime());
                    Toast.makeText(this, "Booking Successful! Reminder set.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void scheduleNotification(String movieTitle, String theaterName, long showtimeMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("theaterName", theaterName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Nhắc trước 15 phút (900,000 ms)
        long triggerTime = showtimeMillis - 900000;
        
        // Nếu thời gian nhắc đã qua, nhắc ngay lập tức (cho mục đích test)
        if (triggerTime < System.currentTimeMillis()) {
            triggerTime = System.currentTimeMillis() + 5000; // 5 giây sau
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}