package com.example.caloriaspucp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ComidaAdapter extends RecyclerView.Adapter<ComidaAdapter.ComidaViewHolder> {
    private List<Comida> listaComidas;
    private Context context;

    public ComidaAdapter(List<Comida> listaComidas) {
        this.listaComidas = listaComidas;
    }

    @NonNull
    @Override
    public ComidaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comida, parent, false);
        return new ComidaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComidaViewHolder holder, int position) {
        Comida comida = listaComidas.get(position);

        TextView textViewnombreComida = holder.itemView.findViewById(R.id.nombreComida);
        textViewnombreComida.setText(comida.getNombre());
        TextView textViewCalorias = holder.itemView.findViewById(R.id.caloriasComida);
        String texto = String.valueOf(comida.getCalorias()) + " cal";
        textViewCalorias.setText(texto);
    }

    @Override
    public int getItemCount() {
        return listaComidas.size();
    }

    public static class ComidaViewHolder extends RecyclerView.ViewHolder {
        Comida comida;

        public ComidaViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
