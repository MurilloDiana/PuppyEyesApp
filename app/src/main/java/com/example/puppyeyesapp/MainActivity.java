package com.example.puppyeyesapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    TTSManager ttsManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //voz
        textView = findViewById(R.id.textView);
        //Service
        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
        //Texto a voz
        ttsManager= new TTSManager();
        ttsManager.init(this);

    }
    public void onClick(View v){
        startActivity(new Intent(MainActivity.this, MapsActivity.class));
        String texto = "Se encuentra en";
        //ttsManager.initQueue(texto);
    }
    // Voz a texto
    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  "es-MX");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Comienza a hablar");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            textView.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }
    }
}