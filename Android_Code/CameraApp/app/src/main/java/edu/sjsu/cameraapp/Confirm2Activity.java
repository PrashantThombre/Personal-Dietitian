package edu.sjsu.cameraapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.os.Parcelable;
import java.net.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.AutoCompleteTextView;
import android.os.StrictMode;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;

import java.util.List;


public class Confirm2Activity extends AppCompatActivity {
    private static final String IP_ADDR = "http://172.20.10.9:8000";
    //    private static final String IP_ADDR = "http://10.250.84.12:8000";
//    private static final String IP_ADDR = "http://192.168.0.21:8000";
    Button btnSubmit = null;
    RadioGroup radioGroup1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm2);
        setTitle("Suggested Drugs");
        btnSubmit = (Button)findViewById(R.id.btn_confirm);
        radioGroup1 = (RadioGroup) findViewById(R.id.radiogroup);

        Intent intent = getIntent();
        String suggestions = intent.getStringExtra("SUGGESTIONS");
        String[] suggestionList = suggestions.split(",");
//        RadioGroup ll = new RadioGroup(this);
//        ll.setOrientation(LinearLayout.HORIZONTAL);
        int counter = 1857;
        for(String suggestion:suggestionList){
            counter++;
            RadioButton rBtn = new RadioButton(this);
            rBtn.setId(View.generateViewId());
            rBtn.setText(suggestion);
            radioGroup1.addView(rBtn);
//            ll.addView(rBtn);
        }
        /*ArrayAdapter itemArrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.activity_confirm2);
        InputStream inputStream = getResources().openRawResource(R.raw.synonym_drugs_new);
        CSVFileReader csvFile = new CSVFileReader(inputStream);
        List scoreList = csvFile.read();

        for(Object str : scoreList) {
            itemArrayAdapter.add(str);
        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, DRUGS);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextView);
        textView.setAdapter(itemArrayAdapter);*/

//        ((ViewGroup) findViewById(R.id.radiogroup)).addView(ll);
    }
/*    private static final String[] DRUGS = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };*/


    public void getRecommendations(View view){
        int radioBtnChecked = radioGroup1.getCheckedRadioButtonId();
        RadioButton rBtn = (RadioButton) findViewById(radioBtnChecked);
        String radioText = rBtn.getText().toString().replaceAll("\"","");
        final String drugname= radioText;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(this.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            new GetRecommendationsTask().execute(drugname);
        } else {

        }
    }

    private class GetRecommendationsTask extends AsyncTask<String, Void, String> {
        private final String U_TAG = GetRecommendationsTask.class.getSimpleName();
        HttpURLConnection connection;
        OutputStreamWriter dataOutputStream;

        @Override
        protected String doInBackground(String... paths) {
            try {
                String resp = getRecommendations(paths[0]);
                return "Server response " + resp;
            } catch (Exception e) {
                return "Unable to upload image";
            }
        }
        @Override
        protected void onPostExecute(String result) {
//            tUploadStatus.setText( result);

        }

        public String getRecommendations(String drugName){
            int serverResponseCode = 0;

            String recommendations = "";
            try{
                URL url = new URL(IP_ADDR+"/drugagent/recommend/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                dataOutputStream = new OutputStreamWriter(connection.getOutputStream ());
                dataOutputStream.write("drugname="+drugName);
                dataOutputStream.flush ();
                dataOutputStream.close ();

                try {
                    serverResponseCode = connection.getResponseCode();
                } catch (OutOfMemoryError e) {
//                    Toast.makeText(MainActivity.this, "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                }
                String serverResponseMessage = connection.getResponseMessage();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                if(serverResponseCode == 200){
                    Intent i = new Intent(getApplicationContext(),FinalActivity.class);
                    System.out.println("200");
                    String s = null;
                    String complete = "";
                    while ((s=br.readLine())!=null)
                    {
                        complete += s;
                    }
                    System.out.println(complete);
//                    String[] suggestions = complete.split(",");
                    i.putExtra("RECOS", complete);
                    startActivity(i);

//                    Toast.makeText(MainActivity.this, "200", Toast.LENGTH_SHORT).show();

                }

            }catch (Exception e){
                e.printStackTrace();
            }

        return "Message";
        }
    }
}
