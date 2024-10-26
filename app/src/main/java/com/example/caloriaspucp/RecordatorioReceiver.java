package com.example.caloriaspucp;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

//Se crea esta clase que extiende de BroadcastReceiver, la cual se activará cuando el AlarmManager dispare la alarma.

public class RecordatorioReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String mensaje = intent.getStringExtra("mensaje");  // Obtiene el mensaje desde el Intent

        String titulo = "Recordatorio de Comida";
        String texto = mensaje;

        // Comprobar si el mensaje es para el recordatorio de comidas no registradas
        if ("No registraste ninguna comida hoy. ¡Recuerda hacerlo!".equals(mensaje)) {
            SharedPreferences prefs = context.getSharedPreferences("calorias", Context.MODE_PRIVATE);
            double totalCalorias = prefs.getFloat("totalCalorias", 0);

            if (totalCalorias > 0) {
                return;  // Si el usuario ha registrado comidas, no mostrar nada
            }

            texto = "No registraste ninguna comida hoy. ¡Recuerda hacerlo!";
        }else if("¡Sigue adelante! Recuerda tu objetivo siempre!".equals(mensaje)){

            titulo = "Motivación";
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channelRecordatorio")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }


    }
}
