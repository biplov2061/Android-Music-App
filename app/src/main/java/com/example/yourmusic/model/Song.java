package com.example.yourmusic.model;

import java.io.Serializable;

public class Song implements Serializable {

    private String title;
    private String artist;
    private String uri;


    public Song(String title, String artist, String uri){
        this.title = title;
        this.artist = artist;
        this.uri = uri;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public String getUri(){
        return uri;
    }
}


