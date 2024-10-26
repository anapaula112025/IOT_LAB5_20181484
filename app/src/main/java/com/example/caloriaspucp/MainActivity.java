package com.example.caloriaspucp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {

    EditText pesoInput;
    EditText alturaInput;
    EditText edadInput;
    Spinner generoSpinner;
    Spinner nivelSpinner;
    Spinner objectiveSpinner;
    Button calculateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Obtener los datos del formulario
        pesoInput = findViewById(R.id.weightInput);
        alturaInput = findViewById(R.id.heightInput);
        edadInput = findViewById(R.id.ageInput);
        generoSpinner = findViewById(R.id.genderGroup);
        nivelSpinner = findViewById(R.id.activityLevel);
        objectiveSpinner = findViewById(R.id.objectiveSpinner);
        calculateBtn = findViewById(R.id.calculateBtn);

        // Cargar el perfil guardado, si existe
        Perfil perfil = leerPerfilObjeto();
        if (perfil != null) {
            pesoInput.setText(perfil.getPeso());
            alturaInput.setText(perfil.getAltura());
            edadInput.setText(perfil.getEdad());
            setSpinnerValue(generoSpinner, perfil.getGenero());
            setSpinnerValue(nivelSpinner, perfil.getNivelActividad());
            setSpinnerValue(objectiveSpinner, perfil.getObjetivo());
        }

        // Al hacer clic en el botón de empezar
        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtiene los valores de los campos
                double peso = Double.parseDouble(pesoInput.getText().toString());
                double altura = Double.parseDouble(alturaInput.getText().toString());
                int edad = Integer.parseInt(edadInput.getText().toString());
                String genero = generoSpinner.getSelectedItem().toString();
                String nivelActividad = objectiveSpinner.getSelectedItem().toString();
                String objetivo = objectiveSpinner.getSelectedItem().toString();

                Perfil perfil = new Perfil(pesoInput.getText().toString(), alturaInput.getText().toString(), edadInput.getText().toString(), genero, nivelActividad, objetivo);
                guardarPerfil(perfil);

                double tmb = calcularTMB(peso, altura, edad, genero);
                double gastoCaloricoDiario = calcularGastoCaloricoDiario(tmb, nivelActividad);
                double caloriasObjetivo = calcularCaloriasObjetivo(gastoCaloricoDiario, objetivo);
                // Crear el Intent para abrir la otra actividad y pasar las calorías recomendadas
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("CALORIAS_RECOMENDADAS", caloriasObjetivo); // Pasar el valor
                startActivity(intent);

            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Fórmula de Harris-Benedict
    public double calcularTMB(double peso, double altura, int edad, String genero) {
        if (genero.equals("Masculino")) {
            return (10 * peso) + (6.25 * altura) - (5 * edad) + 5;
        } else {
            return (10 * peso) + (6.25 * altura) - (5 * edad) - 161;
        }
    }

    public double calcularGastoCaloricoDiario(double tmb, String nivelActividad) {
        double factor = 0;
        if (nivelActividad.equals("Poco o ningún ejercicio")){
            factor = 1.2;
        } else if (nivelActividad.equals("Ejercicio ligero")){
            factor = 1.375;
        } else if (nivelActividad.equals("Ejercicio moderado")){
            factor = 1.55;
        } else if (nivelActividad.equals("Ejercicio fuerte")) {
            factor = 1.725;
        } else{
            factor = 1.9;
        }
        return tmb * factor;

    }

    public double calcularCaloriasObjetivo(double gastoCaloricoDiario, String objetivo) {

        if (objetivo.equals("Subir Peso")){
            return gastoCaloricoDiario + 500;
        } else if (objetivo.equals("Bajar Peso")){
            return gastoCaloricoDiario - 300;
        } else{
            return gastoCaloricoDiario;
        }
    }

    public void guardarPerfil(Perfil perfil) {
        String filename = "perfilObjeto";

        try (FileOutputStream fileOutputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);
             ObjectOutputStream  objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(perfil);
            Toast.makeText(this, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el perfil", Toast.LENGTH_SHORT).show();
        }
    }

    public Perfil leerPerfilObjeto() {
        String filename = "perfilObjeto";

        try (FileInputStream fileInputStream = openFileInput(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return (Perfil) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se encontraron datos de perfil guardados", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //Método sugerido por chatgpt
    public void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

}