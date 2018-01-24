package edu.sjsu.apicall;

import edu.sjsu.cameraapp.MainActivity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 * Created by Prash on 11/8/2017.
 */

 public class GetUrlContentTask extends AsyncTask<String, Integer, String>  {
    protected String doInBackground(String... urls){
        System.out.println("3");
        String content = "",line;
        try {
            System.out.println("4");
            System.out.println(urls[0]);
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int status = connection .getResponseCode();
            System.out.println(status);
            InputStream in = connection.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader rd = new BufferedReader(inr);
            content = "";
            System.out.println(rd);
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }
            System.out.println("6");
        }
        catch (Exception e){
            System.out.println(e);
        }
        return content;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        MainActivity.textView.setText(result);
    }




}