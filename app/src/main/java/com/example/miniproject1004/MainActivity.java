package com.example.miniproject1004;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject1004.adapters.MovieAdapter;
import com.example.miniproject1004.models.Movie;
import com.example.miniproject1004.models.Theater;
import com.example.miniproject1004.models.Showtime;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cinema Discovery");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        rvMovies = findViewById(R.id.rvMovies);
        movieList = new ArrayList<>();
        
        adapter = new MovieAdapter(movieList, movie -> {
            Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie", movie);
            startActivity(intent);
        });

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        fetchMovies();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        
        // Cần cho Alarm Manager trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void fetchMovies() {
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        if (task.getResult().isEmpty()) {
                            seedData();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Movie movie = document.toObject(Movie.class);
                                movie.setId(document.getId());
                                movieList.add(movie);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void seedData() {
        List<Task<Void>> tasks = new ArrayList<>();

        // Sử dụng link ảnh thật từ TMDB
        Movie m1 = new Movie("1", "Avatar: The Way of Water", "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora.", "https://image.tmdb.org/t/p/w500/t6SnaX0STzXfsS3JkvJlgOUv1qI.jpg", 8.5, "192 min");
        Movie m2 = new Movie("2", "John Wick: Chapter 4", "John Wick uncovers a path to defeating The High Table.", "https://image.tmdb.org/t/p/w500/gh2znuSTbtpuzp84fk19o40C08u.jpg", 9.0, "169 min");
        Movie m3 = new Movie("3", "Oppenheimer", "The story of J. Robert Oppenheimer and his role in the atomic bomb.", "https://image.tmdb.org/t/p/w500/8Gxv2mYgiFAao4XoZasAw3QG4Wg.jpg", 8.9, "180 min");
        
        tasks.add(db.collection("movies").document("1").set(m1));
        tasks.add(db.collection("movies").document("2").set(m2));
        tasks.add(db.collection("movies").document("3").set(m3));

        Theater t1 = new Theater("t1", "CGV Vincom Center", "Lê Thánh Tôn, Q1");
        tasks.add(db.collection("theaters").document("t1").set(t1));

        // SUẤT CHIẾU TEST: 11:00 AM ngày 10/04/2026
        Calendar testCal = Calendar.getInstance();
        testCal.set(2026, Calendar.APRIL, 10, 11, 0, 0);
        Showtime testST = new Showtime("test_showtime", "1", "t1", testCal.getTime(), 150000.0);
        tasks.add(db.collection("showtimes").document("test_showtime").set(testST));

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> fetchMovies());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(MainActivity.this, TicketHistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}