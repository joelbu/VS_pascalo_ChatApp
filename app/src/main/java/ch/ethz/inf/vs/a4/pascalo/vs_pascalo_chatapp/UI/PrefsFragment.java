package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;


public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}