package cf.netdex.hidfuzzer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import cf.netdex.hidfuzzer.ltask.HIDTask;
import cf.netdex.hidfuzzer.ltask.LuaTaskLoader;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hidfuzzer";
    public static final String SCRIPT_PATH = "scripts";

    // I only made this map because I'm too lazy to make a custom list adapter
    static final HashMap<String, String> fTaskMap = new HashMap<>();
    static final ArrayList<String> fTaskSpinnerItems = new ArrayList<>();

    private HIDTask mRunningTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load scripts
        String[] scriptFilePaths;
        try {
            scriptFilePaths = getAssets().list(SCRIPT_PATH);

            for (String filePath : scriptFilePaths) {
                if (filePath.endsWith(".lua")) {
                    fTaskMap.put(filePath, SCRIPT_PATH + File.separator + filePath);
                    fTaskSpinnerItems.add(filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // sort out the UI
        final ToggleButton btnPoll = findViewById(R.id.btnPoll);
        final Spinner spnTask = findViewById(R.id.spnTask);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, fTaskSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTask.setAdapter(adapter);

        btnPoll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String selectedTask = (String) spnTask.getSelectedItem();
                String taskSrcFilePath = fTaskMap.get(selectedTask);
                mRunningTask = LuaTaskLoader.createTaskFromLuaFile(this, selectedTask, taskSrcFilePath);
                mRunningTask.execute();
            } else {
                if (mRunningTask != null) {
                    btnPoll.setEnabled(false);
                    mRunningTask.cancel(false);
                }
            }
        });
    }


}
