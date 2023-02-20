package com.jherrera.usocamara;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //create local vars
    private Button buttonTakePicture;
    private ImageView imageViewPicture;
    private ImageButton imageButtonSendWhatsApp;
    private ImageButton imageButtonSendEmail;
    private String imageUrl = "";

    /***
     * Using a global Uri to share image in whatsapp or email
     */
    private Uri imageUri = null;

    /***
     * petition code
     */
    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //calls initialize and events method
        setInitComponents();
        addEventButtons();
    }

    /**
     * This method initialize components
     * */
    private void setInitComponents() {
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        imageViewPicture = findViewById(R.id.imageViewPicture);
        imageButtonSendWhatsApp = findViewById(R.id.imageButtonSendWhatsApp);
        imageButtonSendEmail = findViewById(R.id.imageButtonSendEmail);
    }

    /**
     * This method add events buttons
     * */
    private void addEventButtons() {
        buttonTakePicture.setOnClickListener(view -> {
            processPicture();
        });

        imageButtonSendWhatsApp.setOnClickListener(view -> {
            sendWhatsApp();
        });

        imageButtonSendEmail.setOnClickListener(view -> {
            sendEmail();
        });
    }

    private void sendWhatsApp() {
        Intent intentWhatsApp = new Intent(Intent.ACTION_SEND);
        intentWhatsApp.setType("image/*");
        intentWhatsApp.setPackage("com.whatsapp");
        intentWhatsApp.putExtra(Intent.EXTRA_TEXT, "Hola! Esta es la imagen ");

        if (this.imageUri != null) {
            try {
                intentWhatsApp.putExtra(Intent.EXTRA_STREAM, this.imageUri);
                startActivity(intentWhatsApp);
            }catch (Exception e){
                Toast.makeText(this, "Error al enviar fotografia", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Por favor toma una fotografia", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        String[] TO = {"herrera_jh17@outlook.com"}; //Direcciones email  a enviar.
        String[] CC = {"u20190846@univo.edu.sv"}; //Direcciones email con copia.

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("image/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Compartiendo foto por correo");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola! Esta es la imagen "); // * configurar email aquí!

        try {
            if (this.imageUri != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, this.imageUri);
                startActivity(Intent.createChooser(emailIntent, "Enviar email."));
            }else {
                Toast.makeText(this, "Por favor toma una fotografia", Toast.LENGTH_SHORT).show();
            }
        }
        catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No existe ningún cliente de correo instalado!.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processPicture() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicture();
            }else {
                ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        }else {
            takePicture();
        }
    }

    /**
     * Using camera for take a picture
     * */
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File filePicture = null;
            filePicture = createFile();

            if (filePicture != null) {
                Uri urlFile = FileProvider.getUriForFile(
                        this,
                        "com.jherrera.usocamara",
                        filePicture
                );

                //guardando la uri global de la imagen para poder compartir por whatsapp o email
                this.imageUri = urlFile;

                intent.putExtra(MediaStore.EXTRA_OUTPUT, urlFile);
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * This method create new file
     * */
    private File createFile() {
        String nomeclature = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(new Date());

        String filePrefix = "APPCAM_"+nomeclature+"_";
        File fileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File myImage = null;

        try {
            myImage = File.createTempFile(filePrefix, ".jpg", fileDir);
            imageUrl = myImage.getAbsolutePath();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return myImage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {

        if(requestCode == REQUEST_CODE_CAMERA) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            }else {
                Toast.makeText(this, "Se requieren permisos para la camara", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                imageViewPicture.setImageURI(Uri.parse(imageUrl));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}