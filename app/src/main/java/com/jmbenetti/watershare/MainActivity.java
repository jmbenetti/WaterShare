package com.jmbenetti.watershare;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        share = findViewById(R.id.share);
        imageView = findViewById(R.id.shareimage);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);

        agregarMarca();

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageandText(bitmap);
            }
        });
    }


    private void agregarMarca() {
        Resources res = getResources();
        Drawable drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.marcadeaguaoscura, null);

        // The bitmap we want to watermark. It must be mutable.
        Bitmap bmpOriginal = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Bitmap mainBitmap = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);
        // The watermark itself. It doesn't have to be mutable.
        Bitmap watermarkBitmap = ((BitmapDrawable)drawableMarca).getBitmap();
        // Creating a canvas with mainBitmap
        Canvas canvas = new Canvas(mainBitmap);
        // The actual watermarking
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
}
