package com.example.soc_macmini_15.musicplayer.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soc_macmini_15.musicplayer.Adapter.DirectoryAdapter;
import com.example.soc_macmini_15.musicplayer.General.Constant;
import com.example.soc_macmini_15.musicplayer.General.Helper;
import com.example.soc_macmini_15.musicplayer.R;

import java.io.File;
import java.util.ArrayList;

public class ChooseDirectoryActivity extends AppCompatActivity {

    private ListView lvDirectotyList;
    private Toolbar toolbar;
    private ActionBar actionBar;

    private File root;
    private File sdRoot;
    private File curFolder;
    private ArrayList<File> fileList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_directory_layout);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        toolbar = findViewById(R.id.choose_directory_toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow);

        lvDirectotyList = findViewById(R.id.lv_directory_list);
        lvDirectotyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File fileSelected = fileList.get(i);
                Toast.makeText(ChooseDirectoryActivity.this, fileSelected.getName(), Toast.LENGTH_LONG);
                if (fileSelected.isDirectory()) {
                    setCurrentFolder(fileSelected);
                } else {
                    passedBackFolder(fileSelected);
                }
            }
        });
        fileList = new ArrayList<>();
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        Uri uri = Uri.parse("/sdcard");
        sdRoot = new File(uri.getPath());
        String _curFolderPath = getIntent().getExtras().getString(Constant.CURRENT_FOLDER_PATH);
        setCurrentFolder(new File(_curFolderPath));
    }

    private void passedBackFolder(File f) {
        Intent intent = new Intent();
        intent.putExtra(Constant.BROWSE_AND_SELECT_FOLDER_DATA, f.getAbsolutePath());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_directory_action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_folder_select:
                passedBackFolder(curFolder);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setCurrentFolder(File f) {
        curFolder = f;
        if (curFolder.exists()) {
            if (curFolder.getAbsolutePath().compareTo(root.getAbsolutePath()) == 0)
                actionBar.setTitle("Bộ nhớ điện thoại");
            else if (curFolder.getAbsolutePath().compareTo(sdRoot.getAbsolutePath()) == 0)
                actionBar.setTitle("Thẻ SDCard");
            else actionBar.setTitle(curFolder.getName());
            listDir();
        } else {
            fileList.clear();
            if (root.exists())
                fileList.add(root);
            if (sdRoot.exists())
                fileList.add(sdRoot);
            actionBar.setTitle("Lưu trữ");
            setListView();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void listDir() {
        fileList = Helper.ListFolderAndMusic(curFolder);
        setListView();
    }

    private void setListView() {
        DirectoryAdapter adapter = new DirectoryAdapter(fileList);
        lvDirectotyList.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBackPressed() {
        if (!curFolder.exists()) {
            finish();
        } else if (curFolder.getAbsolutePath().compareTo(root.getAbsolutePath()) == 0 || curFolder.getAbsolutePath().compareTo(sdRoot.getAbsolutePath()) == 0) {
            setCurrentFolder(new File(""));
        } else {
            setCurrentFolder(curFolder.getParentFile());
        }
    }
}
