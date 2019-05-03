package ccl.ubiobio.solmaforo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.android.volley.toolbox.Volley;
import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.github.anastr.speedviewlib.SpeedView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Temperatura extends AppCompatActivity {
private TextView tempActual;
private String fechaCurrent;
private TextView tempPromedio;
private HalfGauge indicador;
private SpeedView gaugeTemperatura;
private SpeedView gaugeTemperaturaProm;
private SharedPreferences leer;
private SharedPreferences.Editor guardar;
// Ocupar el primer gauge con colores amarillo, naranjo y rojo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperatura);
        /*tempActual = findViewById(R.id.actualTemp);
        tempPromedio= findViewById(R.id.promTemp);*/
        Button btnhumedad = findViewById(R.id.hum);
        Button btnradiacion = findViewById(R.id.rad);
        Button btnvolver = findViewById(R.id.volver);
        Button btnactualizar = findViewById(R.id.actualizar);
        gaugeTemperatura= findViewById(R.id.gaugeTemp);
        gaugeTemperaturaProm= findViewById(R.id.gaugeTempProm);
        leer= getSharedPreferences("ccl.ubiobio.solmaforo",MODE_PRIVATE);
        guardar= leer.edit();

        Calendar fechaTemperatura = new GregorianCalendar(TimeZone.getTimeZone("Chile/Continental"));
        int diaActual= fechaTemperatura.get(Calendar.DAY_OF_MONTH);
        int mesActual= fechaTemperatura.get(Calendar.MONTH)+1;
        int anioActual= fechaTemperatura.get(Calendar.YEAR);


        if(mesActual<10 && diaActual<10 ) {
            fechaCurrent="0"+diaActual+"0"+mesActual+anioActual;
        }else{
            if(mesActual<10){
                fechaCurrent=diaActual+"0"+mesActual+anioActual;
            }else{
                if(diaActual<10){
                    fechaCurrent="0"+diaActual+mesActual+anioActual;
                }else{
                    fechaCurrent = "" + diaActual + mesActual + anioActual;
                }
            }
        }
        //Toast.makeText(this, "este es el dia "+fechaCurrent,Toast.LENGTH_SHORT).show();
        setMinMax();
        temperaturaPromedio(fechaCurrent);
        temperaturaActual(fechaCurrent);
        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               volver();
            }
        });

        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temperaturaActual(fechaCurrent);
                temperaturaPromedio(fechaCurrent);
            }
        });
        btnhumedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Humedad;
                Humedad = new Intent(Temperatura.this,Humedad.class);
                startActivity(Humedad);
                finish();
            }
        });
        btnradiacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Radiacion;
                Radiacion = new Intent(Temperatura.this,Radiacion.class);
                startActivity(Radiacion);
                finish();
            }
        });

    }
    @Override
    public void onBackPressed(){
        //Toast.makeText(this,"Quieres volver",Toast.LENGTH_SHORT).show();
        volver();
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
                    /*String valorActual= actual.getString("valor");
                    String horaActual= actual.getString("hora");*/
                    int temperatura= actual.getInt("valor");
                    guardar.putInt("temperaturaActual",temperatura);
                    guardar.commit();
                    gaugeTemperatura.speedTo(temperatura);
                    //tempActual.setText("Temperatura Actual: "+valorActual+"°C");
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeTemperatura.speedTo(leer.getInt("temperaturaActual",0));
            }
        });
        requestQueue.add(request);

    }
    public void temperaturaPromedio(final String fecha){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"E1yGxKAcrg"+"/"+fecha;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, WS_URL, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("LOG_WS", response);
                try {
                    int promedio=0,suma=0;

                    JSONArray responseJson = response.getJSONArray("data");
                    for(int i=0;i<responseJson.length();i++){
                        JSONObject prom = responseJson.getJSONObject(i);
                        suma+=prom.getInt("valor");
                    }

                    promedio= suma/responseJson.length();
                    guardar.putInt("temperaturaPromedio",promedio);
                    guardar.commit();
                    gaugeTemperaturaProm.speedTo(promedio);
                    //tempPromedio.setText("Temperatura promedio: "+promedio+ " °C");

                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(Temperatura.this,"Hubo problemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeTemperaturaProm.speedTo(leer.getInt("temperaturaPromedio",0));

            }
        });
        requestQueue.add(request);

    }
    public void volver(){
        Intent irPrincipal;
        irPrincipal = new Intent(Temperatura.this,MainActivity.class);
        startActivity(irPrincipal);
        finish();
    }
    public void setMinMax(){
        gaugeTemperaturaProm.setMinSpeed(0);
        gaugeTemperaturaProm.setMaxSpeed(60);
        gaugeTemperaturaProm.setUnit("°C");
        gaugeTemperatura.setMinSpeed(0);
        gaugeTemperatura.setMaxSpeed(60);
        gaugeTemperatura.setUnit("°C");

    }
}
