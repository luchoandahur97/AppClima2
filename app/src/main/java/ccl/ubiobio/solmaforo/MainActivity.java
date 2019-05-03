package ccl.ubiobio.solmaforo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {
protected int cont=0;
private TextView maintemp, mainhum, mainrad, mainfecha;
private String fechaCurrent;
private String fecha;
protected String ultimaTemp="...", ultimaRadiacion="...", ultimaHumedad="...",strText;
private SharedPreferences leer;
private SharedPreferences.Editor guardar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btntemperatura = findViewById(R.id.temp);
        Button btnhumedad = findViewById(R.id.hum);
        Button btnradiacion = findViewById(R.id.rad);
        maintemp= findViewById(R.id.maintemp);
        mainhum=findViewById(R.id.mainhumedad);
        mainrad= findViewById(R.id.mainradiacion);
        mainfecha= findViewById(R.id.mainfecha);
        leer= getSharedPreferences("ccl.ubiobio.solmaforo",MODE_PRIVATE);
        guardar= leer.edit();
        //servicioLogin("luisandahur","lucho97");

        Calendar fechaTemperatura = new GregorianCalendar(TimeZone.getTimeZone("Chile/Continental"));
        int diaActual= fechaTemperatura.get(Calendar.DAY_OF_MONTH);
        int mesActual= fechaTemperatura.get(Calendar.MONTH)+1;
        int anioActual= fechaTemperatura.get(Calendar.YEAR);

        if(mesActual<10 && diaActual<10 ) {
            fechaCurrent="0"+diaActual+"0"+mesActual+anioActual;
            fecha="0"+diaActual+"/0"+mesActual+"/"+anioActual;
        }else{
            if(mesActual<10){
                fechaCurrent=diaActual+"0"+mesActual+anioActual;
                fecha=diaActual+"/0"+mesActual+"/"+anioActual;
            }else{
                if(diaActual<10){
                    fechaCurrent="0"+diaActual+mesActual+anioActual;
                    fecha="0"+diaActual+"/"+mesActual+"/"+anioActual;
                }else{
                    fechaCurrent = "" + diaActual + mesActual + anioActual;
                    fecha=diaActual+"/"+mesActual+"/"+anioActual;
                }
            }

        }
        temperaturaActual(fechaCurrent);
        radiacionActual(fechaCurrent);
        humedadActual(fechaCurrent);
        mainfecha.setText(fecha);
        btntemperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Temperatura;
                Temperatura = new Intent(MainActivity.this,Temperatura.class);
                startActivity(Temperatura);
                finish();

            }
        });
        btnhumedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Humedad;
                Humedad = new Intent(MainActivity.this,Humedad.class);
                startActivity(Humedad);
                finish();
            }
        });
        btnradiacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Radiacion;
                Radiacion = new Intent(MainActivity.this,Radiacion.class);
                startActivity(Radiacion);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed(){
        //Toast.makeText(this,"Quieres volver",Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.mensaje)
                .setTitle(R.string.titulo)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog dialogo = builder.create();
        dialogo.show();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        maintemp.setText(ultimaTemp);
        mainrad.setText(ultimaRadiacion);
        mainhum.setText(ultimaHumedad);
    }

    public void servicioLogin(final String nick, final String pass){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/user/login";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, WS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("LOG_WS", response);
                try {
                    JSONObject responseJson = new JSONObject(response);
                    Snackbar.make(MainActivity.this.getCurrentFocus(), responseJson.getString("info"), Snackbar.LENGTH_SHORT).setAction("Action",null).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
            }
        }){
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String,String>();
                params.put("pass",pass/*"12345"*/);
                params.put("nickname",nick/*"test123"*/);
                return params;
            }
        };
        requestQueue.add(request);

    }
    public void temperaturaActual(final String fecha){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"E1yGxKAcrg"+"/"+fecha;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, WS_URL, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("LOG_WS", response);
                try {
                    JSONArray responseJson = response.getJSONArray("data");
                    JSONObject actual = responseJson.getJSONObject(responseJson.length()-1);
                    String valorActual= actual.getString("valor");
                    guardar.putString("temperatura",valorActual);
                    guardar.commit();
                    maintemp.setText(valorActual+"°C");
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                maintemp.setText(leer.getString("temperatura","")+"°C");
            }
        });
        requestQueue.add(request);
    }

    public void radiacionActual(final String fecha){
        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"8IvrZCP3qa"+"/"+fecha;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, WS_URL, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("LOG_WS", response);
                try {
                    JSONArray responseJson = response.getJSONArray("data");
                    JSONObject actual = responseJson.getJSONObject(responseJson.length()-1);
                    String valorActual= actual.getString("valor");
                    guardar.putString("radiacion",valorActual);
                    guardar.commit();
                    mainrad.setText(valorActual);
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                mainrad.setText(leer.getString("radiacion",""));
            }
        });
        requestQueue.add(request);

    }
    public void humedadActual (String fecha){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"VIbSnGKyLW"+"/"+fecha;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, WS_URL, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("LOG_WS", response);
                try {
                    JSONArray responseJson = response.getJSONArray("data");
                    JSONObject actual = responseJson.getJSONObject(responseJson.length()-1);
                    String valorActual= actual.getString("valor");
                    guardar.putString("humedad",valorActual);
                    guardar.commit();
                    mainhum.setText(valorActual+"%");
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                mainhum.setText(leer.getString("humedad","")+"%");
            }
        });
        requestQueue.add(request);

    }
}
