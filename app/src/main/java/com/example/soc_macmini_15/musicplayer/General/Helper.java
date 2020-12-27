package com.example.soc_macmini_15.musicplayer.General;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Helper {
    private static final ArrayList<String> MusicExtensions = new ArrayList<String>(Arrays.asList(".*\\.mp3$", ".*\\.wma$", ".*\\.wav$", ".*\\.3gp$", ".*\\.m4a$"));

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
            if (isMusic(f)) {
                musics.add(f);
            }
        }
        SortFileByName(musics);
        return musics;
    }

    public static boolean isMusic(File music) {
        if (music != null && music.isFile()) {
            String path = music.getAbsolutePath();
            for (String rex : MusicExtensions) {
                if (Pattern.matches(rex, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean haveFolderOrMusic(File f) {
        if (f != null && f.listFiles() != null) {
            for (File file : f.listFiles()) {
                if (file.isDirectory() || isMusic(file)) return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<File> ListFolderAndMusic(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            ArrayList<File> folders = new ArrayList<>();
            ArrayList<File> musics = new ArrayList<>();
            for (File f : files) {
                if (haveFolderOrMusic(f)) folders.add(f);
                else if (isMusic(f)) {
                    musics.add(f);
                }
            }
            SortFileByName(folders);
            SortFileByName(musics);
            folders.addAll(musics);
            return folders;
        }
        return new ArrayList<File>();
    }

    public static File getRemovableSD() {
        String storagePath = "/storage";
        File storage = new File(storagePath);
        if (storage.exists()) {
            File[] files = storage.listFiles();
            for (File file : files) {
                if (file.listFiles() != null) return file;
            }
        }
        return null;
    }

    public static boolean isMusicFolder(File file) {
        if (isMusic(file)) {
            return true;
        }
        if (file != null && file.listFiles() != null) {
            for (File f : file.listFiles()) {
                if (isMusicFolder(f)) return true;
            }
        }
        return false;
    }
}
