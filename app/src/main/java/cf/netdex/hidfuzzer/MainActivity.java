package cf.netdex.hidfuzzer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import cf.netdex.hidfuzzer.task.PowershellTask;
import cf.netdex.hidfuzzer.task.DownloadTask;
import cf.netdex.hidfuzzer.task.WallpaperTask;
import cf.netdex.hidfuzzer.task.FuzzerTask;
import cf.netdex.hidfuzzer.task.HIDTask;
import cf.netdex.hidfuzzer.task.TestTask;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";
    private HIDTask RUNNING_TASK;

    static final Class[] TASKS = {
            TestTask.class,
            WallpaperTask.class,
            DownloadTask.class,
            PowershellTask.class,
            FuzzerTask.class
    };

    static final HashMap<String, Class> mTaskMap = new HashMap<>();
    static final ArrayList<String> mTaskSpinnerItems = new ArrayList<>();
    static {
        for (Class c : TASKS) {
            mTaskMap.put(c.getName(), c);
            mTaskSpinnerItems.add(c.getName());
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        final Spinner spnTask = (Spinner) findViewById(R.id.spnTask);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, mTaskSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTask.setAdapter(adapter);

        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String stask = (String) spnTask.getSelectedItem();
                    Class c = mTaskMap.get(stask);
                    try {

                        RUNNING_TASK = (HIDTask) c.getDeclaredConstructor(Context.class).newInstance(MainActivity.this);
                        RUNNING_TASK.execute();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (RUNNING_TASK != null)
                        RUNNING_TASK.cancel(false);
                }
            }
        });
    }
}
