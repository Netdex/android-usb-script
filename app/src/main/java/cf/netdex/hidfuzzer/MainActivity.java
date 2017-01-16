package cf.netdex.hidfuzzer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "tag_hidfuzzer";
    private FuzzerTask RUNNING_TASK;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
        final ToggleButton btnPoll = (ToggleButton) findViewById(R.id.btnPoll);
        btnPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RUNNING_TASK = new FuzzerTask(MainActivity.this) {
                        @Override
                        protected void onProgressUpdate(FuzzState... s) {
                            super.onProgressUpdate(s);
                            txtStatus.setText(s[0].name());
                        }
                    };
                    RUNNING_TASK.execute();
                } else {
                    if (RUNNING_TASK != null)
                        RUNNING_TASK.cancel(true);
                }
            }
        });
    }
}
