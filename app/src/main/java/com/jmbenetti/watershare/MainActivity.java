package com.jmbenetti.watershare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btnCompartir;
    ImageView imageView;
    double nEscala = 1;
    double nMinimo = 0.1;
    double nMaximo = 10;
    double nVariacion = 0.1;
    int nDesplazamiento = 20;
    int nPosX = 0;
    int nPosY = 0;
    int nMaxDesplazamiento = 4000;
    Button btnAumentar;
    Button btnReducir;
    Button btnIzquierda;
    Button btnDerecha;
    Button btnAbajo;
    Button btnArriba;
    Button btnImagen;
    Button btnMarca;
    Bitmap bmpElegido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCompartir = findViewById(R.id.share);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnReducir = findViewById(R.id.btnReducir);
        btnAbajo = findViewById(R.id.btnAbajo);
        btnArriba = findViewById(R.id.btnArriba);
        btnIzquierda = findViewById(R.id.btnIzquierda);
        btnDerecha = findViewById(R.id.btnDerecha);
        btnImagen = findViewById(R.id.btnImagen);
        btnMarca = findViewById(R.id.btnMarca);
        bmpElegido = null;


        imageView = findViewById(R.id.shareimage);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);

        dibujarConMarca();

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageandText(bitmap);
            }
        });

        btnAumentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"Aumentar",Toast.LENGTH_SHORT).show();
                if (nEscala + nVariacion <= nMaximo) {
                    nEscala += nVariacion;
                    dibujarConMarca();
                }
            }
        });

        btnReducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"Reducir",Toast.LENGTH_SHORT).show();
                if (nEscala - nVariacion >= nMinimo) {
                    nEscala -= nVariacion;
                    dibujarConMarca();
                }
            }
        });


        btnArriba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nPosY - nDesplazamiento >= -nMaxDesplazamiento) {
                    nPosY -= nDesplazamiento;
                    dibujarConMarca();
                }
            }
        });

        btnAbajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"Abajo",Toast.LENGTH_SHORT).show();
                if (nPosY + nDesplazamiento <= nMaxDesplazamiento) {
                    nPosY += nDesplazamiento;
                    dibujarConMarca();
                }
            }
        });

        btnIzquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"Izquierda",Toast.LENGTH_SHORT).show();
                if (nPosX - nDesplazamiento >= -nMaxDesplazamiento) {
                    nPosX -= nDesplazamiento;
                    dibujarConMarca();
                }
            }
        });

        btnDerecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"Derecha",Toast.LENGTH_SHORT).show();
                if (nPosX + nDesplazamiento <= nMaxDesplazamiento) {
                    nPosX += nDesplazamiento;
                    dibujarConMarca();
                }
            }
        });

        ActivityResultLauncher<Intent> launchSomeActivity
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // do your operation from here....
                        if (data != null
                                && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            //Bitmap selectedImageBitmap;
                            try {
                                bmpElegido
                                        = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        selectedImageUri);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
//                            imageView.setImageBitmap(
//                                    bmpElegido);
                            dibujarConMarca();
                        }
                    }
                });

        btnImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Toast.makeText(getApplicationContext(), "Cambiar imagen", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                launchSomeActivity.launch(i);
            }
        });


        btnMarca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Cambiar marca", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void dibujarConMarca() {
        Resources res = getResources();
        Drawable drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.marcadeaguaoscura, null);

        // Levanto imagen placeholder si no hay nada seleccionado
        Drawable drawablePlaceHolder = ResourcesCompat.getDrawable(res, R.drawable.soniquito, null);

        Bitmap bmpOriginal = null;
        if(bmpElegido==null){
            bmpOriginal = ((BitmapDrawable) drawablePlaceHolder).getBitmap();
        }
        else
        {
            bmpOriginal = bmpElegido;
        }

        // Lo convierto en mutable
        Bitmap mainBitmap = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);
        // La marca de agua
        Bitmap watermarkOriginal = ((BitmapDrawable) drawableMarca).getBitmap();
        double nAlto = watermarkOriginal.getHeight();
        double nAncho = watermarkOriginal.getWidth();

        // La marca de agua cambiada de tamaño
        Bitmap watermarkBitmap = getResizedBitmap(watermarkOriginal, (int) (nAncho * nEscala),
                (int) (nAlto * nEscala));
        // Creo un canvas con el bitmap mutable
        Canvas canvas = new Canvas(mainBitmap);
        // Pongo la marca de agua
        canvas.drawBitmap(watermarkBitmap, nPosX, nPosY, null);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), mainBitmap));
    }

    private void shareImageandText(Bitmap bitmap) {
        Uri uri = getmageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        // uri de la imagen a compartir
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // texto para compartir
        intent.putExtra(Intent.EXTRA_TEXT, "Compartiendo via WaterShare");

        // Asunto
        intent.putExtra(Intent.EXTRA_SUBJECT, "Asunto del mensaje");

        // tipo imagen
        intent.setType("image/png");

        // compartir a través de...
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    // Recuperando la uri para compartir
    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.jmbenetti.watershare.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return uri;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Crear matrix para manipulación
        Matrix matrix = new Matrix();
        // Cambiar tamaño de bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // Recrear el bitmap
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }

}
