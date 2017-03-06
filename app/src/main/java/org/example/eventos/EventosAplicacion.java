package org.example.eventos;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jamarfal on 10/2/17.
 */

public class EventosAplicacion extends Application {
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String ITEMS_CHILD_NAME = "eventos";
    private static DatabaseReference eventosReference;
    private static Context context;
    static final String URL_SERVIDOR = "http://cursoandroid.hol.es/notificaciones/";
    static String ID_PROYECTO = "eventos-ff21b";
    String idRegistro = "";
    private FirebaseStorage storage;
    private static StorageReference storageRef;
    static FirebaseRemoteConfig mFirebaseRemoteConfig;
    static String colorFondo;
    static Boolean acercaDe;

    @Override
    public void onCreate() {
        super.onCreate();
        EventosAplicacion.context = getApplicationContext();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        eventosReference = database.getReference(ITEMS_CHILD_NAME);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://eventos-ff21b.appspot.com");

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings
                        .Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig
                .setDefaults(R.xml.remote_config_default);

        long cacheExpiration = 100;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        getColorFondo();
                        getAcercaDe();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
                        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
                    }
                });

    }



    public static Context getAppContext() {
        return EventosAplicacion.context;
    }

    public static DatabaseReference getItemsReference() {
        return eventosReference;
    }

    static void mostrarDialogo(final Context context, String mensaje, String evento) {
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("evento", evento);
        context.startActivity(intent);
    }

    static void mostrarDialogo(final Context context, String mensaje) {
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        context.startActivity(intent);
    }

    public static class registrarDispositivoEnServidorWebTask
            extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "registrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection)
                        direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new
                        OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
            if (res == "ok") {
                guardarIdRegistroPreferencias(contexto, idRegistroTarea);
            }
        }
    }

    public static void guardarIdRegistroPreferencias(Context context, String idRegistro) {
        final SharedPreferences prefs = context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("idRegistro", idRegistro);
        editor.commit();
    }

    public static String dameIdRegistroPreferencias(Context context) {
        final SharedPreferences preferencias =
                context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        String idRegistro = preferencias.getString("idRegistro", "");
        return idRegistro;
    }

    public static void guardarIdRegistro(Context context, String idRegistro) {
        registrarDispositivoEnServidorWebTask tarea =
                new registrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = idRegistro;
        tarea.execute();
    }

    public static void eliminarIdRegistro(Context context) {
        desregistrarDispositivoEnServidorWebTask tarea =
                new desregistrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = dameIdRegistroPreferencias(context);
        tarea.execute();

    }

    public static class desregistrarDispositivoEnServidorWebTask
            extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "desregistrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection)
                        direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new
                        OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
            if (res == "ok") {
                guardarIdRegistroPreferencias(contexto, "");
            }
        }
    }

    public static StorageReference getStorageReference() {
        return storageRef;
    }

    public static String getFormattedEventInfo(String event, String day, String city, String comment) {
        String eventFormattedInfo = "";
        eventFormattedInfo = "Evento: " + event + "\n";
        eventFormattedInfo = eventFormattedInfo + "Día: " + day + "\n";
        eventFormattedInfo = eventFormattedInfo + "Ciudad: " + city + "\n";
        eventFormattedInfo = eventFormattedInfo + "Comentario: " + comment;
        return eventFormattedInfo;
    }

    private void getColorFondo() {
        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
    }

    private void getAcercaDe() {
        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
    }
}
