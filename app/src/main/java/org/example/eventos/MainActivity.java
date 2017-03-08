package org.example.eventos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.example.eventos.EventosAplicacion.PLAY_SERVICES_RESOLUTION_REQUEST;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.
        OnConnectionFailedListener {

    @BindView(R.id.reciclerViewEventos)
    RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter adapter;
    private String[] permissions;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!comprobarGooglePlayServices()) {
            Toast.makeText(this, "Error Google Play Services: no está instalado o no es válido.", Toast.LENGTH_LONG).show();
            finish();
        }

        ButterKnife.bind(this);

        EventosAplicacion app = (EventosAplicacion) getApplicationContext();

        databaseReference = app.getItemsReference();
        initializeRecyclerView();

        initializeToolbar();


        subscribeToAllThemeForPushNotification();
        permissions = new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.GET_ACCOUNTS,
                android.Manifest.permission.ACCESS_NETWORK_STATE
        };
        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppInvite.API)
                .enableAutoManage(this, this).build();

        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);
                                    android.net.Uri url = Uri.parse(deepLink);
                                    String descuento = url.getQueryParameter("descuento");
                                    EventosAplicacion.mostrarDialogo(getApplicationContext(), "Tienes un descuento del "
                                            + descuento + "% gracias a la invitación: " + invitationId);
                                }
                            }
                        });
    }

    private void subscribeToAllThemeForPushNotification() {
        final SharedPreferences preferencias = getApplicationContext().getSharedPreferences("Temas",
                Context.MODE_PRIVATE);
        if (!preferencias.getBoolean("Inicializado", false)) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    "Temas", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Inicializado", true);
            editor.commit();
            FirebaseMessaging.getInstance().subscribeToTopic("Todos");
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        current = this;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.keySet().size() > 4) {
            String evento = extras.getString("evento");
            String dia = extras.getString("dia");
            String ciudad = extras.getString("ciudad");
            String comentario = extras.getString("comentario");
            EventosAplicacion.mostrarDialogo(
                    getApplicationContext(),
                    EventosAplicacion.getFormattedEventInfo(evento, dia, ciudad, comentario),
                    evento
            );
            for (String key : extras.keySet()) {
                getIntent().removeExtra(key);
            }
            extras = null;
        }
    }

    private void initializeRecyclerView() {
        adapter = new EventosRecyclerAdapter(R.layout.evento,
                databaseReference);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private boolean comprobarGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private static MainActivity current;

    public static MainActivity getCurrentContext() {
        return current;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_temas:
                intent = new Intent(getBaseContext(), TemasActivity.class);
                startActivity(intent);
                break;
            case R.id.action_share_photo:
                intent = new Intent(this, RetoEstoyAquiActivity.class);
                startActivity(intent);
                break;
            case R.id.action_invitar:
                invitar();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                /*if (!(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this,
                            "Permiso denegado para mantener escribir en el almacenamiento.", Toast.LENGTH_SHORT).show();
                }*/

                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            Toast.makeText(MainActivity.this,
                                    "Algunos de los permisos no han sido concedidos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }
                return;
            }
            case 3: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this,
                            "Permiso denegado para acceder a las cuentas", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
    }

    private void invitar() {
        Intent intent = new
                AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title)).setMessage(getString(R.string.invitation_message)).setDeepLink(Uri.parse(getString(R.string.invitation_deep_link))).setCustomImage(Uri.parse(getString(R.string.invitation_custom_image))).setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
                Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
            }
        }
    }
//
//    protected void onPostResume() {
//        super.onPostResume();
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//        FirebaseRemoteConfig.setDefaults(R.xml.remote_config_default);
//        long cacheExpiration = 60;
//        mFirebaseRemoteConfig.fetch(cacheExpiration)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        mFirebaseRemoteConfig.activateFetched();
//                        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
//                        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
//                        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
//                    }
//                });
//    }
}
