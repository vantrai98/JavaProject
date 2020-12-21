package com.example.soc_macmini_15.musicplayer.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FavoriteSongDAO {
    @Insert
    public void insert(FavoriteSong song);
    @Delete
    public void delete(FavoriteSong song);
    @Query("SELECT * FROM FavoriteSong")
    public List<FavoriteSong> getAll();
    @Query("SELECT * FROM FavoriteSong where path = :path")
    public FavoriteSong getFavoriteSongByPath(String path);
}
