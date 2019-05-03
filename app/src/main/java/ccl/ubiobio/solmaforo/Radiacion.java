package ccl.ubiobio.solmaforo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.ProgressiveGauge;
import com.github.anastr.speedviewlib.SpeedView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
// Ocupar el primer gauge con colores de rayos UV
public class Radiacion extends AppCompatActivity {
    private TextView radActual;
    private String fechaCurrent;
    private TextView radPromedio;
    private SpeedView gaugeRadiacionProm;
    private SpeedView gaugeRadiacion;
    private SharedPreferences leer;
    private SharedPreferences.Editor guardar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiacion);

        Button btnvolver = findViewById(R.id.volver);
        Button btnactualizar = findViewById(R.id.actualizar);
        Button btntemperatura = findViewById(R.id.temp);
        Button btnhumedad = findViewById(R.id.hum);
        gaugeRadiacion= findViewById(R.id.gaugeRad);
        gaugeRadiacionProm= findViewById(R.id.gaugeRadProm);
        leer= getSharedPreferences("ccl.ubiobio.solmaforo",MODE_PRIVATE);
        guardar= leer.edit();
        /*radActual = findViewById(R.id.actualRad);
        radPromedio= findViewById(R.id.promRad);*/
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
        setMinMax();
        radiacionActual(fechaCurrent);
        radiacionPromedio(fechaCurrent);
        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent irPrincipal;
                irPrincipal = new Intent(Radiacion.this,MainActivity.class);
                startActivity(irPrincipal);
                finish();
            }
        });
        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiacionActual(fechaCurrent);
                radiacionPromedio(fechaCurrent);
            }
        });
        btntemperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Temperatura;
                Temperatura = new Intent(Radiacion.this,Temperatura.class);
                startActivity(Temperatura);
                finish();

            }
        });
        btnhumedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Humedad;
                Humedad = new Intent(Radiacion.this,Humedad.class);
                startActivity(Humedad);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed(){
        volver();
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
                    String horaActual= actual.getString("hora");
                    int radiacion= actual.getInt("valor");
                    guardar.putInt("radiacionActual",radiacion);
                    guardar.commit();
                    gaugeRadiacion.speedTo(radiacion);
                    //radActual.setText("Temperatura Actual: "+valorActual);
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeRadiacion.speedTo(leer.getInt("radiacionActual",0));
            }
        });
        requestQueue.add(request);

    }
    public void radiacionPromedio(final String fecha){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"8IvrZCP3qa"+"/"+fecha;
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
                    guardar.putInt("radiacionPromedio",promedio);
                    guardar.commit();
                    gaugeRadiacionProm.speedTo(promedio);
                    //radPromedio.setText("Radiación promedio: "+promedio);

                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeRadiacionProm.speedTo(leer.getInt("radiacionPromedio",0));
            }
        });
        requestQueue.add(request);

    }
    public void volver(){
        Intent irPrincipal;
        irPrincipal = new Intent(Radiacion.this,MainActivity.class);
        startActivity(irPrincipal);
        finish();

    }
    public void setMinMax(){
        gaugeRadiacionProm.setMinSpeed(0);
        gaugeRadiacionProm.setMaxSpeed(1600);
        gaugeRadiacionProm.setUnit("nm");
        gaugeRadiacion.setMinSpeed(0);
        gaugeRadiacion.setMaxSpeed(1600);
        gaugeRadiacion.setUnit("nm");

    }
}
