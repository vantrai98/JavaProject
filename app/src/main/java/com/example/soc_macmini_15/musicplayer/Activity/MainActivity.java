package com.example.soc_macmini_15.musicplayer.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean firstOpen = true;

    private Menu menu;

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

    SharedPreferences pref;

    private final int MY_PERMISSION_REQUEST = 100;
    private int repeatMode; //Chế độ lặp
    private boolean shuffleMode; //Chế độ phát

    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;

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
            firstOpen = false;
        }
        initRepeatMode();
        initShuffleMode();
        grantedPermission();
    }

    /**
     * Khởi tạo các view
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgbtnShuffle = findViewById(R.id.img_btn_shuffle);
        imgBtnRepeat = findViewById(R.id.img_btn_repeat);
        lvListSong = findViewById(R.id.lv_list_song);
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
        Toolbar toolbar = findViewById(R.id.toolbar);

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
                        listMusicFromFolder();
                        break;
                    case R.id.nav_online:
                        listMusicFromFavoriteList();
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
                        setCurrSong(getRandomSong(), true);
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
                    playMusic();
                }
            }
        });

        rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        Uri uri = Uri.parse("sdcard/");
        sdRoot = new File(uri.getPath());
        songAdapter = new SongAdapter();
        repeatMode = Constant.NO_REPEAT;
        shuffleMode = false;
        playNow = false;
    }

    private void registerMyReceiver() {
        appReceiver = new AppReceiver();
        appReceiver.setMainActivity(this);
        registerReceiver(appReceiver, new IntentFilter(Constant.ACL_CONNECTED));
        registerReceiver(appReceiver, new IntentFilter(Constant.ACL_DISCONNECTED));
        registerReceiver(appReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    private void unRegisterMyReceiver() {
        unregisterReceiver(appReceiver);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initCurFolder() {
        String curFolderPath = pref.getString(Constant.CURRENT_FOLDER_PATH, null);
        File file = new File(curFolderPath);
        if (file.exists())
            setCurFolder(file);
        else setCurFolder(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initCurSong() {
        String curMusicPath = pref.getString(Constant.CURRENT_MUSIC_PATH, null);
        if (curMusicPath != null) {
            try {
                File file = new File(curMusicPath);
                setCurrSong(new SongModel(file.getName(), "", file.getAbsolutePath()), false);
            } catch (Exception ex) {

            }
        }
    }

    private void initRepeatMode() {
        int _repeatMode = pref.getInt(Constant.REPEAT_MODE, Constant.NO_REPEAT);
        setRepeatMode(_repeatMode, false);
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

    private void initShuffleMode() {
        boolean _shuffleMode = pref.getBoolean(Constant.SHUFFLE_MODE, false);
        setShuffleMode(_shuffleMode, false);
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

    private void saveFavoriteSong(final FavoriteSong song) {
        final Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FavoriteSong old = database.FavoriteSongDAO().getFavoriteSongByPath(song.path);
                if (old != null) {
                    Toast.makeText(activity, "Bài hát này đã được thêm vào danh sách yêu thích.", Toast.LENGTH_SHORT).show();
                } else {
                    database.FavoriteSongDAO().insert(song);
                }
            }
        });
    }

    private void removeFavoriteSong(final FavoriteSong song) {
        final Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FavoriteSong old = database.FavoriteSongDAO().getFavoriteSongByPath(song.path);
                if (old != null) {
                    database.FavoriteSongDAO().delete(old);
                } else {
                    Toast.makeText(activity, "Bài hát không có trong danh mục yêu thích", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<FavoriteSong> getFavoriteList() {
        final List<FavoriteSong> favoriteSongs = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                favoriteSongs.addAll(0, database.FavoriteSongDAO().getAll());
            }
        });
        return favoriteSongs;
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
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
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
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    }
                }
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
                    if (f.isDirectory()) setCurFolder(f);
                    else {
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
        if (curFolder.getAbsolutePath().compareTo(rootFolder.getAbsolutePath()) == 0)
            setTitle("Bộ nhớ điện thoại");
        else if (curFolder.getAbsolutePath().compareTo(sdRoot.getAbsolutePath()) == 0)
            setTitle("Thẻ SDCard");
        else setTitle(curFolder.getName());
        listMusicFromFolder();
    }

    private void listMusicFromFavoriteList() {
        List<FavoriteSong> favoriteSongs = getFavoriteList();
        final ArrayList<SongModel> songs = new ArrayList<>();
        for (int i = 0; i < favoriteSongs.size(); i++) {
            File file = new File(favoriteSongs.get(i).path);
            songs.add(new SongModel(file.getName(), "", file.getAbsolutePath()));
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
                songModels.add(new SongModel(file.getName(), "", file.getAbsolutePath()));
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
     * Function to show the dialog for about us.
     */
    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
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
            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_option:
                browseAndSelectFolder();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    Toast.makeText(this, "Chọn bài hát ...", Toast.LENGTH_SHORT).show();
                } else if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                } else {
                    playMusic();
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
        Toast.makeText()
        return null;
    }

    private void playPrevSong() {
        SongModel songModel = getPrevSong();
        if (songModel != null) {
            setCurrSong(songModel, true);
        }
    }

    private void playNextSong() {
        SongModel songModel = getNextSong();
        if (songModel != null) {
            setCurrSong(songModel, true);
        }
    }

    /**
     * chơi 1 bài nhạc
     */

    public void playMusic() {
        try {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mediaPlayer.start();
            playCycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseMusic() {
        mediaPlayer.pause();
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
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
    }
}
