package com.artrom.flychat.internet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.artrom.flychat.R;

public class CreateChatActivity extends AppCompatActivity {

    TextView createchat_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }

        createchat_title = findViewById(R.id.createchat_title);
        createchat_title.setText("Список запросов");
    }

    /*View.OnClickListener radioButtonClickListener = v -> {
        RadioButton rb = (RadioButton) v;
        switch (rb.getId()) {
            case R.id.radio_chat:
                MyToast(getApplicationContext(), "radio_chat", Toast.LENGTH_LONG);
                break;
            case R.id.radio_contact:
                MyToast(getApplicationContext(), "radio_contact", Toast.LENGTH_LONG);
                break;

            default:
                break;
        }
    };*/
}