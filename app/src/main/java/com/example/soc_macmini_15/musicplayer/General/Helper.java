package com.example.soc_macmini_15.musicplayer.General;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Helper {
    private static final ArrayList<String> MusicExtensions = new ArrayList<String>(Arrays.asList(".mp3", ".wma", ".wav", ".3gp", ".m4a"));

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void SortFileByName(ArrayList<File> files) {
        files.sort(new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<File> ListFolderAndSortByName(File file) {
        File[] files = file.listFiles();
        ArrayList<File> folders = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) folders.add(f);
        }
        SortFileByName(folders);
        return folders;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<File> ListMusicAndSortByName(File file) {
        File[] files = new File[]{};
        if (file.listFiles() != null)
            files = file.listFiles();
        ArrayList<File> musics = new ArrayList<>();
        for (File f : files) {
            if (f.isFile()) {
                String ex = f.getPath().substring(f.getPath().lastIndexOf("."));
                if (MusicExtensions.contains(ex)) musics.add(f);
            }
        }
        SortFileByName(musics);
        return musics;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<File> ListFolderAndMusic(File file) {
        if(file.exists()) {
            File[] files = file.listFiles();
            ArrayList<File> folders = new ArrayList<>();
            ArrayList<File> musics = new ArrayList<>();
            for (File f : files) {
                if (f.isDirectory()) folders.add(f);
                else if (f.isFile()) {
                    String ex = f.getPath().substring(f.getPath().lastIndexOf("."));
                    if (MusicExtensions.contains(ex)) musics.add(f);
                }
            }
            SortFileByName(folders);
            SortFileByName(musics);
            folders.addAll(musics);
            return folders;
        }
        return new ArrayList<File>();
    }
}
