package org.example.eventos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jamarfal on 10/2/17.
 */

public class EventosRecyclerAdapter extends FirebaseRecyclerAdapter<EventoItem,
        EventosRecyclerAdapter.EventoViewHolder> {

    public EventosRecyclerAdapter(int modelLayout,
                                  DatabaseReference ref) {
        super(EventoItem.class, modelLayout, EventosRecyclerAdapter.EventoViewHolder.class, ref);

    }

    @Override
    public EventoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);
        new EventoViewHolder(view);

        return new EventoViewHolder(view);
    }

    @Override
    protected void populateViewHolder(EventoViewHolder holder, EventoItem item, int position) {
        String txtEvento = item.getEvento();
        String txtCiudad = item.getCiudad();
        String txtFecha = item.getFecha();
        holder.txtEvento.setText(txtEvento);
        holder.txtCiudad.setText(txtCiudad);
        holder.txtFecha.setText(txtFecha);
        new DownloadImageTask((ImageView) holder.imagen)
                .execute(item.getImagen());
    }

    class EventoViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @BindView(R.id.txtEvento)
        TextView txtEvento;
        @BindView(R.id.txtCiudad)
        TextView txtCiudad;
        @BindView(R.id.txtFecha)
        TextView txtFecha;
        @BindView(R.id.imgImagen)
        ImageView imagen;

        public EventoViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            EventoItem currentItem = (EventoItem) getItem(position);
            Context context = EventosAplicacion.getAppContext();
            Intent intent = new Intent(context, EventoDetallesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("evento", currentItem.getId());
            context.startActivity(intent);
        }
    }

    private class DownloadImageTask
            extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mImagen = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mImagen = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mImagen;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
