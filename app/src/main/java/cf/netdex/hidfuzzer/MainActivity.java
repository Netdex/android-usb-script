package cf.netdex.hidfuzzer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import cf.netdex.hidfuzzer.task.PowershellTask;
import cf.netdex.hidfuzzer.task.DownloadTask;
import cf.netdex.hidfuzzer.task.WallpaperTask;
import cf.netdex.hidfuzzer.task.FuzzerTask;
import cf.netdex.hidfuzzer.task.HIDTask;
import cf.netdex.hidfuzzer.task.TestTask;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";
    private HIDTask RUNNING_TASK;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        final Spinner spnTask = (Spinner) findViewById(R.id.spnTask);

        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String stask = (String) spnTask.getSelectedItem();
                    switch(stask){
                        case "Fuzzer":
                            RUNNING_TASK = new FuzzerTask(MainActivity.this);
                            break;
                        case "Test":
                            RUNNING_TASK = new TestTask(MainActivity.this);
                            break;
                        case "Wallpaper":
                            RUNNING_TASK = new WallpaperTask(MainActivity.this);
                            break;
                        case "Download":
                            RUNNING_TASK = new DownloadTask(MainActivity.this);
                            break;
                        case "PowerShell":
                            RUNNING_TASK = new PowershellTask(MainActivity.this);
                            break;
                    }
                    RUNNING_TASK.execute();
                } else {
                    if (RUNNING_TASK != null)
                        RUNNING_TASK.cancel(false);
                }
            }
        });
    }
}
