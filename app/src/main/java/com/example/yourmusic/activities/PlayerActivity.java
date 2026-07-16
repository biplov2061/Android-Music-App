package com.example.yourmusic.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.yourmusic.R;
import com.example.yourmusic.model.Song;

public class PlayerActivity extends AppCompatActivity {
    TextView tvSongName , tvArtistName;

    MediaPlayer mediaPlayer;

    ImageButton btnPlayPause;

    ImageButton btnPrev;

    ImageButton btnNext;
    SeekBar seekBar;

    ArrayList<Song> songList;
    int currentIndex = 0;

    boolean isPlaying = false;

    Handler handler = new Handler();


    TextView tvCurrentDuration , tvDuration;


    ImageView imgAlbumArt;



    @SuppressLint({"WrongViewCast", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        //view components initialization
        tvSongName = findViewById(R.id.tvSongName);
        tvArtistName = findViewById(R.id.tvArtistName);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.sbMusicSeekBar);
        tvCurrentDuration = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        imgAlbumArt = findViewById(R.id.imgAlbumArt);

        String title = getIntent().getStringExtra("title");
        String artist = getIntent().getStringExtra("artist");

        tvSongName.setText(title);
        tvArtistName.setText(artist);

        String uriString = getIntent().getStringExtra("path");
        btnPlayPause = findViewById(R.id.btnPlayPause);
        songList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        currentIndex = getIntent().getIntExtra("index", 0);

        playMusic(uriString);

        btnPlayPause.setOnClickListener(v ->{
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    isPlaying = false;
                    btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
                }
                else{
                    mediaPlayer.start();
                    isPlaying = true;

                    btnPlayPause.setImageResource(R.drawable.baseline_pause_24);

                }
            }
        });


        btnNext.setOnClickListener(v ->{

            Log.d("PLAYER", "songList = " + songList);
            Log.d("PLAYER", "currentIndex = " + currentIndex);
            if (currentIndex < songList.size() - 1){
                currentIndex++;
            }else{
                currentIndex = 0;  //loop back to first song
            }
            try {
                playSong(currentIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        btnPrev.setOnClickListener(v ->{

            if (currentIndex > 0){
                currentIndex--;
            }else{
                currentIndex = songList.size() - 1;
            }
            try {
                playSong(currentIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        //allow user to drag seekbar

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //only seek if user is dragging
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                //pause updates while user drags
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 if(mediaPlayer != null){
                     mediaPlayer.seekTo(seekBar.getProgress());
                     handler.post(updateSeekBarRunnable);  //resume updates
                 }
            }
        });


    }

   private Runnable updateSeekBarRunnable = new Runnable() {
       @Override
       public void run() {
           if(mediaPlayer != null){
               seekBar.setProgress(mediaPlayer.getCurrentPosition());

               handler.postDelayed(this, 500);
           }
       }
   };



    //plays the song
    private void playSong(int index) throws IOException {

        //stop old callbacks before new song
        handler.removeCallbacks(updateSeekBarRunnable);

        if (mediaPlayer != null){
             mediaPlayer.release();
             mediaPlayer = null;
        }

        currentIndex = index;

        Song song = songList.get(index);

        loadAlbumArt(song.getUri());
        mediaPlayer = new MediaPlayer();


        try{
            mediaPlayer.setDataSource(this, Uri.parse(song.getUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();

            tvDuration.setText(formatTime(mediaPlayer.getDuration()));


            mediaPlayer.setOnCompletionListener(mp -> {
               if(currentIndex < songList.size() - 1){
                    currentIndex++;
               }else{
                   currentIndex = 0; //loop playlist
               }
                try {
                    playSong(currentIndex);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            seekBar.setMax(mediaPlayer.getDuration());
            handler.post(updateSeekBarRunnable);


            //updating song and artist name of current song playing
            tvSongName.setText(song.getTitle());
            tvArtistName.setText(song.getArtist());

            btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //load album art

    private void loadAlbumArt(String uriString) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try{
            retriever.setDataSource(this, Uri.parse(uriString));
            byte[] art = retriever.getEmbeddedPicture();

            if(art != null){
                imgAlbumArt.setImageResource(R.drawable.waves);

            }else{
                imgAlbumArt.setImageResource(R.drawable.waves);
            }
        }catch (Exception e){
            imgAlbumArt.setImageResource(R.drawable.waves);
        }finally {
                retriever.release();

        }
    }


    //time formatter for playback and song duration
    private String formatTime(int milliseconds){
        int minutes = milliseconds / 1000 / 60;
        int seconds = (milliseconds / 1000) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }


    private void playMusic(String uriString){

        Uri uri = Uri.parse(uriString);
        mediaPlayer = new MediaPlayer();

        try{
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }


   @Override
    protected void onDestroy(){
        super.onDestroy();

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
   }

    public Runnable getUpdateSeekBarRunnable() {
        return updateSeekBarRunnable;
    }

    public void setUpdateSeekBarRunnable(Runnable updateSeekBarRunnable) {
        this.updateSeekBarRunnable = updateSeekBarRunnable;
    }
}
