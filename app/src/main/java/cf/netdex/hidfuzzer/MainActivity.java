package cf.netdex.hidfuzzer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.task.FuzzerTask;
import cf.netdex.hidfuzzer.task.HIDTask;
import cf.netdex.hidfuzzer.task.TestTask;
import cf.netdex.hidfuzzer.util.Func;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";
    private HIDTask RUNNING_TASK;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        final Spinner spnTask = (Spinner) findViewById(R.id.spnTask);

        final Func<HIDTask.RunState> updatef = new Func<HIDTask.RunState>() {
            @Override
            public void run(HIDTask.RunState... s) {
                txtStatus.setText(s[0].name());
            }
        };
        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String stask = (String) spnTask.getSelectedItem();
                    switch(stask){
                        case "Fuzzer":
                            RUNNING_TASK = new FuzzerTask(MainActivity.this, updatef);
                            break;
                        case "Test":
                            RUNNING_TASK = new TestTask(MainActivity.this, updatef);
                            break;
                    }
                    RUNNING_TASK.execute();
                } else {
                    if (RUNNING_TASK != null)
                        RUNNING_TASK.cancel(true);
                }
            }
        });
    }
}
