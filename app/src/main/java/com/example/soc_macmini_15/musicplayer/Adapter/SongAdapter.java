package com.example.soc_macmini_15.musicplayer.Adapter;

import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.soc_macmini_15.musicplayer.Model.SongModel;
import com.example.soc_macmini_15.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends BaseAdapter {

    private ArrayList<SongModel> songs;

    public SongAdapter(){

    }

    public SongAdapter(ArrayList<SongModel> songs){
        this.songs = songs;
    }

    public ArrayList<SongModel> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<SongModel> songs) {
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View songView;
        if(view==null){
            songView = View.inflate(viewGroup.getContext(),R.layout.playlist_item,null);
        }
        else songView = view;

        SongModel song = songs.get(i);

        ((ImageView)songView.findViewById(R.id.iv_song_thumb)).setImageResource(R.drawable.music_icon);
        ((TextView)songView.findViewById(R.id.tv_song_title)).setText(song.getTitle());
        ((TextView)songView.findViewById(R.id.tv_song_subtitle)).setText(song.getSubTitle());

        return songView;
    }
}
