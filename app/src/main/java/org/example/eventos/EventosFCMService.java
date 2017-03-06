package org.example.eventos;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by jamarfal on 10/2/17.
 */

public class EventosFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String evento = remoteMessage.getData().get("evento");
            String dia = remoteMessage.getData().get("dia");
            String ciudad = remoteMessage.getData().get("ciudad");
            String comentario = remoteMessage.getData().get("comentario");
            EventosAplicacion.mostrarDialogo(getApplicationContext(), EventosAplicacion.getFormattedEventInfo(evento, dia, ciudad, comentario), evento);
        } else {
            if (remoteMessage.getNotification() != null) {
                EventosAplicacion.mostrarDialogo(getApplicationContext(), remoteMessage.getNotification().getBody());
            }
        }
    }
}
