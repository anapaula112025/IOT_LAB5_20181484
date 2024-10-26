package com.example.caloriaspucp;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    DecimalFormat df = new DecimalFormat("#.00");

    EditText nombreComidaInput;
    EditText caloriasComidaInput;
    EditText nombreActividadInput;
    EditText caloriasActividadInput;
    Button btnAddFood;
    TextView tvTotalCalorias;
    TextView tvCaloriasRecomendadas;
    TextView tvCaloriasRestantes;
    private RecyclerView recyclerViewComidas;
    private RecyclerView recyclerViewActividades;
    private ComidaAdapter comidaAdapter;
    private ActividadAdapter actividadAdapter;
    private List<Comida> listaComidas;
    private List<Actividad> listaActividades;
    double totalCaloriasConsDia = 0;
    double caloriasRecomendadas;

    String channelId = "channelExcesoComida";
    String channelId2 = "channelRecordatorio";

    // Definir el catálogo de alimentos comunes
    private List<Comida> catalogoAlimentos = new ArrayList<>();

    public void catalogoAlimentos() {
        catalogoAlimentos.add(new Comida("Manzana", 52));
        catalogoAlimentos.add(new Comida("Platano", 89));
        catalogoAlimentos.add(new Comida("Pan Integral", 79));
        catalogoAlimentos.add(new Comida("Arroz Cocido (100g)", 130));
        catalogoAlimentos.add(new Comida("Pollo a la Parrilla (100g)", 165));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Inicializar catálogo de alimentos
        catalogoAlimentos();
        // Configuración del Spinner
        Spinner spinnerCatalogoAlimentos = findViewById(R.id.spinnerCatalogoAlimentos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                obtenerNombresAlimentos()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCatalogoAlimentos.setAdapter(adapter);

        // Configurar la selección de un alimento: sugerencia de chatgpt
        spinnerCatalogoAlimentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Comida comidaSeleccionada = catalogoAlimentos.get(position);
                nombreComidaInput.setText(comidaSeleccionada.getNombre());
                caloriasComidaInput.setText(String.valueOf(comidaSeleccionada.getCalorias()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        createNotificationExcesoChannel();
        createNotificationRecordatorioChannel();

        // Creacion de los recordatorios: recomendacion por chatgpt
        programarRecordatorio(8, 0, "No olvides registrar tu desayuno");
        programarRecordatorio(13, 0, "No olvides registrar tu almuerzo");
        programarRecordatorio(20, 0, "No olvides registrar tu cena");
        programarRecordatorio(22, 0, "No registraste ninguna comida hoy. ¡Recuerda hacerlo!");

        // Obtener los datos
        nombreComidaInput = findViewById(R.id.nombreComidaInput);
        caloriasComidaInput = findViewById(R.id.caloriasInput);
        nombreActividadInput = findViewById(R.id.nombreActividadInput);
        caloriasActividadInput = findViewById(R.id.caloriasActividadInput);
        btnAddFood = findViewById(R.id.btnAddComida);
        tvTotalCalorias = findViewById(R.id.tvTotalCalorias);
        tvCaloriasRecomendadas = findViewById(R.id.tvCaloriasRecomendadas);
        recyclerViewComidas = findViewById(R.id.recyclerViewComidasAgregadas);
        recyclerViewActividades = findViewById(R.id.recyclerViewActividadesAgregadas);
        tvCaloriasRestantes = findViewById(R.id.tvCaloriasRestantes);

        // Configurar RecyclerView1
        listaComidas = new ArrayList<>();
        comidaAdapter = new ComidaAdapter(listaComidas);
        recyclerViewComidas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComidas.setAdapter(comidaAdapter);

        // Configurar RecyclerView2
        listaActividades = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(listaActividades);
        recyclerViewActividades.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewActividades.setAdapter(actividadAdapter);

        // Recuperar las calorías recomendadas del Intent
        caloriasRecomendadas = getIntent().getDoubleExtra("CALORIAS_RECOMENDADAS", 2000);
        String texto1 = "Calorías recomendadas: " + df.format(caloriasRecomendadas)+ " cal";
        String texto4 = "Calorías que faltan consumir: " + df.format(caloriasRecomendadas)+ " cal";
        tvCaloriasRecomendadas.setText(texto1);
        tvCaloriasRestantes.setText(texto4);

        //sumar calorias para obtener el total de calorias al dia
        btnAddFood.setOnClickListener(v -> agregarComida());

        if(listaComidas.isEmpty()){
            // Guardar total de calorías en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("calorias", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("totalCalorias", (float) totalCaloriasConsDia);
            editor.apply();
        }

        //Para la notificación de objetivo
        EditText intervaloMinutosInput = findViewById(R.id.intervaloMinutosInput);
        Button btnStartMotivacion = findViewById(R.id.btnStartMotivacion);

        btnStartMotivacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intervaloMinutos = intervaloMinutosInput.getText().toString();
                if (!intervaloMinutos.isEmpty()) {
                    int intervaloMinutosInt = Integer.parseInt(intervaloMinutos);
                    programarNotificacionMotivacion(intervaloMinutosInt);
                }
            }
        });

        // Configurar el botón para agregar actividades físicas
        Button btnAddActividad = findViewById(R.id.btnAddActividad);
        btnAddActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarActividad();
            }
        });



    }

    public void agregarComida() {

        // Obtener datos ingresados
        String nombre = nombreComidaInput.getText().toString();

        if(nombre.isEmpty() || (caloriasComidaInput.getText().toString()).isEmpty()){
            Toast.makeText(this, "Completar campos vacios", Toast.LENGTH_LONG).show();
        }else{
            double calorias = Double.parseDouble(caloriasComidaInput.getText().toString());

            // Agregar comida a la lista y actualizar el total de calorías
            Comida comida = new Comida(nombre, calorias);
            listaComidas.add(comida);
            comidaAdapter.notifyItemInserted(listaComidas.size() - 1);

            // Sumar calorías y actualizar el TextView
            totalCaloriasConsDia += calorias;
            String texto = "Total de calorías consumidas: " + df.format(totalCaloriasConsDia) + " cal";
            tvTotalCalorias.setText(texto);

            // Actualizar las calorías restantes
            actualizarCaloriasRestantes();

            // Guardar total de calorías en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("calorias", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("totalCalorias", (float) totalCaloriasConsDia);
            editor.apply();

            // notificar si el usuario ha excedido las calorías recomendadas
            notificarExcesoCalorias();

            // Limpiar campos de entrada
            nombreComidaInput.setText("");
            caloriasComidaInput.setText("");
        }

    }

    public void createNotificationExcesoChannel() {
        //android.os.Build.VERSION_CODES.O == 26
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Alerta de exceso de calorías",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones cuando se exceden las calorías recomendadas");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            askPermission();
        }
    }

    public void createNotificationRecordatorioChannel() {
        //android.os.Build.VERSION_CODES.O == 26
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId2,
                    "Recordatorio de comidas",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones para recordar registrar sus comidas en ciertos horarios");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            askPermission();
        }
    }

    public void askPermission(){
        //android.os.Build.VERSION_CODES.TIRAMISU == 33
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity2.this,
                    new String[]{POST_NOTIFICATIONS},
                    101);
        }
    }

    private void notificarExcesoCalorias() {
        if (totalCaloriasConsDia > caloriasRecomendadas) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.baseline_warning_24)
                    .setContentTitle("¡Exceso de Calorías!")
                    .setContentText("Has consumido más calorías de las recomendadas. Prueba hacer ejercicio o reducir las calorías en tu próxima comida :).")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            // Enviar la notificación
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1, builder.build());
            }
        }
    }

    //Método recomendado por chatgpt (Utilizar AlarmManager en Android para programar notificaciones a horas específicas)
    public void programarRecordatorio(int hora, int minuto, String mensaje) {

        // Verificar si se tiene permiso para programar alarmas exactas en Android 12 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Redirigir al usuario a la configuración para otorgar permiso de alarmas exactas
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Salir del método hasta que el usuario otorgue el permiso
            }
        }

        // Configuracion de la hora de la alarma
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        // Crear un intent para el BroadcastReceiver
        Intent intent = new Intent(this, RecordatorioReceiver.class);
        intent.putExtra("mensaje", mensaje);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                hora * 100 + minuto,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        // Configurar el AlarmManager para disparar la alarma en la hora especificada
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    //Método recomendado por chatgpt
    public void programarNotificacionMotivacion(int intervaloMinutos) {
        // Configurar el tiempo de repetición en milisegundos
        long intervaloMilisegundos = intervaloMinutos * 60 * 1000L;

        Intent intent = new Intent(this, RecordatorioReceiver.class);
        intent.putExtra("mensaje", "¡Sigue adelante! Recuerda tu objetivo siempre!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                2000,  // Identificador único para esta alarma
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configurar el AlarmManager para repetirse cada intervalo especificado
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Cancelar cualquier alarma anterior con el mismo PendingIntent
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);

            // Configurar una nueva alarma repetitiva con el nuevo intervalo
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervaloMilisegundos,
                    intervaloMilisegundos,
                    pendingIntent
            );
        }

        // Mostrar un Toast para indicar al usuario que la notificación de motivación se ha configurado
        Toast.makeText(this, "Notificación de motivación configurada cada " + intervaloMinutos + " minutos.", Toast.LENGTH_LONG).show();
    }

    private void actualizarCaloriasRestantes() {
        // Calcula calorías restantes
        double caloriasRestantes = caloriasRecomendadas - totalCaloriasConsDia;

        // Verificar si el usuario ha superado o alcanzado su meta
        if (caloriasRestantes > 0) {
            String texto2 = "Calorías que faltan consumir: " + df.format(caloriasRestantes) + " cal";
            tvCaloriasRestantes.setText(texto2);
        } else {
            String texto2 = "¡Meta superada! Has excedido tu objetivo diario por " + df.format(Math.abs(caloriasRestantes)) + " calorías";
            tvCaloriasRestantes.setText(texto2);
        }
    }

    private List<String> obtenerNombresAlimentos() {
        List<String> nombresAlimentos = new ArrayList<>();
        for (Comida comida : catalogoAlimentos) {
            nombresAlimentos.add(comida.getNombre() + " " +comida.getCalorias()+" " + "cal");
        }
        return nombresAlimentos;
    }

    private void agregarActividad() {

        // Obtener los datos ingresados
        String nombreActividad = nombreActividadInput.getText().toString();

        if(nombreActividad.isEmpty() || (caloriasActividadInput.getText().toString()).isEmpty()){
            Toast.makeText(this, "Completar campos vacios", Toast.LENGTH_LONG).show();
        }
        else{
            double caloriasActividad = Double.parseDouble(caloriasActividadInput.getText().toString());

            totalCaloriasConsDia -= caloriasActividad;// Restar las calorías de la actividad del total diario

            // Agregar comida a la lista y actualizar el total de calorías
            Actividad actividad = new Actividad(nombreActividad, caloriasActividad);
            listaActividades.add(actividad);
            actividadAdapter.notifyItemInserted(listaActividades.size() - 1);

            // Actualizar el TextView del total de calorías consumidas
            TextView tvTotalCalorias = findViewById(R.id.tvTotalCalorias);
            String texto3 = "Total de calorías consumidas: " + df.format(totalCaloriasConsDia) + " cal";
            tvTotalCalorias.setText(texto3);

            // Actualizar las calorías restantes
            actualizarCaloriasRestantes();

            // Limpiar los campos de entrada
            nombreActividadInput.setText("");
            caloriasActividadInput.setText("");
        }
    }


}
