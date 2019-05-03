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
import com.ekn.gruzer.gaugelibrary.FullGauge;
import com.github.anastr.speedviewlib.ProgressiveGauge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Humedad extends AppCompatActivity {
    private String fechaCurrent;
    private TextView humActual;
    private TextView humPromedio;
    private int dia, mes, anio;
    private FullGauge indicador;
    private ProgressiveGauge gaugeHumedadProm;
    private ProgressiveGauge gaugeHumedad;
    private SharedPreferences leer;
    private SharedPreferences.Editor guardar;
    //private DatePickerDialog selectorFecha;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humedad);

        Button btnvolver = findViewById(R.id.volver);
        Button btnactualizar = findViewById(R.id.actualizar);
        Button btntemperatura = findViewById(R.id.temp);
        Button btnradiacion = findViewById(R.id.rad);
        gaugeHumedadProm =  findViewById(R.id.gaugeHumProm);
        gaugeHumedad= findViewById(R.id.gaugeHum);
        leer= getSharedPreferences("ccl.ubiobio.solmaforo",MODE_PRIVATE);
        guardar= leer.edit();
        /*humActual = findViewById(R.id.actualHum);
        humPromedio= findViewById(R.id.promHum);*/
        final Calendar fechaTemperatura = new GregorianCalendar(TimeZone.getTimeZone("Chile/Continental"));
        int diaActual= fechaTemperatura.get(Calendar.DAY_OF_MONTH);
        int mesActual= fechaTemperatura.get(Calendar.MONTH)+1;
        int anioActual= fechaTemperatura.get(Calendar.YEAR);


        //gaugeHumedadProm.speedTo(50);

        /*Range rango1= new Range();
        rango1.setColor(Color.parseColor("#ce0000"));
        rango1.setTo(15.0);
        rango1.setFrom(0.0);
        Range rango2= new Range();
        rango2.setColor(Color.parseColor("#E3E500"));
        rango2.setFrom(16.0);
        rango2.setTo(25.0);


        Range rango3= new Range();
        rango3.setColor(Color.parseColor("#00b20b"));
        rango3.setFrom(26.0);
        rango3.setTo(30.0);

        Range rango4= new Range();
        rango4.setColor(Color.parseColor("#E3E500"));
        rango4.setFrom(30.0);
        rango4.setTo(100.0);
        indicador=;
        indicador.addRange(rango1);
        indicador.addRange(rango2);
        indicador.addRange(rango3);
        indicador.addRange(rango4);
        indicador.setMaxValue(100.0);
        indicador.setMinValue(0.0);
        indicador.setValue(50.0);*/
        /*dia=fechaTemperatura.get(Calendar.DAY_OF_MONTH);
        mes=fechaTemperatura.get(Calendar.MONTH);
        anio=fechaTemperatura.get(Calendar.YEAR);*/

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
        humedadActual(fechaCurrent);
        humedadPromedio(fechaCurrent);
        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volver();
            }
        });
        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                humedadActual(fechaCurrent);
                humedadPromedio(fechaCurrent);
            }
        });
        btntemperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Temperatura;
                Temperatura = new Intent(Humedad.this,Temperatura.class);
                startActivity(Temperatura);
                finish();

            }
        });
        btnradiacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Radiacion;
                Radiacion = new Intent(Humedad.this,Radiacion.class);
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
                    //String valorActual= actual.getString("valor");
                    int humedad= actual.getInt("valor");
                    guardar.putInt("humedadActual",humedad);
                    guardar.commit();
                    gaugeHumedad.speedTo(humedad);
                    //String horaActual= actual.getString("hora");
                    //humActual.setText("Humedad Actual: "+valorActual);
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeHumedad.speedTo(leer.getInt("humedadActual",0));
            }
        });
        requestQueue.add(request);

    }
    public void humedadPromedio (String fecha){

        String WS_URL="http://arrau.chillan.ubiobio.cl:8075/ubbiot/web/mediciones/medicionespordia/"+"ZaNBTAqOR7"+"/"+"VIbSnGKyLW"+"/"+fecha;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, WS_URL, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d("LOG_WS", response);
                try {
                    int promedio=0;
                    int suma=0;
                    JSONArray responseJson = response.getJSONArray("data");

                    for(int i=0;i<responseJson.length();i++){
                        JSONObject prom = responseJson.getJSONObject(i);
                        suma+=prom.getInt("valor");
                    }
                    promedio= suma/responseJson.length();
                    guardar.putInt("humedadPromedio",promedio);
                    guardar.commit();
                    gaugeHumedadProm.speedTo(promedio);
                    //humPromedio.setText("Humedad promedio: "+promedio);
                    //Toast.makeText(Temperatura.this,"valor. "+valorActual+" y la hora es: "+horaActual,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LOG_WS",error.toString());
                gaugeHumedadProm.speedTo(leer.getInt("humedadPromedio",0));
            }
        });
        requestQueue.add(request);

    }
    public void volver(){
        Intent irPrincipal;
        irPrincipal = new Intent(Humedad.this,MainActivity.class);
        startActivity(irPrincipal);
        finish();

    }

    public void setMinMax(){
        gaugeHumedadProm.setMinSpeed(0);
        gaugeHumedadProm.setMaxSpeed(100);
        gaugeHumedadProm.setUnit("%RH");
        gaugeHumedad.setMinSpeed(0);
        gaugeHumedad.setMaxSpeed(100);
        gaugeHumedad.setUnit("%RH");

    }
}
