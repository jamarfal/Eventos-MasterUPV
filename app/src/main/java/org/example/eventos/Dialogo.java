package org.example.eventos;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by jamarfal on 10/2/17.
 */

public class Dialogo extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra("mensaje")) {

            String mensaje = extras.getString("mensaje");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Mensaje:");
            alertDialog.setMessage(mensaje);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CERRAR",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (getIntent().hasExtra("evento")) {
                                String evento = extras.getString("evento");
                                goToEventsDetails(evento);
                            }

                            finish();
                        }
                    });
            alertDialog.show();
            extras.remove("mensaje");
        }
    }

    private void goToEventsDetails(String evento) {
        Intent intent = new Intent(this, EventoDetallesActivity.class);
        intent.putExtra("evento", evento);
        startActivity(intent);

    }
}
