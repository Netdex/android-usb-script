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

import cf.netdex.hidfuzzer.task.DownloadRunTask;
import cf.netdex.hidfuzzer.task.LtdPowerShellTask;
import cf.netdex.hidfuzzer.task.PowershellTask;
import cf.netdex.hidfuzzer.task.SerialTransferDebugTask;
import cf.netdex.hidfuzzer.task.WallpaperTask;
import cf.netdex.hidfuzzer.task.FuzzerTask;
import cf.netdex.hidfuzzer.task.HIDTask;
import cf.netdex.hidfuzzer.task.TestTask;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";

    /* initialize class map to dynamically load tasks */
    static final Class[] fTasks = {
            TestTask.class,
            SerialTransferDebugTask.class,
            WallpaperTask.class,
            DownloadRunTask.class,
            PowershellTask.class,
            LtdPowerShellTask.class,
            FuzzerTask.class
    };

    static final HashMap<String, Class> fTaskMap = new HashMap<>();                                     // I only made this map because I'm too lazy to make a custom list adapter
    static final ArrayList<String> fTaskSpinnerItems = new ArrayList<>();

    static {                                                                                            // static blocks aren't that bad... okay?
        for (Class c : fTasks) {
            fTaskMap.put(c.getSimpleName(), c);
            fTaskSpinnerItems.add(c.getSimpleName());
        }
    }

    /* end class map initialization */

    private HIDTask mRunningTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        final Spinner spnTask = (Spinner) findViewById(R.id.spnTask);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, fTaskSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTask.setAdapter(adapter);

        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String stask = (String) spnTask.getSelectedItem();
                    Class c = fTaskMap.get(stask);
                    try {

                        mRunningTask = (HIDTask) c.getDeclaredConstructor(Context.class).newInstance(MainActivity.this);
                        mRunningTask.execute();
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
                    if (mRunningTask != null)
                        mRunningTask.cancel(false);
                }
            }
        });
    }
}
