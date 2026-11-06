package fr.etien278.tablesexpress;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(VoixPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
