package com.mytlogos.enterprise;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mytlogos.enterprise.ui.AddMedium;

public class AddMediumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_medium_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AddMedium.newInstance())
                    .commitNow();
        }
    }
}
