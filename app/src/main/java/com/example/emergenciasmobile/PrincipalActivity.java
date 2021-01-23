package com.example.emergenciasmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PrincipalActivity extends AppCompatActivity {

    private Button btnCerrar;
    private ImageButton ImgButtonEmergency;

    private AlertDialog mAlertDialogPermisos;

    private JsonObjectRequest mJsonObjectRequest;
    private RequestQueue mRequestQueue;
    private LocationManager mLocationManager;

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        btnCerrar = findViewById(R.id.btnCerrar);
        ImgButtonEmergency = findViewById(R.id.btnEmergenciaP);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences =
                        getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
                preferences
                        .edit()
                        .clear()
                        .apply();

                Intent intent = new Intent(PrincipalActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        ImgButtonEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationPermisos()) {
                    /**envio a la BD**/

                    if (obtenerPosiciones())
                    {
                        enviarPosicione();
                    }


                }else
                    {
                        solicitarPermisos();
                    }
            }
        });


    }

    @SuppressLint("MissingPermission")
    private boolean obtenerPosiciones()
    {
        if (  !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(PrincipalActivity.this, "ACTIVAR GPS", Toast.LENGTH_LONG)
                    .show();

            return false;
        }


        /*Toast.makeText(PrincipalActivity.this, "GPS", Toast.LENGTH_LONG)
                .show();*/
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return true;
    }

    private void solicitarPermisos()
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(PrincipalActivity.this);
                mBuilder.setTitle("Permisos Location");
                mBuilder.setMessage("Por favor otorgar permisos de ubicación");
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Solicitar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        mAlertDialogPermisos.cancel();
                        mAlertDialogPermisos.hide();
                    }
                });
                mAlertDialogPermisos = mBuilder.create();
                mAlertDialogPermisos.show();


            }else
                {
                    /****/
                    ActivityCompat.requestPermissions(PrincipalActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},150);
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults)
    {

        switch (requestCode)
        {
            case 150:

                if (permissions.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (obtenerPosiciones())
                    {
                        enviarPosicione();
                    }

                }else
                    {
                        solicitarPermisos();
                    }

                break;
        }


    }


    private void enviarPosicione()
    {
        /*$cedula = $_GET['cedula'];
        $lat = $_GET['lat'];
        $lng = $_GET['lng'];*/
        SharedPreferences mSharedPreferences = getSharedPreferences("preferenciasLogin",MODE_PRIVATE);
        String cedula = mSharedPreferences.getString("usuario","error");

        String url = getResources().getString(R.string.url_pos_paciente)+"?cedula="+cedula
                +"&lat="+mLocation.getLatitude()+"&lng="+mLocation.getLongitude();

        Log.e("LOCATION",url);



        mJsonObjectRequest = new JsonObjectRequest(url

                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                try {
                    if(response.getString("status").equals("ok"))
                    {
                        Toast.makeText(PrincipalActivity.this, "DATOS ENVIADOS", Toast.LENGTH_SHORT).show();
                    }else
                        {
                            Toast.makeText(PrincipalActivity.this, "NO UPDATE", Toast.LENGTH_SHORT).show();
                        }
                } catch (JSONException e)
                {
                    Toast.makeText(PrincipalActivity.this, e.getMessage().toString()
                            , Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(PrincipalActivity.this, error.getMessage().toString()
                        , Toast.LENGTH_SHORT).show();
            }
        });

        mRequestQueue = Volley.newRequestQueue(PrincipalActivity.this);
        mRequestQueue.add(mJsonObjectRequest);

    }

    private boolean locationPermisos()
    {
       if(ContextCompat
               .checkSelfPermission(PrincipalActivity.this
                       ,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
       {
           return true;
       }
       return false;
    }
}