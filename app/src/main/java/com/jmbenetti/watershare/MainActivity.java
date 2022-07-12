package com.jmbenetti.watershare;

import static android.view.MotionEvent.INVALID_POINTER_ID;

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
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MotionEventCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
//
    Button btnCompartir;
    ImageView imgPrincipal;
    double nEscala = 1;
    double nMinimo = 0.1;
    double nMaximo = 10;
    double nVariacion = 0.1;
    int nDesplazamiento = 20;
    float nPosicionXMarca = 0;
    float nPosicionYMarca = 0;
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
    Bitmap bmpMarcaElegida;
    int mActivePointerId = INVALID_POINTER_ID;
    float mLastTouchX;
    float mLastTouchY;
    float mPosX;
    float mPosY;

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


        imgPrincipal = findViewById(R.id.shareimage);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        imgPrincipal.setLayoutParams(params);

        imgPrincipal.setOnTouchListener((v, event) -> {

            final int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = event.getActionIndex();
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);

                    // Recordar dónde empezamos(para arrastrar)
                    mLastTouchX = x;
                    mLastTouchY = y;
                    // Guardar el ID de este puntero(para arrastrar)
                    mActivePointerId = event.getPointerId(0);

                    //Toast.makeText(getApplicationContext(), x + ", " + y, Toast.LENGTH_SHORT).show();
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    // Encontrar el índice del puntero activo para obtener su posición
                    final int pointerIndex = event.findPointerIndex(mActivePointerId);

                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);

                    // Calcular la distancia movida
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    //Asigno la distancia movida a las variables de posición de marca y redibujo
                    nPosicionXMarca = mPosX;
                    nPosicionYMarca = mPosY;

                    dibujarConMarca();

                    // Recordar esta posición para el siguiente evento
                    mLastTouchX = x;
                    mLastTouchY = y;

                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    //Toast.makeText(getApplicationContext(), mPosX + ", " + mPosY, Toast.LENGTH_SHORT).show();
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {

                    final int pointerIndex = event.getActionIndex();
                    final int pointerId = event.getPointerId(pointerIndex);

                    if (pointerId == mActivePointerId) {
                        // Este era nuestro puntero activo. Elegir uno nuevo y ajustar.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = event.getX(newPointerIndex);
                        mLastTouchY = event.getY(newPointerIndex);
                        mActivePointerId = event.getPointerId(newPointerIndex);
                    }
                    break;
                }
            }
            return true;

        });


        dibujarConMarca();


        btnCompartir.setOnClickListener(v -> {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imgPrincipal.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            compartirImagenConTexto(bitmap);
        });

        btnAumentar.setOnClickListener(v -> {
            if (nEscala + nVariacion <= nMaximo) {
                nEscala += nVariacion;
                dibujarConMarca();
            }
        });

        btnReducir.setOnClickListener(v -> {
            if (nEscala - nVariacion >= nMinimo) {
                nEscala -= nVariacion;
                dibujarConMarca();
            }
        });


        btnArriba.setOnClickListener(v -> {
            if (nPosicionYMarca - nDesplazamiento >= -nMaxDesplazamiento) {
                nPosicionYMarca -= nDesplazamiento;
                dibujarConMarca();
            }
        });

        btnAbajo.setOnClickListener(v -> {
            if (nPosicionYMarca + nDesplazamiento <= nMaxDesplazamiento) {
                nPosicionYMarca += nDesplazamiento;
                dibujarConMarca();
            }
        });

        btnIzquierda.setOnClickListener(v -> {
            if (nPosicionXMarca - nDesplazamiento >= -nMaxDesplazamiento) {
                nPosicionXMarca -= nDesplazamiento;
                dibujarConMarca();
            }
        });

        btnDerecha.setOnClickListener(v -> {
            if (nPosicionXMarca + nDesplazamiento <= nMaxDesplazamiento) {
                nPosicionXMarca += nDesplazamiento;
                dibujarConMarca();
            }
        });

        ActivityResultLauncher<Intent> actCargarImagen
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        Intent datos = result.getData();
                        if (datos != null
                                && datos.getData() != null) {
                            Uri uriImagenElegida = datos.getData();
                            try {
                                bmpElegido
                                        = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        uriImagenElegida);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            nPosicionXMarca = 0;
                            nPosicionYMarca = 0;
                            dibujarConMarca();
                        }
                    }
                });

        ActivityResultLauncher<Intent> actCargarMarca
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        Intent datos = result.getData();
                        if (datos != null
                                && datos.getData() != null) {
                            Uri uriImagenElegida = datos.getData();
                            try {
                                bmpMarcaElegida
                                        = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        uriImagenElegida);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            nPosicionXMarca = 0;
                            nPosicionYMarca = 0;
                            dibujarConMarca();
                        }
                    }
                });

        btnImagen.setOnClickListener(v -> {
            Intent miIntent = new Intent();
            miIntent.setType("image/*");
            miIntent.setAction(Intent.ACTION_GET_CONTENT);

            actCargarImagen.launch(miIntent);
        });


        btnMarca.setOnClickListener(v -> {
            //Toast.makeText(getApplicationContext(), "Cambiar marca", Toast.LENGTH_SHORT).show();
            Intent miIntent = new Intent();
            miIntent.setType("image/*");
            miIntent.setAction(Intent.ACTION_GET_CONTENT);

            actCargarMarca.launch(miIntent);
        });

    }


    private void dibujarConMarca() {
        Resources res = getResources();
        //Levanto marca placeholder por si no hay nada seleccionado
        Drawable drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.marcadeaguaoscura, null);

        // Levanto imagen placeholder por si no hay nada seleccionado
        Drawable drawablePlaceHolder = ResourcesCompat.getDrawable(res, R.drawable.soniquito, null);

        Bitmap bmpOriginal;
        if(bmpElegido==null){
            bmpOriginal = ((BitmapDrawable) drawablePlaceHolder).getBitmap();
        }
        else
        {
            bmpOriginal = bmpElegido;
        }

        // Convierto el Bitmap elegido en mutable
        Bitmap mainBitmap = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);

        // La marca de agua
        Bitmap bmpMarca = null;
        if(bmpMarcaElegida == null) {
            bmpMarca = ((BitmapDrawable) drawableMarca).getBitmap();
        }
        else
        {
            bmpMarca = bmpMarcaElegida;
        }
        double nAlto = bmpMarca.getHeight();
        double nAncho = bmpMarca.getWidth();

        // La marca de agua cambiada de tamaño
        Bitmap watermarkBitmap = redimensionarBitmap(bmpMarca, (int) (nAncho * nEscala),
                (int) (nAlto * nEscala));
        // Creo un canvas con el bitmap mutable
        Canvas canvas = new Canvas(mainBitmap);
        // Pongo la marca de agua
        canvas.drawBitmap(watermarkBitmap, nPosicionXMarca, nPosicionYMarca, null);
        imgPrincipal.setImageDrawable(new BitmapDrawable(getResources(), mainBitmap));
    }

    private void compartirImagenConTexto(Bitmap bitmap) {
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

    public static Bitmap redimensionarBitmap(Bitmap bm, int newWidth, int newHeight) {
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
