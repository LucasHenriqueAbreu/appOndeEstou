package utfpr.edu.br.ondeestou;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utfpr.edu.br.ondeestou.model.Localizacao;
import utfpr.edu.br.ondeestou.service.LocalizacaoService;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final String EXTRA_MESSAGE = "utfpr.edu.br.MESSAGE";
    private final String TAG = this.getClass().getSimpleName();
    public LocalizacaoService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.locationService = retrofit.create(LocalizacaoService.class);

        getAllLocalizacaos();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        if ( ! lm.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
            showAlert();
        }

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);

        ActivityCompat.requestPermissions( this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1 );
    }

    private void createLocalizacao(Localizacao newLocalizacao) {

        Call<Localizacao> call = this.locationService.createLocalizacao(newLocalizacao);
        call.enqueue(new Callback<Localizacao>() {
            @Override
            public void onResponse(Call<Localizacao> call, Response<Localizacao> response) {
                //displayLocalizacao(response.body());
                Log.e(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<Localizacao> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to create post" , Toast.LENGTH_LONG).show();
                Log.e(TAG,t.toString());
            }
        });
    }

    private void getAllLocalizacaos() {
        Call<List<Localizacao>> getAllLocalizacaosCall = this.locationService.getAllLocalizacoes();

        getAllLocalizacaosCall.enqueue(new Callback<List<Localizacao>>() {
            @Override
            public void onResponse(Call<List<Localizacao>> call, Response<List<Localizacao>> response) {
                //displayLocalizacao(response.body().get(0));
                Log.e(TAG, response.body().get(0).toString());
            }

            @Override
            public void onFailure(Call<List<Localizacao>> call, Throwable t) {
                Log.e(TAG, "Error occured while fetching post.");
            }
        });
    }

    /**Chamada do botão sendMessage */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edtMsg);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void showAlert() {
        AlertDialog.Builder alerta = new AlertDialog.Builder( this );
        alerta.setTitle( "Atenção" );
        alerta.setMessage( "GPS não habilitado. Deseja Habilitar??" );
        alerta.setCancelable( false );
        alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                startActivity( i );
            }
        } );
        alerta.setNegativeButton( "Cancelar", null );
        alerta.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Localizacao localizacao = new Localizacao();
        localizacao.setDesc("Nada ainda");
        localizacao.setLat(location.getLatitude());
        localizacao.setLog(location.getLongitude());
        localizacao.setAlt(location.getAltitude());
        createLocalizacao(localizacao);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
