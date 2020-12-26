package com.example.soc_macmini_15.musicplayer.Model;

public class SongModel {

    private String title;
    private String path;
    private boolean isFavorite;

    public SongModel(String title, String path, boolean isFavorite) {
        this.title = title;
        this.path = path;
        this.isFavorite = isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
