package cf.netdex.hidfuzzer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";
    private FuzzerTask RUNNING_TASK;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    RUNNING_TASK = new FuzzerTask(MainActivity.this);
                    RUNNING_TASK.execute();
                }
                else{
                    RUNNING_TASK.cancel(true);
                }
            }
        });
    }
}
