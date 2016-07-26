package com.example.lucky.itzme;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity
{
    //Uniform Resource Identifier (URI) reference
    private Uri imageToUploadUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.capturebtn);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String tmpDir= Environment.getExternalStorageDirectory()+"/tmpcamtest";
                File f = new File(tmpDir, "CAMTEST_IMAGE.jpg");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                imageToUploadUri = Uri.fromFile(f);

                startActivityForResult(takePictureIntent, 1);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (imageToUploadUri !=null)
        {
             //Image captured and saved to fileUri specified in Intent

             Log.d("Itzme", "Image Captured");
            Toast.makeText(this, "image saved to:\n" + imageToUploadUri, Toast.LENGTH_LONG).show();
        }
        if(imageToUploadUri ==null)
        {
            //Image captured and saved to fileUri specified in Intent

            Toast.makeText(this, "No image saved to:\n" + imageToUploadUri, Toast.LENGTH_LONG).show();
            Log.d("Itzme", "Image not Captured");
        }

        if(resultCode == RESULT_CANCELED)
        {
            //user cancelled the image capture;
            Log.d("Itzme" ,"Result Cancelled");
        }


    }
}

