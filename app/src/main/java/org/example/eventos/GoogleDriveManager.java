package org.example.eventos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by jamarfal on 20/2/17.
 */

public class GoogleDriveManager {
    private Handler carga = new Handler();
    private ProgressDialog dialogo;
    Drive servicio = null;
    GoogleAccountCredential credencial = null;
    String nombreCuenta = null;
    private Activity activity;
    public static final int SOLICITUD_SELECCION_CUENTA = 1;

    public GoogleDriveManager(Activity activity) {
        this.activity = activity;
        credencial = GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(DriveScopes.DRIVE));
        SharedPreferences prefs = activity.getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        nombreCuenta = prefs.getString("nombreCuenta", null);
    }

    public boolean hasSettedGoogleAccount() {
        return nombreCuenta != null;
    }

    public void PedirCredenciales() {
        if (nombreCuenta == null) {
            activity.startActivityForResult(credencial.newChooseAccountIntent(), SOLICITUD_SELECCION_CUENTA);
        }
    }
    
}
