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
    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    private ArrayList<File> files;

    public void setRoot(File root) {
        this.root = root;
    }

    public void setSdRoot(File sdRoot) {
        this.sdRoot = sdRoot;
    }

    private File root;
    private File sdRoot;

    public DirectoryAdapter() {

    }

    public DirectoryAdapter(ArrayList<File> files) {
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
        if (view == null) {
            fileView = View.inflate(viewGroup.getContext(), R.layout.directory_item, null);
        } else fileView = view;

        File file = files.get(i);
        ImageView im_directory_thumb = (ImageView) fileView.findViewById(R.id.im_directory_thumb);
        if (file.isDirectory()) {
            im_directory_thumb.setImageResource(R.drawable.folder_regular_blue_icon);
        } else {
            im_directory_thumb.setImageResource(R.drawable.music_icon);
        }
        TextView tv_directory_item_name = (TextView) fileView.findViewById(R.id.tv_directory_item_name);
        if (root != null && file.getName().compareTo(root.getName()) == 0) {
            tv_directory_item_name.setText(R.string.phone_external_SD);
        } else if (sdRoot != null && file.getName().compareTo(sdRoot.getName()) == 0) {
            tv_directory_item_name.setText(R.string.removeble_external_SD);
        } else {
            tv_directory_item_name.setText(file.getName());
        }
        return fileView;
    }
}
