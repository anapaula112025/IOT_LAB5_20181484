package com.example.caloriaspucp;

public class Actividad {
    private String nombre;
    private double calorias;

    public Actividad(String nombre, double calorias) {
        this.nombre = nombre;
        this.calorias = calorias;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCalorias() {
        return calorias;
    }
}
