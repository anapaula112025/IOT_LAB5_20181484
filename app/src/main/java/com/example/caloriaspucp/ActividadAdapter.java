package com.example.caloriaspucp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>{
    private List<Actividad> listaActividades;
    private Context context;

    public ActividadAdapter(List<Actividad> listaComidas) {
        this.listaActividades = listaComidas;
    }

    @NonNull
    @Override
    public ActividadAdapter.ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
        return new ActividadAdapter.ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadAdapter.ActividadViewHolder holder, int position) {
        Actividad actividad = listaActividades.get(position);

        TextView textViewnombreActividad = holder.itemView.findViewById(R.id.nombreActividad);
        textViewnombreActividad.setText(actividad.getNombre());
        TextView textViewCalorias = holder.itemView.findViewById(R.id.caloriasActividad);
        String texto = String.valueOf(actividad.getCalorias()) + " cal";
        textViewCalorias.setText(texto);
    }

    @Override
    public int getItemCount() {
        return listaActividades.size();
    }

    public static class ActividadViewHolder extends RecyclerView.ViewHolder {
        Actividad actividad;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
