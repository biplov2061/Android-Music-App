package com.example.yourmusic.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yourmusic.R;
import com.example.yourmusic.adapter.songAdapter;
import com.example.yourmusic.model.Song;

import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;

    RecyclerView recyclerView;
    songAdapter adapter;


    SearchView searchView;

    ImageButton ibSearchClear;

    TextView greetingView ;

    ArrayList<Song> allSongs = new ArrayList<>(); // holds complete library
    ArrayList<Song> filteredSongs = new ArrayList<>(); //holds what recycler view display


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rvSongs);
        Log.d("MusicApp", "checkPermission called");
        checkPermission();


        searchView = findViewById(R.id.svSongSearch);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String newText) {
                filteredSongs(newText);
                return true;
            }
        });

        greetingView = findViewById(R.id.tvGreeting);
        int CurrentHour;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int hour = LocalTime.now().getHour();

            String greeting;
            if (hour < 12){
                greeting = "Good Morning 🌅";
            } else if ( hour < 17) {
                greeting = "Good Afternoon ☀️";
            } else if ( hour < 21) {
                greeting = "Good Evening 🌇";
            } else{
                greeting = "Good Night 🌃";

            }
            greetingView.setText(greeting);
        }




    }



    //song filtering

    private void filteredSongs(String text){
        Log.d("SEARCH", "Searching: " + text);
        filteredSongs.clear();

        if(text.trim().isEmpty()){
            filteredSongs.addAll(allSongs);
        }else{
            for (Song song : allSongs){
                Log.d("SEARCH", "Title = " + song.getTitle());
                Log.d("SEARCH", "Query = " + text);
                if (song.getTitle().toLowerCase().contains(text.toLowerCase())
                || song.getArtist().toLowerCase().contains(text.toLowerCase())){
                    filteredSongs.add(song);
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d("SEARCH", "Filtered size = " + filteredSongs.size());
    }







    //check if app has the storage permission or not
    private void checkPermission() {

        String permission;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        Log.d("MusicApp", "Using permission: " + permission);

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("MusicApp", "Requesting permission dialog...");

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    STORAGE_PERMISSION_CODE
            );

        } else {
            Log.d("MusicApp", "Permission already granted → loading songs");
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                loadSongs();

            }
        }
    }

    private void loadSongs() {


        Log.d("MusicApp", "loadSongs() called");

        allSongs = new ArrayList<>();

        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };

        try (android.database.Cursor cursor = getContentResolver().query(
                collection,
                projection,
                null,
                null,
                null)) {

            if (cursor != null) {

                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

                while (cursor.moveToNext()) {

                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);

                    Long id = cursor.getLong(idIndex);

                    Uri songUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                    );

                    allSongs.add(new Song(title, artist, songUri.toString()));

                    filteredSongs.clear();
                    filteredSongs.addAll(allSongs);
                }
            }
        }

        adapter = new songAdapter(filteredSongs,(song, position) -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("title", song.getTitle());
            intent.putExtra("artist", song.getArtist());
            intent.putExtra("path", song.getUri());
            intent.putExtra("index", position);
            intent.putExtra("songList", filteredSongs);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}