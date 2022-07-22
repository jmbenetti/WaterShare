package com.jmbenetti.watershare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityShareNow extends AppCompatActivity {
    public static String szUriRecibida = "";
    public static Activity instancia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_now);

        instancia = this;

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String accion = intent.getAction();
        String type = intent.getType();
        boolean bDobleCompartido = intent.getBooleanExtra("watershare", false);

        if (bDobleCompartido) {
//            szUriRecibida = "";
            Toast.makeText(getApplicationContext(),
                    "You have already watermarked this. Now choose another app to share with.", Toast.LENGTH_LONG).show();
//            finishAndRemoveTask();
        }

        if (Intent.ACTION_SEND.equals(accion) && type != null) {
            if (type.startsWith("image/")) {
                Intent intentMain = new Intent(this, MainActivity.class);
                intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                if (!bDobleCompartido) {
                    Uri uriImagen = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    this.szUriRecibida = uriImagen.toString();
//                }
                startActivity(intentMain);
                if(bDobleCompartido) finishAndRemoveTask();
            }
        }
    }


}