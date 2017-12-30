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


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "tag_hidfuzzer";
    public static final String SCRIPT_PATH = "scripts";

    // I only made this map because I'm too lazy to make a custom list adapter
    static final HashMap<String, String> fTaskMap = new HashMap<>();
    static final ArrayList<String> fTaskSpinnerItems = new ArrayList<>();

    private HIDTask mRunningTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load scripts
        String[] scriptFilePaths = null;
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
        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        final Spinner spnTask = (Spinner) findViewById(R.id.spnTask);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, fTaskSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTask.setAdapter(adapter);

        btnPoll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String selectedTask = (String) spnTask.getSelectedItem();
                String taskSrcFilePath = fTaskMap.get(selectedTask);
                mRunningTask = createTaskFromLuaFile(selectedTask, taskSrcFilePath);
                mRunningTask.execute();
            } else {
                if (mRunningTask != null)
                    mRunningTask.cancel(false);
            }
        });
    }

    public HIDTask createTaskFromLuaFile(String name, String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(path)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            br.close();
            String src = sb.toString();
            HIDTask task = new HIDTask(this, name, src);
            return task;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
