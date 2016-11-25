package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();

    }
}
