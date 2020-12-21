package com.example.soc_macmini_15.musicplayer.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {FavoriteSong.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteSongDAO FavoriteSongDAO();
}
