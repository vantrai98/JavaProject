package com.example.soc_macmini_15.musicplayer.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soc_macmini_15.musicplayer.Adapter.SongAdapter;
import com.example.soc_macmini_15.musicplayer.BroadcastReceiver.AppReceiver;
import com.example.soc_macmini_15.musicplayer.Database.AppDatabase;
import com.example.soc_macmini_15.musicplayer.Database.FavoriteSong;
import com.example.soc_macmini_15.musicplayer.General.Constant;
import com.example.soc_macmini_15.musicplayer.General.Helper;
import com.example.soc_macmini_15.musicplayer.Model.SongModel;
import com.example.soc_macmini_15.musicplayer.R;
import com.example.soc_macmini_15.musicplayer.Service.KilledAppService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean firstOpen = true;
    private boolean playNow;

    private ImageButton imgBtnPlayPause, imgbtnShuffle, imgBtnPrev, imgBtnNext, imgBtnRepeat;
    private SeekBar seekbarController;
    private DrawerLayout mDrawerLayout;
    private TextView tvCurrentTime, tvTotalTime;
    private ListView lvListSong;

    private AppDatabase database;

    private ArrayList<SongModel> songs;
    private SongAdapter songAdapter;
    private String searchText = "";
    private SongModel currSong;
    private File curFolder;

    private File rootFolder;
    private File sdRoot;
    private int listType;

    SharedPreferences pref;

    private final int MY_PERMISSION_REQUEST = 100;
    private int repeatMode; //Chế độ lặp
    private boolean shuffleMode; //Chế độ phát

    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    Context context;

    private AppReceiver appReceiver = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        registerMyReceiver();
        if (firstOpen) {
            initCurFolder();
            initCurSong();
            initListMode();
            firstOpen = false;
        }
        initRepeatMode();
        initShuffleMode();
        grantedPermission();
        createNotificationChannel();
    }

    /**
     * Khởi tạo các view và các biến
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgbtnShuffle = findViewById(R.id.img_btn_shuffle);
        imgBtnRepeat = findViewById(R.id.img_btn_repeat);
        lvListSong = findViewById(R.id.lv_list_song);
        registerForContextMenu(lvListSong);
        lvListSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setCurrSong(songs.get(i), true);
            }
        });
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        seekbarController = findViewById(R.id.seekbar_controller);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imgBtnPlayPause = findViewById(R.id.img_btn_play);
        Toolbar toolbar = findViewById(R.id.mainToolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgbtnShuffle.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnRepeat.setOnClickListener(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_device:
                        setListMode(Constant.DEVICE);
                        break;
                    case R.id.nav_favorite:
                        setListMode(Constant.FAVORITE);
                        break;
                    case R.id.nav_about:
                        about();
                        break;
                }
                return true;
            }
        });

        seekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean flag = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                flag = b;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currSong == null) {
                    seekbarController.setProgress(0);
                } else if (flag) {
                    tvCurrentTime.setText(getTimeFormatted(seekBar.getProgress()));
                    mediaPlayer.seekTo(seekbarController.getProgress());
                    flag = false;
                }
            }
        });

        pref = getApplicationContext().getSharedPreferences(Constant.APP_PREF, 0);
        context = this;
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                switch (repeatMode) {
                    case Constant.NO_REPEAT:
                        setCurrSong(currSong, false);
                        break;
                    case Constant.REPEAT_ONE:
                        setCurrSong(currSong, true);
                        break;
                    case Constant.REPEAT_ALL:
                        SongModel f = getRandomSong();
                        if (f != null) {
                            setCurrSong(f, true);
                        } else {
                            setCurrSong(currSong, false);
                        }
                        break;
                }
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekbarController.setMax(mediaPlayer.getDuration());
                tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
                if (playNow) {
                    playMusic(true);
                }
            }
        });

        database = Room.databaseBuilder(this, AppDatabase.class, Constant.DATABASE_NAME).build();

        rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        Uri uri = Uri.parse("sdcard1/");
        sdRoot = new File(uri.getPath());
        songAdapter = new SongAdapter();
        repeatMode = Constant.NO_REPEAT;
        shuffleMode = false;
        playNow = false;
    }

    /**
     * Yêu câu các quyền cần thiết
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initListMode() {
        int listMode = pref.getInt(Constant.LIST_TYPE, Constant.DEVICE);
        setListMode(listMode);
    }

    /**
     * Get giá trị ban đầu cho list nhạc từ device hay từ favorite list
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initCurFolder() {
        String a = rootFolder.getAbsolutePath();
        String curFolderPath = pref.getString(Constant.CURRENT_FOLDER_PATH, rootFolder.getAbsolutePath());
        File file = new File(curFolderPath);
        if (file.exists())
            setCurFolder(file);
        else setCurFolder(rootFolder);
    }

    /**
     * Get bài nhạc đang đươc phát lần trước
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initCurSong() {
        String curMusicPath = pref.getString(Constant.CURRENT_MUSIC_PATH, null);
        if (curMusicPath != null) {
            try {
                File file = new File(curMusicPath);
                setCurrSong(new SongModel(file.getName().substring(0, file.getName().lastIndexOf('.')), file.getAbsolutePath(), false), false);
            } catch (Exception ex) {

            }
        }
    }

    /**
     * Get chế độ repeat
     */
    private void initRepeatMode() {
        int _repeatMode = pref.getInt(Constant.REPEAT_MODE, Constant.NO_REPEAT);
        setRepeatMode(_repeatMode, false);
    }

    /**
     * Get chế độ phát (ngẫu nhiên hay tuần tự)
     */
    private void initShuffleMode() {
        boolean _shuffleMode = pref.getBoolean(Constant.SHUFFLE_MODE, false);
        setShuffleMode(_shuffleMode, false);
    }

    /**
     * return media player
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setListMode(int listType) {
        this.listType = listType;
        pref.edit().putInt(Constant.LIST_TYPE, this.listType).commit();
        setTitle();
        if (this.listType == Constant.FAVORITE) {
            listSong();
        } else if (this.listType == Constant.DEVICE) {
            setCurFolder(curFolder);
        }
    }

    private void setTitle() {
        if (this.listType == Constant.FAVORITE) {
            setTitle(R.string.favorites);
        } else if (this.listType == Constant.DEVICE) {
            if (curFolder.getAbsolutePath().compareTo(rootFolder.getAbsolutePath()) == 0)
                setTitle(R.string.phone_external_SD);
            else if (curFolder.getAbsolutePath().compareTo(sdRoot.getAbsolutePath()) == 0)
                setTitle(R.string.removeble_external_SD);
            else if (curFolder == null)
                setTitle(R.string.storage);
            else setTitle(curFolder.getName());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listSong() {
        switch (this.listType) {
            case Constant.DEVICE:
                listMusicFromFolder();
                break;
            case Constant.FAVORITE:
                listMusicFromFavoriteList();
                break;
        }
    }

    /**
     * Yêu câu các quyền cần thiết
     */

    private void grantedPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, R.string.provide_storage_accesss_permission, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }
    }

    /**
     * Xử lí khi yêu cầu quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    }
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.BROWSE_AND_SELECT_FOLDER: {
                if (resultCode == Activity.RESULT_OK) {
                    searchText = "";
                    String filePath = data.getStringExtra(Constant.BROWSE_AND_SELECT_FOLDER_DATA);
                    File f = new File(filePath);
                    if (f.exists()) {
                        setListMode(Constant.DEVICE);
                        if (f.isDirectory()) {
                            setCurFolder(f);
                        } else {
                            setCurFolder(f.getParentFile());
                            for (SongModel s : songs) {
                                if (s.getPath().compareTo(f.getAbsolutePath()) == 0) {
                                    setCurrSong(s, true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setListView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                if (searchText.isEmpty()) {
                    setListView();
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.menu_folder:
                browseAndSelectFolder();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        if (this.listType == Constant.DEVICE) {
            menuInflater.inflate(R.menu.device_song_menu, menu);
        } else if (this.listType == Constant.FAVORITE) {
            menuInflater.inflate(R.menu.favorite_song_menu, menu);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SongModel songModel = songs.get(info.position);
        switch (item.getItemId()) {
            case R.id.menu_add_favorite:
                addFavoriteSong(new FavoriteSong(songModel.getPath()));
                break;
            case R.id.menu_remove_favorite:
                removeFavoriteSong(new FavoriteSong(songModel.getPath()));
                break;
            case R.id.menu_delete_song:
                onDeleteMusic(songModel.getPath());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void registerMyReceiver() {
        appReceiver = new AppReceiver();
        appReceiver.setMainActivity(this);
        registerReceiver(appReceiver, new IntentFilter(Constant.ACL_CONNECTED));
        registerReceiver(appReceiver, new IntentFilter(Constant.ACL_DISCONNECTED));
        registerReceiver(appReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(appReceiver, new IntentFilter(Constant.PREV));
        registerReceiver(appReceiver, new IntentFilter(Constant.PAUSE));
        registerReceiver(appReceiver, new IntentFilter(Constant.NEXT));
        registerReceiver(appReceiver, new IntentFilter(Constant.PLAY));
        registerReceiver(appReceiver, new IntentFilter(Constant.CLOSE));
    }

    private void unRegisterMyReceiver() {
        unregisterReceiver(appReceiver);
    }

    private void setCurrSong(SongModel songModel, boolean play) {
        playNow = play;
        currSong = songModel;
        ((TextView) findViewById(R.id.tv_song_name)).setText(currSong.getTitle());
        ((TextView) findViewById(R.id.tv_current_time)).setText(getTimeFormatted(0));
        saveCurMusic();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currSong.getPath());
            mediaPlayer.prepareAsync();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setCurFolder(File f) {
        curFolder = f;
        saveCurFolder();
        setTitle();
        listMusicFromFolder();
    }

    private void listMusicFromFavoriteList() {
        List<FavoriteSong> favoriteSongs = getFavoriteList();
        final ArrayList<SongModel> songs = new ArrayList<>();
        for (int i = 0; i < favoriteSongs.size(); i++) {
            File file = new File(favoriteSongs.get(i).path);
            songs.add(new SongModel(file.getName().substring(0, file.getName().lastIndexOf('.')), file.getAbsolutePath(), true));
        }
        setSongs(songs);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listMusicFromFolder() {
        ArrayList<File> songs = Helper.ListMusicAndSortByName(curFolder);
        final ArrayList<SongModel> songModels = new ArrayList<>();
        songs.forEach(new Consumer<File>() {
            @Override
            public void accept(File file) {
                songModels.add(new SongModel(file.getName().substring(0, file.getName().lastIndexOf('.')), file.getAbsolutePath(), false));
            }
        });
        setSongs(songModels);
    }

    private void setSongs(ArrayList<SongModel> songs) {
        this.songs = songs;
        setListView();
    }

    private void setListView() {
        ArrayList<SongModel> searchSongs = new ArrayList<SongModel>();
        if (searchText.trim().isEmpty()) searchSongs = songs;
        else {
            for (SongModel s : songs) {
                if (s.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                    searchSongs.add(s);
            }
        }
        SongAdapter adapter = new SongAdapter(searchSongs);
        lvListSong.setAdapter(adapter);
    }

    /**
     * Hiển thị thông tin ứng dụng nghe nhạc
     */
    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMusic(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            Toast.makeText(this, R.string.removed_device_list, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.remove_failed, Toast.LENGTH_SHORT).show();
        }
        listSong();
    }

    private void onDeleteMusic(final String path) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.removed_device_list_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMusic(path);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void browseAndSelectFolder() {
        Intent intent = new Intent(this, ChooseDirectoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.CURRENT_FOLDER_PATH, curFolder.getAbsolutePath());
        intent.putExtras(bundle);
        startActivityForResult(intent, Constant.BROWSE_AND_SELECT_FOLDER);
    }

    private void saveCurFolder() {
        pref.edit().putString(Constant.CURRENT_FOLDER_PATH, curFolder.getAbsolutePath()).commit();
    }

    private void saveCurMusic() {
        pref.edit().putString(Constant.CURRENT_MUSIC_PATH, currSong.getPath()).commit();
    }

    private void setRepeatMode(int mode, boolean notify) {
        repeatMode = mode;
        pref.edit().putInt(Constant.REPEAT_MODE, mode).commit();
        setRepeatView(notify);
    }

    private void setRepeatView(boolean notify) {
        switch (repeatMode) {
            case Constant.NO_REPEAT:
                imgBtnRepeat.setImageDrawable(getDrawable(R.drawable.no_repeat_icon));
                if (notify) Toast.makeText(this, R.string.no_repeat, Toast.LENGTH_SHORT).show();
                break;
            case Constant.REPEAT_ONE:
                imgBtnRepeat.setImageDrawable(getDrawable(R.drawable.repeat_one_icon));
                if (notify) Toast.makeText(this, R.string.repeat_one, Toast.LENGTH_SHORT).show();
                break;
            case Constant.REPEAT_ALL:
                imgBtnRepeat.setImageDrawable(getDrawable(R.drawable.repeat_icon));
                if (notify) Toast.makeText(this, R.string.repeat_all, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setShuffleMode(boolean mode, boolean notify) {
        shuffleMode = mode;
        pref.edit().putBoolean(Constant.SHUFFLE_MODE, mode).commit();
        setShuffleView(notify);
    }

    private void setShuffleView(boolean notify) {
        imgbtnShuffle.setImageDrawable(getDrawable(shuffleMode ? R.drawable.shuffle_icon : R.drawable.no_shuffle_icon));
        if (notify) {
            if (shuffleMode)
                Toast.makeText(this, R.string.shuffle, Toast.LENGTH_SHORT).show();
            else if (notify) Toast.makeText(this, R.string.no_shuffle, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeRepeatMode() {
        switch (repeatMode) {
            case Constant.NO_REPEAT:
                setRepeatMode(Constant.REPEAT_ONE, true);
                break;
            case Constant.REPEAT_ONE:
                setRepeatMode(Constant.REPEAT_ALL, true);
                break;
            case Constant.REPEAT_ALL:
                setRepeatMode(Constant.NO_REPEAT, true);
                break;
        }
    }

    private void changeShuffleMode() {
        setShuffleMode(!shuffleMode, true);
        setShuffleView(true);
    }

    /**
     * Tương tác database để lấy dữ kiệu bài hát yêu thích
     */

    private void addFavoriteSong(final FavoriteSong song) {
        final Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FavoriteSong old = database.FavoriteSongDAO().getFavoriteSongByPath(song.path);
                if (old == null) {
                    database.FavoriteSongDAO().insert(song);
                }
            }
        }).start();
        Toast.makeText(activity, R.string.added_favorite_list, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeFavoriteSong(final FavoriteSong song) {
        final Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FavoriteSong old = database.FavoriteSongDAO().getFavoriteSongByPath(song.path);
                if (old != null) {
                    database.FavoriteSongDAO().delete(old);
                }
            }
        }).start();
        Toast.makeText(activity, R.string.removed_favorite_list, Toast.LENGTH_SHORT).show();
        listSong();
    }

    private List<FavoriteSong> getFavoriteList() {
        final List<FavoriteSong> favoriteSongs = new ArrayList<>();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                favoriteSongs.addAll(database.FavoriteSongDAO().getAll());
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        try {
            thread.join();
            return favoriteSongs;
        } catch (Exception ex) {

        }
        return favoriteSongs;
    }

    /**
     * Xử lí các sự kiện touch/click trên player control
     *
     * @param v
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_btn_play:
                if (currSong == null) {
                    Toast.makeText(this, R.string.select_song, Toast.LENGTH_SHORT).show();
                } else if (mediaPlayer.isPlaying()) {
                    pauseMusic(true);
                } else {
                    playMusic(true);
                }
                break;
            case R.id.img_btn_shuffle:
                changeShuffleMode();
                break;
            case R.id.img_btn_previous:
                playPrevSong();
                break;
            case R.id.img_btn_next:
                playNextSong();
                break;
            case R.id.img_btn_repeat:
                changeRepeatMode();
                break;
        }
    }

    private SongModel getRandomSong() {
        Random random = new Random();
        if (songs.size() > 0) {
            int newIndex = random.nextInt(songs.size());
            return songs.get(newIndex);
        }
        Toast.makeText(this, R.string.empty_playlist, Toast.LENGTH_SHORT).show();
        return null;
    }

    private SongModel getNextSong() {
        if (songs.size() > 0) {
            int curIndex = songs.lastIndexOf(currSong);
            if (shuffleMode) {
                return getRandomSong();
            } else if (curIndex < songs.size() - 1) {
                return songs.get(curIndex + 1);
            } else {
                return songs.get(0);
            }
        }
        Toast.makeText(this, R.string.empty_playlist, Toast.LENGTH_SHORT).show();
        return null;
    }

    private SongModel getPrevSong() {
        if (songs.size() > 0) {
            int curIndex = songs.lastIndexOf(currSong);
            if (shuffleMode) {
                return getRandomSong();
            } else if (curIndex != 0) {
                return songs.get(curIndex - 1);
            } else {
                return songs.get(songs.size() - 1);
            }
        }
        Toast.makeText(this, R.string.empty_playlist, Toast.LENGTH_SHORT).show();
        return null;
    }

    public void playPrevSong() {
        SongModel songModel = getPrevSong();
        if (songModel != null) {
            setCurrSong(songModel, true);
        }
    }

    public void playNextSong() {
        SongModel songModel = getNextSong();
        if (songModel != null) {
            setCurrSong(songModel, true);
        }
    }

    /**
     * chơi 1 bài nhạc
     */

    public void playMusic(boolean showNotify) {
        try {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mediaPlayer.start();
            playCycle();
            if (showNotify) {
                createMediaPlayerController(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseMusic(boolean showNotify) {
        mediaPlayer.pause();
        if (showNotify) {
            createMediaPlayerController(false);
        }
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
    }

    public void closeNotification() {
        pauseMusic(false);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.cancel(Constant.NOTIFICATION_ID);
    }

    /**
     * Cập nhật trạng thái của bản nhạc
     */
    private void playCycle() {
        try {
            seekbarController.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tạo trình điều khiển trên thanh thông báo
     */
    private void createMediaPlayerController(final boolean isPlay) {
        final Activity mainActivity = this;
        Intent prevIntent = new Intent();
        prevIntent.setAction(Constant.PREV);
        Intent pauseIntent = new Intent();
        if (isPlay) {
            pauseIntent.setAction(Constant.PAUSE);
        } else {
            pauseIntent.setAction(Constant.PLAY);
        }
        Intent nextIntent = new Intent();
        nextIntent.setAction(Constant.NEXT);
        Intent closeIntent = new Intent();
        closeIntent.setAction(Constant.CLOSE);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(mainActivity, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(mainActivity, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(mainActivity, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(mainActivity, 1, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CONTTROLLER_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.previous_icon, "Previous", prevPendingIntent)
                .addAction(isPlay ? R.drawable.pause_icon : R.drawable.play_icon, isPlay ? "Pause" : "Play", pausePendingIntent)
                .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_close, "Close", closePendingIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2 /* #1: pause button */))
                .setContentTitle(currSong.getTitle())
                .setProgress(0, 100, true)
                .setOngoing(true)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(mainActivity);
        managerCompat.notify(Constant.NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constant.NOTIFICATION_CONTTROLLER_ID, Constant.NOTIFICATION_CONTTROLLER_ID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(Constant.NOTIFICATION_CONTTROLLER_ID);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
        unRegisterMyReceiver();
        closeNotification();
    }
}
