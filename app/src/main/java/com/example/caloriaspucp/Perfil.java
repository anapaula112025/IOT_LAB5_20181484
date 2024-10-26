package com.example.caloriaspucp;

import java.io.Serializable;

public class Perfil implements Serializable {
    private String peso;
    private String altura;
    private String edad;
    private String genero;
    private String nivelActividad;
    private String objetivo;

    // Constructor
    public Perfil(String peso, String altura, String edad, String genero, String nivelActividad, String objetivo) {
        this.peso = peso;
        this.altura = altura;
        this.edad = edad;
        this.genero = genero;
        this.nivelActividad = nivelActividad;
        this.objetivo = objetivo;
    }

    public String getPeso() { return peso; }
    public String getAltura() { return altura; }
    public String getEdad() { return edad; }
    public String getGenero() { return genero; }
    public String getNivelActividad() { return nivelActividad; }
    public String getObjetivo() { return objetivo; }
}
