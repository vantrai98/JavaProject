package com.example.soc_macmini_15.musicplayer.Database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "FavoriteSong")
public class FavoriteSong {
    public FavoriteSong(String path){
        this.path = path;
    }
    @PrimaryKey
    @NonNull
    public String path;
}
