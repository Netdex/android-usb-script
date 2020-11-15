package org.netdex.hidfuzzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SelectAssetActivity extends AppCompatActivity {

    public static final String SCRIPT_PATH = "scripts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_asset);

        getSupportActionBar().setTitle("Select Lua Asset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ArrayList<LuaAssetAdapter.LuaAsset> assets = new ArrayList<>();
        String[] scriptFilePaths;
        try {
            scriptFilePaths = getAssets().list(SCRIPT_PATH);
            for (String filePath : scriptFilePaths) {
                if (filePath.endsWith(".lua")) {
                    assets.add(new LuaAssetAdapter.LuaAsset(filePath, Paths.get(SCRIPT_PATH, filePath).toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LuaAssetAdapter adapter = new LuaAssetAdapter(assets) {
            @Override
            public void onLuaAssetSelected(LuaAsset asset) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name", asset.getName());
                returnIntent.putExtra("path", asset.getPath());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        };
        recyclerView.setAdapter(adapter);
    }
}