package fr.etien278.tablesexpress;

import android.os.Bundle;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(VoixPlugin.class);
        super.onCreate(savedInstanceState);
        // l'écran reste allumé tant que l'appli est ouverte
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
