package com.example.yourmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yourmusic.R;
import com.example.yourmusic.model.Song;

import java.util.List;





public class songAdapter extends RecyclerView.Adapter<songAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    private OnSongClickListener listener;
    private List<Song> songList;

    public songAdapter(List<Song> songList, OnSongClickListener listener){
        this.songList = songList;
        this.listener = listener;

    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        holder.itemView.setOnClickListener(v ->{
             listener.onSongClick(song, position);
        });
    }

    @Override
    public int getItemCount(){
        return songList.size();
    }


    public static class SongViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
        }
    }




}
