package org.example.eventos;

/**
 * Created by jamarfal on 10/2/17.
 */

public class EventoItem {

    private String id;
    private String evento;
    private String ciudad;
    private String fecha;
    private String imagen;

    public EventoItem() {
    }

    public EventoItem(String id, String evento, String ciudad,
                      String fecha, String imagen) {
        this.id = id;
        this.evento = evento;
        this.ciudad = ciudad;
        this.fecha = fecha;
        this.imagen = imagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
