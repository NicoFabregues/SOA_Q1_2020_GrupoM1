package com.example.app.models;

import android.content.Intent;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPServiceLogin extends HTTPService{

    // Nombre del thread usado para debugging
    private static final String class_name = HTTPServiceLogin.class.getSimpleName();
    private static final String ENDPOINT = "/api/api/login";
    private static final String TYPE_EVENTS = "Login usuario";
    private static final String EVENT_DESCRIPTION = "Se registra en el servidor un login de usuario";
    private static final String TIPO_METRICA = "Cantidad de inicios de sesion";
    private Intent intentServiceRegistrarEvento;

    public HTTPServiceLogin() {
        super(class_name);
    }

    private void startHTTPServiceRegistrarEvento() {
        Evento evento = new Evento(TYPE_EVENTS, EVENT_DESCRIPTION);
        JSONObject req = evento.getJSONForRergistrarEvento();
        this.intentServiceRegistrarEvento = new Intent(this, HTTPServiceRegistrarEvento.class);
        this.intentServiceRegistrarEvento.putExtra("token", token);
        this.intentServiceRegistrarEvento.putExtra("jsonObject", req.toString());
        startService(this.intentServiceRegistrarEvento);
    }

    protected void updateOrCreateMetrica() {
        super.updateOrCreateMetrica(TIPO_METRICA);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(connectionManager.hayConexion()) {
            try {
                if (intent.hasExtra("jsonObject")) {
                    request = new JSONObject(intent.getStringExtra("jsonObject"));
                }
                POST(request);
                if (exception != null) {
                    Intent i = new Intent("com.example.intentservice.intent.action.LOGIN_RESPONSE");
                    i.putExtra("success", success);
                    i.putExtra("mensaje", "Error en envio de request");
                    //Se envian los valores al bradcast reciever del presenter de login
                    sendBroadcast(i);
                }
                else if (!success) {
                    Intent i = new Intent("com.example.intentservice.intent.action.LOGIN_RESPONSE");
                    i.putExtra("success", success);
                    i.putExtra("mensaje", "Usuario o contraseña incorrectos");
                    //Se envian los valores al bradcast reciever del presenter de login
                    sendBroadcast(i);
                }
                else {
                    token = response.getString("token");
                    refreshToken = response.getString("token_refresh");
                    updateOrCreateMetrica();
                    startHTTPServiceRegistrarEvento();
                    Intent i = new Intent("com.example.intentservice.intent.action.LOGIN_RESPONSE");
                    i.putExtra("success", success);
                    i.putExtra("mensaje", "Login exitoso");
                    i.putExtra("token", token);
                    i.putExtra("refresh_token", refreshToken);
                    //Se envian los valores al bradcast reciever del presenter de login
                    sendBroadcast(i);
                }
                stopSelf();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Intent i = new Intent("com.example.intentservice.intent.action.LOGIN_RESPONSE");
            i.putExtra("success", success);
            i.putExtra("mensaje", "No hay  conexión a Internet");
            //Se envian los valores al bradcast reciever del presenter de login
            sendBroadcast(i);
        }
    }

    protected String getUrl() {
        return super.getUrl() + ENDPOINT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(intentServiceRegistrarEvento != null)
            stopService(intentServiceRegistrarEvento);
    }
}
