package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedQRContent;

public class ShowKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_key);

        String info = getIntent().getStringExtra("info");

        ParsedQRContent parsed = QRContentParser.parse(info);

        TextView name = (TextView) findViewById(R.id.textView_showKey_name);
        TextView key = (TextView) findViewById(R.id.textView_showKey_key);
        TextView id = (TextView) findViewById(R.id.textView_showKey_id);

        name.setText("Name: " + parsed.name);
        key.setText("Key: " + parsed.key);
        id.setText("ID: " + parsed.id.toString());
    }
}
