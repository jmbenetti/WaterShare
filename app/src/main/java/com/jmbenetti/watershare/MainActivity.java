package com.jmbenetti.watershare;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
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
//import com.jmbenetti.watershare.CustomImageView;

public class MainActivity extends AppCompatActivity {
    //
    Button btnCompartir;
    ImageView imgPrincipal;
    double nEscala = .5;
    double nMinimo = 0.1;
    double nMaximo = 10;
    double nVariacion = 0.1;
    //    float nPosicionXMarca = 0;
//    float nPosicionYMarca = 0;
//    float nPosicionXPorcentaje = 0;
//    float nPosicionYPorcentaje = 0;
    double nPosicionXMarca = 0;
    double nPosicionYMarca = 0;
    double nPosicionXPorcentaje = 0;
    double nPosicionYPorcentaje = 0;


    boolean bLeerPosicionGuardada = false;
    Button btnAumentar;
    Button btnReducir;
    Button btnImagen;
    Button btnMarca;
    Button btnSave;
    Bitmap bmpElegido;
    Bitmap bmpMarcaElegida;
    SeekBar seekBarTransparencia;
    int mActivePointerId = INVALID_POINTER_ID;
    //    float mLastTouchX;
//    float mLastTouchY;
    double mLastTouchX;
    double mLastTouchY;
    //    float mPosX;
//    float mPosY;
    int nAnchoPredeterminado = 1024;
    static int nDensidadPredeterminada = 260;
    //    float nAnchoActualMarca;
//    float nAltoActualMarca;
    double nAnchoActualMarca;
    double nAltoActualMarca;

    boolean bArrastrarMarca = false;
    int nOpacidadPredeterminada = 50;
    int nOpacidadMarca;
    int nDensidadPantalla;
    Uri uriExterno;
    float nAnchoDibujado;
    float nAltoDibujado;
    boolean bPreparandoImagen;
    boolean bDirectShare;
//    boolean bCerrarApp = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        nDensidadPantalla = (int) (metrics.density * 160f);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher_round);
        }

        nOpacidadMarca = nOpacidadPredeterminada;

        btnCompartir = findViewById(R.id.btnShare);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnReducir = findViewById(R.id.btnReducir);
        btnSave = findViewById(R.id.btnSave);

        btnImagen = findViewById(R.id.btnImagen);
        btnMarca = findViewById(R.id.btnMarca);
        seekBarTransparencia = findViewById(R.id.seekBarTransparencia);
        bmpElegido = null;
        seekBarTransparencia.setProgress(nOpacidadMarca);

        seekBarTransparencia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                nOpacidadMarca = progress;
                dibujarConMarca();

            }
        });


        imgPrincipal = findViewById(R.id.shareimage);

        imgPrincipal.setOnTouchListener(touchListenerImg);

        imgPrincipal.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //imgPrincipal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float imageViewSurfaceWidth = imgPrincipal.getWidth();
                float imageViewSurfaceHeight = imgPrincipal.getHeight();
                nAnchoDibujado = imageViewSurfaceWidth;
                nAltoDibujado = imageViewSurfaceHeight;
                //Log.i("prueba", "width = " + imageViewSurfaceWidth + ", height = " + imageViewSurfaceHeight);


                if (bPreparandoImagen) {
                    dibujarConMarca();
                    bPreparandoImagen = false;
                    if (bDirectShare) {
//                        bCerrarApp = true;
                        compartirConMarca();
                        bDirectShare = false;
//                        bDirectShare = false;
//                        ActivityShareNow.szUriRecibida = "";
                    }
//                    ActivityShareNow.szUriRecibida = "";
//                    ActivityEdit.szUriRecibida = "";
                }
            }
        });



        //Levanto la marca de agua por defecto guardada, si es que existe
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("watermarks", Context.MODE_PRIVATE);
            File mypath = new File(directory, "default.png");
            definirMarcaConUri(Uri.fromFile(mypath));
//            definirMarcaConUri(Uri.fromFile(new File("/data/user/0/com.jmbenetti.watershare/app_watermarks/default.png")));
        }
        catch(Exception e)
        {
            //No hay marca guardada
        }

        prepararTodo();


        btnSave.setOnClickListener(v -> {
            guardarBitmap(bmpMarcaElegida, "watermarks", "default.png");
            guardarDatosMarca();
        });

        btnCompartir.setOnClickListener(v -> {
            compartirConMarca();
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
                            definirImagenConUri(uriImagenElegida);
                            leerDatosMarca();
                            limpiarImageView();
                            prepararImageView();
                            //dibujarConMarca();
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
                            definirMarcaConUri(uriImagenElegida);
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
            Intent miIntent = new Intent();
            miIntent.setType("image/*");
            miIntent.setAction(Intent.ACTION_GET_CONTENT);

            actCargarMarca.launch(miIntent);
        });

    }

    private void prepararTodo()
    {
        recibirUriActividades();
        leerDatosMarca();
        limpiarImageView();
        prepararImageView();
    }

    private void recibirUriActividades()
    {
        if (ActivityEdit.szUriRecibida != "") {
            recibirImagenEnviada(ActivityEdit.szUriRecibida);
            ActivityEdit.instancia.finishAndRemoveTask();
            bDirectShare = false;
        }

        if (ActivityShareNow.szUriRecibida != "") {
            recibirImagenEnviada(ActivityShareNow.szUriRecibida);
            ActivityShareNow.instancia.finishAndRemoveTask();
            bDirectShare = true;
        }

        if (uriExterno != null) {
            definirImagenConUri(uriExterno);
            uriExterno = null;
        }
        ActivityShareNow.szUriRecibida = "";
        ActivityEdit.szUriRecibida = "";

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //S??lo inicializo si vengo de otra activity con datos compartidos

        if (ActivityEdit.szUriRecibida != "" | ActivityShareNow.szUriRecibida !="") {
            prepararTodo();
        }
        //Toast.makeText(getApplicationContext(), "Nuevo intent", Toast.LENGTH_LONG).show();
    }

    private void compartirConMarca() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imgPrincipal.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        compartirImagenConTexto(bitmap);
    }

    private void limpiarImageView() {
        imgPrincipal.setImageDrawable(null);
    }

    private void prepararImageView() {

        Resources res = getResources();
        Drawable drawablePlaceHolder;

        Bitmap bmpOriginal = null;
        if (bmpElegido == null) {
            drawablePlaceHolder = ResourcesCompat.getDrawable(res, R.drawable.placeholderimagen, null);
            if (drawablePlaceHolder != null) {
                bmpOriginal = ((BitmapDrawable) drawablePlaceHolder).getBitmap();
                bmpOriginal = redimensionarAnchoBitmap(bmpOriginal, nAnchoPredeterminado);
                //Guardo la imagen predeterminada como elegida para procesar afuera
                bmpElegido = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);
            }
        } else {
            bmpOriginal = bmpElegido.copy(Bitmap.Config.ARGB_8888, true);
        }

        //Pongo la imagen como est?? para que se me actualice alto y ancho de imageview
        imgPrincipal.setImageDrawable(new BitmapDrawable(getResources(), bmpOriginal));
        bPreparandoImagen = true;

    }

    private View.OnTouchListener touchListenerImg = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final int action = event.getActionMasked();

            //Guardo el ancho de la marca en escala de pantalla t??ctil
//            float f = imgPrincipal.getWidth();
//            nAnchoActualMarca = (float) (bmpMarcaElegida.getWidth() * imgPrincipal.getWidth() /
//                    bmpElegido.getWidth() * nEscala);
//            nAltoActualMarca = (float) (bmpMarcaElegida.getHeight() * imgPrincipal.getHeight() /
//                    bmpElegido.getHeight() * nEscala);

            nAnchoActualMarca = (double) (bmpMarcaElegida.getWidth() * imgPrincipal.getWidth() /
                    bmpElegido.getWidth() * nEscala);
            nAltoActualMarca = (double) (bmpMarcaElegida.getHeight() * imgPrincipal.getHeight() /
                    bmpElegido.getHeight() * nEscala);

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = event.getActionIndex();
//                    final float x = event.getX(pointerIndex);
//                    final float y = event.getY(pointerIndex);
                    final double x = event.getX(pointerIndex);
                    final double y = event.getY(pointerIndex);

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
                        mLastTouchX = x;
                        mLastTouchY = y;
                    } else {
                        bArrastrarMarca = false;
                    }

                    // Guardar el ID de este puntero(para arrastrar)
                    mActivePointerId = event.getPointerId(0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (bArrastrarMarca) {
                        // Encontrar el ??ndice del puntero activo para obtener su posici??n
                        final int pointerIndex = event.findPointerIndex(mActivePointerId);
                        final double x = event.getX(pointerIndex);
                        final double y = event.getY(pointerIndex);


                        // Calcular la distancia movida
                        final double dx = x - mLastTouchX;
                        final double dy = y - mLastTouchY;

                        //Reviso que est?? dentro de los l??mites del imageview para mover
                        boolean bMoverHorizontal =
                                nPosicionXMarca + dx > 0 - nAnchoActualMarca / 2 && nPosicionXMarca + dx <= imgPrincipal.getWidth() - nAnchoActualMarca / 3;
                        boolean bMoverVertical =
                                nPosicionYMarca + dy > 0 - nAltoActualMarca / 2 && nPosicionYMarca + dy <= imgPrincipal.getHeight() - nAltoActualMarca / 3;
//                                y > nAltoActualMarca / 2 && y <= imgPrincipal.getHeight() - nAltoActualMarca /3;

                        if (bMoverHorizontal) {
                            nPosicionXMarca += dx;
                            mLastTouchX = x;
                        }
                        if (bMoverVertical) {
                            nPosicionYMarca += dy;
                            mLastTouchY = y;
                        }

                        dibujarConMarca();


                        // Recordar esta posici??n para el siguiente evento


                    }

                    break;
                }

                case MotionEvent.ACTION_UP:
                    //lo mismo que cancel, sigue abajo
                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    bArrastrarMarca = false;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {

                    final int pointerIndex = event.getActionIndex();
                    final int pointerId = event.getPointerId(pointerIndex);

                    if (pointerId == mActivePointerId) {
                        if (bArrastrarMarca) {
                            // Este era nuestro puntero activo. Elegir uno nuevo y ajustar.
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            mLastTouchX = event.getX(newPointerIndex);
                            mLastTouchY = event.getY(newPointerIndex);
                            mActivePointerId = event.getPointerId(newPointerIndex);
                        }
                    }
                    break;
                }
            }
            return true;


            //
        }
    };

    void guardarBitmap(Bitmap bmpGuardado, String szCarpeta, String szNombre) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(szCarpeta, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File mypath = new File(directory, szNombre);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bmpGuardado.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void definirImagenConUri(Uri uri) {
        try {
            bmpElegido
                    = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bmpElegido = redimensionarAnchoBitmap(bmpElegido, nAnchoPredeterminado);

    }

    void guardarDatosMarca() {
        SharedPreferences sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);

        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Guardo la posici??n de la marca como porcentaje de cada eje
//        float nX = nPosicionXMarca * 100 / imgPrincipal.getWidth();
//        float nY = nPosicionYMarca * 100 / imgPrincipal.getHeight();
        double nX = nPosicionXMarca * 100 / imgPrincipal.getWidth();
        double nY = nPosicionYMarca * 100 / imgPrincipal.getHeight();

        myEdit.putString("watermark_x", String.valueOf(nX));
        myEdit.putString("watermark_y", String.valueOf(nY));
        myEdit.putString("watermark_scale", String.valueOf(nEscala));
        myEdit.putString("watermark_opacity", String.valueOf(nOpacidadMarca));

        myEdit.commit();

    }

    void leerDatosMarca() {
        try {
            SharedPreferences sh = getSharedPreferences("appdata", Context.MODE_PRIVATE);

//        nPosicionXPorcentaje = sh.getFloat("watermark_x", 0f);
//        nPosicionYPorcentaje = sh.getFloat("watermark_y", 0f);
//        nEscala = (double) sh.getFloat("watermark_scale", 0f);
            nPosicionXPorcentaje = Double.parseDouble(sh.getString("watermark_x", "50"));
            nPosicionYPorcentaje = Double.parseDouble(sh.getString("watermark_y", "50"));
            nEscala = Double.parseDouble(sh.getString("watermark_scale", "0.5"));
            nOpacidadMarca = Integer.parseInt(sh.getString("watermark_opacity", "125"));
            seekBarTransparencia.setProgress(nOpacidadMarca);

            bLeerPosicionGuardada = true;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
        }

    }

    void definirMarcaConUri(Uri uri) {
        try {
            bmpMarcaElegida
                    = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bmpMarcaElegida = redimensionarAnchoBitmap(bmpMarcaElegida, nAnchoPredeterminado);
        calcularPosicionMarca();
    }

    void calcularPosicionMarca() {
//        nPosicionXMarca = 0;
//        nPosicionYMarca = 0;
//        mPosX = 0;
//        mPosY = 0;
//        mLastTouchX = 0;
//        mLastTouchY = 0;
        leerDatosMarca();
//        mPosX = nPosicionXMarca;
//        mPosY = nPosicionYMarca;
//        mLastTouchX = 0;
//        mLastTouchY = 0;

    }

    //    void recibirImagenEnviada(Intent intent) {
    void recibirImagenEnviada(String szImagen) {
        uriExterno = Uri.parse(szImagen);

    }


    private void dibujarConMarca() {
        //Levanto ancho y alto de pantalla

//        System.out.println(imgPrincipal.getWidth() + ", " + imgPrincipal.getHeight());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int nAltoPantalla = displayMetrics.heightPixels;
        int nAnchoPantalla = displayMetrics.widthPixels;

        //


        Resources res = getResources();
        //Marca placeholder por si no hay nada seleccionado
        Drawable drawableMarca;
        // Placeholder imagen por si no hay nada seleccionado
        Drawable drawablePlaceHolder;

        Bitmap bmpOriginal = null;
        if (bmpElegido == null) {
            drawablePlaceHolder = ResourcesCompat.getDrawable(res, R.drawable.placeholderimagen, null);
            if (drawablePlaceHolder != null) {
                bmpOriginal = ((BitmapDrawable) drawablePlaceHolder).getBitmap();
                bmpOriginal = redimensionarAnchoBitmap(bmpOriginal, nAnchoPredeterminado);
                //Guardo la imagen predeterminada como elegida para procesar afuera
                bmpElegido = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);
            }
        } else {
            bmpOriginal = bmpElegido.copy(Bitmap.Config.ARGB_8888, true);
        }

        //Pongo la imagen como est?? para que se me actualice alto y ancho de imageview
        imgPrincipal.setImageDrawable(new BitmapDrawable(getResources(), bmpOriginal));
//        Toast.makeText(getApplicationContext(),imgPrincipal.getAnchoReal() + ", " + imgPrincipal.getAltoReal(), Toast.LENGTH_LONG).show();
        //----

        // Convierto el Bitmap elegido en mutable
        //if (bmpOriginal != null) bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);

        // La marca de agua
        Bitmap bmpMarca = null;
        if (bmpMarcaElegida == null) {
            drawableMarca = ResourcesCompat.getDrawable(res, R.drawable.defaultwatermark, null);
            if (drawableMarca != null) bmpMarca = ((BitmapDrawable) drawableMarca).getBitmap();
            if (bmpMarca != null) {
                bmpMarca = redimensionarAnchoBitmap(bmpMarca, nAnchoPredeterminado);
                //Guardo la marca predeterminada como elegida para procesar afuera
                bmpMarcaElegida = bmpMarca.copy(Bitmap.Config.ARGB_8888, true);
            }
        } else {
            bmpMarca = bmpMarcaElegida;
        }
        //Ajusto la marca de agua seg??n la escala manual
        if (bmpMarca != null) {
            //bmpMarca = redimensionarAnchoBitmap(bmpMarca, (int) (nAnchoPredeterminado * nEscala));
            bmpMarca = redimensionarAnchoBitmap(bmpMarca,(int)(nAnchoDibujado * nEscala) );
        }

//        Bitmap bmpCanvas = bmpOriginal;
        // Creo un canvas con el bitmap mutable
        Canvas canvas = new Canvas(bmpOriginal);
        //Elijo el nivel de transparencia
        Paint paint = new Paint();
        paint.setAlpha(nOpacidadMarca);


        if (bmpMarca != null) {
            //Escalo el toque a las dimensiones de la imagen
//            float nPosicionXAjustada = 0f;
//            float nPosicionYAjustada = 0f;
            double nPosicionXAjustada = 0;
            double nPosicionYAjustada = 0;
            if (nPosicionXMarca != 0 | bLeerPosicionGuardada) {
//                nPosicionXAjustada = nPosicionXMarca / imgPrincipal.getWidth() *
//                        bmpOriginal.getWidth();

                //Si le?? la marca desde los datos guardados, la levanto como porcentaje
                if (bLeerPosicionGuardada) {
                    nPosicionXAjustada = nPosicionXPorcentaje * bmpOriginal.getWidth() / 100;
//                    nPosicionXMarca = nPosicionXAjustada * nAnchoPantalla / bmpOriginal.getWidth();
                    nPosicionXMarca = nPosicionXAjustada * nAnchoDibujado / bmpOriginal.getWidth();

//                    mPosX = nPosicionXMarca;
                    //mLastTouchX = nPosicionXMarca;
                } else {
//                    nPosicionXAjustada = nPosicionXMarca / nAnchoPantalla *
//                            bmpOriginal.getWidth();
                    nPosicionXAjustada = nPosicionXMarca / nAnchoDibujado *
                            bmpOriginal.getWidth();
                }
            } else {
                nPosicionXAjustada = 0;
            }
            if (nPosicionYMarca != 0 | bLeerPosicionGuardada) {
//                nPosicionYAjustada = nPosicionYMarca / imgPrincipal.getHeight() *
//                        bmpOriginal.getHeight();

                //Si le?? la marca desde los datos guardados, la levanto como porcentaje
                if (bLeerPosicionGuardada) {
//                    double nRelacionAjuste = nPosicionXAjustada / nPosicionXMarca;
                    nPosicionYAjustada = nPosicionYPorcentaje * bmpOriginal.getHeight() / 100;
                    double nRelacionXY = nPosicionYAjustada / nPosicionXAjustada;

                    //nPosicionYMarca = nPosicionXMarca * nRelacionXY;
                    nPosicionYMarca = nPosicionYAjustada * nAltoDibujado / bmpOriginal.getHeight();

                } else {

//                    double nRelacionResolucion = nPosicionXAjustada / nPosicionXMarca;
//                    nPosicionYAjustada = nPosicionYMarca * nRelacionResolucion;
                    nPosicionYAjustada = nPosicionYMarca / nAltoDibujado *
                            bmpOriginal.getHeight();
                }
            } else {
                nPosicionYAjustada = 0;
            }
            canvas.drawBitmap(bmpMarca, (float) nPosicionXAjustada, (float) nPosicionYAjustada, paint);
            if (bLeerPosicionGuardada) {
                bLeerPosicionGuardada = false;
            }

        }
        imgPrincipal.setImageDrawable(new BitmapDrawable(getResources(), bmpOriginal));
        //System.out.println(imgPrincipal.getHeight());
    }

    private void compartirImagenConTexto(Bitmap bitmap) {
        Uri uri = getmageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        // uri de la imagen a compartir
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // texto para compartir
        intent.putExtra(Intent.EXTRA_TEXT, "Shared via WaterShare");

        // Control para no compartir dos veces con la misma app
        intent.putExtra("watershare", true);

        // Asunto
        intent.putExtra(Intent.EXTRA_SUBJECT, "");

        // tipo imagen
        intent.setType("image/png");

        // compartir a trav??s de...
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(bDirectShare) bDirectShare = false;
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(bCerrarApp) finishAndRemoveTask();
//    }

    // Recuperando la uri para compartir
    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            boolean mkDirOk = imagefolder.mkdirs();
            //Tiro cualquier cosa para evitar un warning
            if (mkDirOk) System.out.println(" ");
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
        double scaleWidth = ((double) newWidth) / width;
        double scaleHeight = ((double) newHeight) / height;
        // Crear matrix para manipulaci??n
        Matrix matrix = new Matrix();
        // Cambiar tama??o de bitmap
        matrix.postScale((float) scaleWidth, (float) scaleHeight);
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


    //Salir del programa pulsando dos veces atr??s
    boolean bSalirPulsadoUnaVez = false;

    @Override
    public void onBackPressed() {
        if (bSalirPulsadoUnaVez) {
            super.onBackPressed();
            return;
        }

        this.bSalirPulsadoUnaVez = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                bSalirPulsadoUnaVez = false;
            }
        }, 2000);
    }

}
