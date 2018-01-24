package edu.sjsu.cameraapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

public class FinalActivity extends AppCompatActivity {
    TextView tv_reco ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        setTitle("Food Administrator");
        tv_reco = (TextView) findViewById(R.id.txt_recommendations);
        Intent i  = getIntent();
        String recommendations = i.getStringExtra("RECOS").replaceAll(";","\n");
        if(recommendations.isEmpty() || recommendations.equals("")){
            recommendations = "Hurray! No restrictions!";
        }
        tv_reco.setText(recommendations);
    }
}
