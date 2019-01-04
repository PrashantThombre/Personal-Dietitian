package edu.sjsu.cameraapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import com.theartofdev.edmodo.cropper.CropImage;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.widget.Toast;
import android.os.StrictMode;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import edu.sjsu.apicall.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.lang.String;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    private static final String IP_ADDR = "http://172.20.10.9:8000";


    static final int REQUEST_CAPTURE = 1;
    ImageView iv_camera;
    public  static Button  btn_camera;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1002;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 1003;

    private File output=null;
    private File croppedFile = null;

    public static TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Food Administrator");
        //
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        btn_camera = (Button) findViewById(R.id.btn_camera);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        textView = (TextView) findViewById(R.id.textView);

        new GetUrlContentTask().execute(IP_ADDR+"/drugagent/");


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
    }

    public void launchCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        output = new File(getApplicationContext().getExternalCacheDir().getPath(), "pickImageResult.jpeg");
        Uri outputFileUri = Uri.fromFile(output);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent,REQUEST_CAPTURE);
    }
    private Boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK){

            Uri imageUri = CropImage.getCaptureImageOutputUri(this);
            Intent cropIntent = CropImage.activity(imageUri).getIntent(this);
            startActivityForResult(cropIntent,CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            new GetUrlContentTask().execute(IP_ADDR+"/drugagent/");
        }

        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap mCropImage = null;
                try{
                    mCropImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                    croppedFile = new File(getApplicationContext().getExternalCacheDir().getPath(), "CroppedImage.jpg");
                    mCropImage.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(croppedFile));


                }catch (Exception e){

                }
                iv_camera.setImageBitmap(mCropImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!hasCamera()){
                        btn_camera.setEnabled(false);
                    }
                } else {
                    Toast.makeText(this, "The app needs the Write permission!", Toast.LENGTH_LONG).show();
                    btn_camera.setEnabled(false);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!hasCamera()){
                        btn_camera.setEnabled(false);
                    }
                } else {
                    Toast.makeText(this, "The app needs the READ permission!", Toast.LENGTH_LONG).show();
                    btn_camera.setEnabled(false);
                }
                return;
            }
        }
    }


    public void postImage(View view){
        final String selectedFilePath = croppedFile.getAbsolutePath();
        System.out.println(selectedFilePath+" :Selected");

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new UploadImageTask().execute(selectedFilePath);
        }
    }

    private class UploadImageTask extends AsyncTask<String, Void, String> {
        private final String U_TAG = UploadImageTask.class.getSimpleName();


        @Override
        protected String doInBackground(String... paths) {
            try {
                int resp = uploadFile(paths[0]);
                return "Server response " + resp;
            } catch (Exception e) {
                return "Unable to upload image";
            }
        }


        public int uploadFile(final String selectedFilePath) {

            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "92Jq0M08a2Yt02f5jU53dfa4c0p1f55d";


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);


            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length - 1];

            if (!selectedFile.isFile()) {

                Log.e(U_TAG,"Source File Doesn't Exist: " + selectedFilePath);
                return 0;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(IP_ADDR+"/drugagent/list/");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("docfile", selectedFilePath);
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    String dispName= "image";
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\""+ dispName +"\";filename=\""
                            + fileName + "\"" + lineEnd);
                    dataOutputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    System.out.println(fileName);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        try {
                            dataOutputStream.write(buffer, 0, bufferSize);
                        } catch (OutOfMemoryError e) {
                            Toast.makeText(MainActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    try {
                        serverResponseCode = connection.getResponseCode();
                    } catch (OutOfMemoryError e) {
                    }
                    String serverResponseMessage = connection.getResponseMessage();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));


                    //response code of 200 indicates the server status OK
                    if (serverResponseCode == 201) {
                        System.out.println("201");
                        Log.e(U_TAG,"File Upload completed.\n\n " + fileName);
                    }else if(serverResponseCode == 200){
                        Intent i = new Intent(getApplicationContext(),Confirm2Activity.class);
                        System.out.println("200");
                        String s = null;
                        String complete = "";
                        while ((s=br.readLine())!=null)
                        {
                            complete += s;
                        }
                        System.out.println(complete);
                        String suggestions = complete.replaceAll("\\\\","").replaceAll("\\[","").replaceAll("\\]","");
                        System.out.println(suggestions);
                        i.putExtra("SUGGESTIONS", suggestions);
                        startActivity(i);

                        Toast.makeText(MainActivity.this, "200", Toast.LENGTH_SHORT).show();

                        Log.e(U_TAG,"File Upload completed 200.\n\n " + fileName);
                    }
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"File Not Found");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"URL Error!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"Cannot Read/Write File");
                }
                return serverResponseCode;
            }

        }
    }
}