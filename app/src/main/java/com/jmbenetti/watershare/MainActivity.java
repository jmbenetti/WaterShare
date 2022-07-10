package com.jmbenetti.watershare;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Button share;
    ImageView imageView;
    double nEscala = 1;
    double nMinimo = 0.1;
    double nMaximo = 10;
    double nVariacion = 0.1;
    Button btnAumentar;
    Button btnReducir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        share = findViewById(R.id.share);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnReducir = findViewById(R.id.btnReducir);
        imageView = findViewById(R.id.shareimage);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);

        dibujarConMarca();

        share.setOnClickListener(new View.OnClickListener() {
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

    }


    private void dibujarConMarca() {
        Resources res = getResources();
        Drawable drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.marcadeaguaoscura, null);

        // El drawable original
        Drawable drawableImagen = ResourcesCompat.getDrawable(res, R.drawable.soniquito, null);
        // Lo convierto en bitmap
        Bitmap bmpOriginal = ((BitmapDrawable) drawableImagen).getBitmap();

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
        canvas.drawBitmap(watermarkBitmap, 0, 0, null);
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
