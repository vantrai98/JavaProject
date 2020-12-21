package com.example.soc_macmini_15.musicplayer.Adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.soc_macmini_15.musicplayer.Model.SongModel;
import com.example.soc_macmini_15.musicplayer.R;

import java.io.File;
import java.util.ArrayList;

public class DirectoryAdapter extends BaseAdapter {
    private ArrayList<File> files;

    public DirectoryAdapter(){

    }

    public  DirectoryAdapter(ArrayList<File> files){
        this.files = files;
    }
    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int i) {
        return files.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View fileView;
        if(view==null){
            fileView = View.inflate(viewGroup.getContext(), R.layout.directory_item,null);
        }
        else fileView = view;

        File file = files.get(i);
        if(file.isDirectory()){
            ((ImageView)fileView.findViewById(R.id.im_directory_thumb)).setImageResource(R.drawable.folder_icon);
        }
        else {
            ((ImageView)fileView.findViewById(R.id.im_directory_thumb)).setImageResource(R.drawable.music_icon);
        }
        ((TextView)fileView.findViewById(R.id.tv_directory_item_name)).setText(file.getName());

        return fileView;
    }
}
