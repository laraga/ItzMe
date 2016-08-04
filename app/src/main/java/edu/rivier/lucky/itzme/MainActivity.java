package edu.rivier.lucky.itzme;

import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;


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
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageToUploadUri=null;

                try
                {
                    File f = new File(getTempFile("IMG", ".jpg"));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    imageToUploadUri = Uri.fromFile(f);

                    startActivityForResult(intent, 1);
                }
                catch (IOException e)
                {
                    Toast.makeText(MainActivity.this, "Exception! " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button extractBtn = (Button)findViewById(R.id.extractbtn);
        extractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Info", "Extract Button Clicked");

                SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(MainActivity.this, "FileOpen",
                        new SimpleFileDialog.SimpleFileDialogListener()
                        {
                            @Override
                            public void onChosenDir(String chosenDir)
                            {
                                String chosenFile;
                                // The code in this function will be executed when the dialog OK button is pushed
                                chosenFile = chosenDir;
                                //toastLong("Chosen FileOpenDialog File: " + chosenFile);
                                try
                                {
                                    String tmpDirStr = getTempFile("TMP", ".tmp");
                                    File tmpDir = new File(tmpDirStr);
                                    tmpDir.delete();

                                    tmpDir.mkdirs();

                                    ZipUtil.unzip(chosenFile, tmpDirStr);
                                    Log.d("Unzip", "Unzipped File Successfully");
                                    if(SignatureUtil.verifySignature(tmpDirStr, "PUBKEY", "SIGNATURE", "IMGFILE"))
                                    {
                                        toastLong("Verified");
                                        Log.d("Info", "Verified");

                                        String imgFileStr = getOutPath();

                                        File fromFile = new File(tmpDirStr + "/IMGFILE");
                                        File toFile = new File(imgFileStr);

                                        fromFile.renameTo(toFile);

                                        toastLong("Image Saved: " + imgFileStr);
                                    }
                                    else
                                    {
                                        toastLong("Failed Verification");
                                        Log.d("Info", "Failed");
                                    }

                                    rmDir(tmpDirStr);
                                }
                                catch (IOException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                                catch (NoSuchAlgorithmException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                                catch (InvalidKeyException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                                catch (SignatureException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                                catch (NoSuchProviderException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                                catch (InvalidKeySpecException e)
                                {
                                    toastLong("Error: "+e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });

                FileOpenDialog.Default_File_Name = "";
                FileOpenDialog.chooseFile_or_Dir();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            if (imageToUploadUri != null)
            {
                //Image captured and saved to fileUri specified in Intent

                try {
                    String imgFile = imageToUploadUri.getPath();
                    byte[] sigBytes = SignatureUtil.signFile(getCertDir() + "/privkey", imgFile);
                    String signFile = getTempFile("SIG", ".sig");
                    SignatureUtil.writeBytesToFile(sigBytes, signFile);
                    String zipFile = getZipFilePath();
                    ZipUtil.zip(imgFile, signFile, getCertDir()+"/pubkey", zipFile);

                    removeFile(imgFile);
                    removeFile(signFile);

                    Toast.makeText(MainActivity.this, zipFile + " Saved", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    Log.d("error", e.getMessage(), e);
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
            else
            {
                //Image captured and saved to fileUri specified in Intent

                toastLong("Image Capture Failed");
                Log.d("Itzme", "Image not Captured");
            }
        }

        if(resultCode == RESULT_CANCELED)
        {
            //user cancelled the image capture;
            Log.d("Itzme" ,"Result Cancelled");
        }

    }
/**************************** Utility Methods ****************************/

    private String getCertDir()
    {
        return (new ContextWrapper(this)).getApplicationInfo().dataDir + "/certificates";
    }

    private String getTempDir() throws IOException {
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/itzme/tmp");
        if (!tmpDir.exists())
            tmpDir.mkdirs();
        return tmpDir.getAbsolutePath();
    }

    private String getTempFile(String prefix, String extn) throws IOException {
        File tmpDir = new File(getTempDir());
        File outputFile = File.createTempFile(prefix, extn, tmpDir);
        return outputFile.getAbsolutePath();
    }

    private String getZipFilePath() throws IOException {
        File sigDir = new File(Environment.getExternalStorageDirectory() + "/itzme/signedimages");
        if(!sigDir.exists())
            sigDir.mkdirs();
        File outputFile = File.createTempFile("IMG", ".simg", sigDir);

        return outputFile.getAbsolutePath();
    }

    private String getOutPath() throws IOException {
        File verDir = new File(Environment.getExternalStorageDirectory() + "/itzme/verifiedimages");
        if(!verDir.exists())
            verDir.mkdirs();
        File outputFile = File.createTempFile("IMG", ".jpg", verDir);

        return outputFile.getAbsolutePath();
    }

    private void removeFile(String path)
    {
        File fileToDelete = new File(path);
        fileToDelete.delete();
    }

    private void rmDir(String path)
    {
        File dir = new File(path);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }

        dir.delete();
    }

    private void toastLong(String msg)
    {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

}
