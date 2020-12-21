package com.example.soc_macmini_15.musicplayer.Database;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;

import com.example.soc_macmini_15.musicplayer.Activity.MainActivity;

import java.util.List;

public class AgentAsyncTask extends AsyncTask<Void, Void, List<FavoriteSong>> {
    public AgentAsyncTask(Activity context){
        this.activity = context;
    }
    private Activity activity;

    @Override
    protected List<FavoriteSong> doInBackground(Void... voids) {
        AppDatabase database = Room.databaseBuilder(activity,AppDatabase.class,"mysql").build();
        database.FavoriteSongDAO().insert(new FavoriteSong("hello"));
        List<FavoriteSong> a = database.FavoriteSongDAO().getAll();
        return a;
    }

    @Override
    protected void onPostExecute(List<FavoriteSong> favoriteSongs) {
        super.onPostExecute(favoriteSongs);
    }

}
