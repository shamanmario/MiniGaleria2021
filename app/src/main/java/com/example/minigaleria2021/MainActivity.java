package com.example.minigaleria2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView foto;
    private final String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int TOMAR_FOTO = 200;
    private final int RECUPERAR_DESDE_GALERIA = 300;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foto = findViewById(R.id.fotito);
        bitmap = null;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            requestPermissions(permisos, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean cerrarApp = false;

        if(requestCode == 100){
            if(!(grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show();
                cerrarApp = true;
            }

            if(!(grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(this, "Se requiere permiso de escritura en memoria", Toast.LENGTH_SHORT).show();
                cerrarApp = true;
            }

            if(!(grantResults[2] == PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(this, "Se requiere permiso de lectura de memoria", Toast.LENGTH_SHORT).show();
                cerrarApp = true;
            }

        }

        if(cerrarApp){
            Toast.makeText(this, "Vuelva a abrir la app, y otorgue los permisos requeridos", Toast.LENGTH_SHORT).show();
            finishAffinity();
            System.exit(0);
        }

    }

    public void tomarFoto(View view){
        Intent intento = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intento, TOMAR_FOTO);
    }

    public void guardarFotoEnMemoria(View view){
        OutputStream streamSalida = null;
        File archivoFoto = null;
        String nombreArchivo = "";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentResolver resolver = getContentResolver();
            ContentValues valores = new ContentValues();

            nombreArchivo = System.currentTimeMillis()+"_fotoprueba";

            valores.put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo);
            valores.put(MediaStore.Images.Media.MIME_TYPE, "Image/jpg");
            valores.put(MediaStore.Images.Media.RELATIVE_PATH, "FotosPrueba/MiApp");
            valores.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri coleccion = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri fotoUri = resolver.insert(coleccion, valores);

            try{
                streamSalida = resolver.openOutputStream(fotoUri);
            } catch(FileNotFoundException e){
                e.printStackTrace();
            }

            valores.clear();
            valores.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(fotoUri, valores, null, null);



        } else {
            String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            nombreArchivo = System.currentTimeMillis()+"_fotoPrueba.jpg";
            archivoFoto = new File(ruta, nombreArchivo);

            try{
                streamSalida = new FileOutputStream(archivoFoto);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }

        }

        if(bitmap!=null){
            boolean fotoGuardada = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, streamSalida);

            if(fotoGuardada){
                Toast.makeText(this, "Foto guardada exitosamente", Toast.LENGTH_SHORT).show();
            }

            if(streamSalida != null){
                try{
                    streamSalida.flush();
                    streamSalida.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            if(archivoFoto != null){
                MediaScannerConnection.scanFile(this, new String[]{archivoFoto.toString()}, null, null);
            }



        } else {
            Toast.makeText(this, "Primero debe tomar una foto", Toast.LENGTH_SHORT).show();
        }








    }

    public void recuperardeGaleria(View view){
        Intent intento = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intento, RECUPERAR_DESDE_GALERIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case TOMAR_FOTO: //tomar fotito
                if(resultCode == RESULT_OK){
                    bitmap = (Bitmap) data.getExtras().get("data");
                    foto.setImageBitmap(bitmap);
                }
                break;

            case RECUPERAR_DESDE_GALERIA: //buscando fotito desde la galería
                if(resultCode == RESULT_OK){
                    Uri ruta = data.getData();
                    foto.setImageURI(ruta);
                }

                break;
            default:
                System.out.println("Aiuraaaaa");
        }

    }
}