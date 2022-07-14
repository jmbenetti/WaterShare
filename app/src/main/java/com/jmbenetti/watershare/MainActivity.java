package com.jmbenetti.watershare;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //
    Button btnCompartir;
    ImageView imgPrincipal;
    double nEscala = .5;
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
    int nAnchoPredeterminado = 1024;
    static int nDensidadPredeterminada = 260;
    int nAnchoActualMarca;
    int nAltoActualMarca;
    boolean bArrastrarMarca = false;
    int nTransparencia = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_round);

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

//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT
//        );
//        imgPrincipal.setLayoutParams(params);

        imgPrincipal.setOnTouchListener((v, event) -> {

            final int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = event.getActionIndex();
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    boolean bCoincideX = false;
                    boolean bCoincideY = false;
                    if (x >= nPosicionXMarca && x <= nPosicionXMarca + nAnchoActualMarca) {
                        bCoincideX = true;
                    }
                    if (y >= nPosicionYMarca && y <= nPosicionYMarca + nAltoActualMarca) {
                        bCoincideY = true;
                    }
                    if (bCoincideX && bCoincideY) {
                        bArrastrarMarca = true;
                    }
                    else
                    {
                        bArrastrarMarca = false;
                    }
                    // Recordar dónde empezamos(para arrastrar)
                    mLastTouchX = x;
                    mLastTouchY = y;
                    // Guardar el ID de este puntero(para arrastrar)
                    mActivePointerId = event.getPointerId(0);
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

                    if(bArrastrarMarca) {
                        //Asigno la distancia movida a las variables de posición de marca y redibujo
                        nPosicionXMarca = mPosX;
                        nPosicionYMarca = mPosY;
                        //System.out.println("Marca en : " + nPosicionXMarca + ", " + nPosicionYMarca);


                        dibujarConMarca();
                    }

                    // Recordar esta posición para el siguiente evento
                    mLastTouchX = x;
                    mLastTouchY = y;

                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    bArrastrarMarca = false;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    bArrastrarMarca = false;
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
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            bmpElegido = redimensionarAnchoBitmap(bmpElegido, nAnchoPredeterminado);

                            nPosicionXMarca = 0;
                            nPosicionYMarca = 0;
                            mLastTouchX = 0;
                            mLastTouchY = 0;
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
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bmpMarcaElegida = redimensionarAnchoBitmap(bmpMarcaElegida, nAnchoPredeterminado);
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
        //Marca placeholder por si no hay nada seleccionado
        Drawable drawableMarca;
        // Placeholder imagen por si no hay nada seleccionado
        Drawable drawablePlaceHolder;

        Bitmap bmpOriginal;
        if (bmpElegido == null) {
            drawablePlaceHolder = ResourcesCompat.getDrawable(res, R.drawable.placeholderimagen, null);
            bmpOriginal = ((BitmapDrawable) drawablePlaceHolder).getBitmap();
            bmpOriginal = redimensionarAnchoBitmap(bmpOriginal, nAnchoPredeterminado);
        } else {
            bmpOriginal = bmpElegido;
        }

        // Convierto el Bitmap elegido en mutable
        Bitmap mainBitmap = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);

        // La marca de agua
        Bitmap bmpMarca = null;
        if (bmpMarcaElegida == null) {
            drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.defaultwatermark, null);
            bmpMarca = ((BitmapDrawable) drawableMarca).getBitmap();
            bmpMarca = redimensionarAnchoBitmap(bmpMarca, nAnchoPredeterminado);
        } else {
            bmpMarca = bmpMarcaElegida;
        }
        //Ajusto la marca de agua según la escala manual
        bmpMarca = redimensionarAnchoBitmap(bmpMarca, (int) (nAnchoPredeterminado * nEscala));
        // Creo un canvas con el bitmap mutable
        Canvas canvas = new Canvas(mainBitmap);
        //Elijo el nivel de transparencia
        Paint paint = new Paint();
        paint.setAlpha(nTransparencia);
        // Pongo la marca de agua
        canvas.drawBitmap(bmpMarca, nPosicionXMarca, nPosicionYMarca, paint);
        nAnchoActualMarca = bmpMarca.getWidth();
        nAltoActualMarca = bmpMarca.getHeight();
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
        Bitmap bmpResultado;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Crear matrix para manipulación
        Matrix matrix = new Matrix();
        // Cambiar tamaño de bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // Recrear el bitmap
        bmpResultado = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bmpResultado.setDensity(nDensidadPredeterminada);
        return bmpResultado;
    }

    public static Bitmap redimensionarAnchoBitmap(Bitmap bm, int anchoRequerido) {
        int anchoCargado = bm.getWidth();
        int altoCargado = bm.getHeight();
        double nuevoAlto = (double) anchoRequerido / (double) anchoCargado * (double) altoCargado;
        return redimensionarBitmap(bm, anchoRequerido, (int) nuevoAlto);
    }


}
