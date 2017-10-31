package utfpr.edu.br.ondeestou;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utfpr.edu.br.ondeestou.model.Localizacao;
import utfpr.edu.br.ondeestou.model.LocalizacaoLocal;
import utfpr.edu.br.ondeestou.service.LocalizacaoService;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final String EXTRA_MESSAGE = "utfpr.edu.br.MESSAGE";
    private final String TAG = this.getClass().getSimpleName();
    public LocalizacaoService locationService;
    public List<Localizacao> localizacoes = new ArrayList<>();
    public ListView listaLocalizacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAllLocalizacao();

        listaLocalizacoes = (ListView) findViewById(R.id.lista);
        ArrayAdapter<Localizacao> adapter = new ArrayAdapter<Localizacao>(this,
                android.R.layout.simple_list_item_1, localizacoes);
        listaLocalizacoes.setAdapter(adapter);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.locationService = retrofit.create(LocalizacaoService.class);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        if ( ! lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showAlert();
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);

        ActivityCompat.requestPermissions( this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1 );
    }

    @Override
    public void onLocationChanged(Location location) {
        Localizacao localizacao = new Localizacao();
        localizacao.setDesc("Nada ainda");
        localizacao.setLat(location.getLatitude());
        localizacao.setLog(location.getLongitude());
        localizacao.setAlt(location.getAltitude());
        saveLocalizacao(localizacao);
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

    /**
     * Salva uma localização e altera se já existir (na base online)
     * @param newLocalizacao
     */
    private void createLocalizacaoOnServer(Localizacao newLocalizacao) {
        Call<Localizacao> call = this.locationService.createLocalizacao(newLocalizacao);
        call.enqueue(new Callback<Localizacao>() {
            @Override
            public void onResponse(Call<Localizacao> call, Response<Localizacao> response) {
                //displayLocalizacao(response.body());
                //localizacoes = response.body();
                Log.e(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<Localizacao> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to create post" , Toast.LENGTH_LONG).show();
                Log.e(TAG,t.toString());
            }
        });
    }

    /**
     * Salva uma localização e altera se já existir (na base local)
     * @param localizacaoLocal
     */
    private void createLocalizacaoLocal(LocalizacaoLocal localizacaoLocal) {
        try {
            Long id = localizacaoLocal.save();
            Log.v("id_value", String.valueOf(id));
        } catch (Exception err) {
            Log.e("err",String.valueOf(err));
        }
    }

    /**
     * Busca toas as localizações na base online.
     */
    private void getAllLocalizacao() {
        Call<List<Localizacao>> getAllLocalizacaosCall = this.locationService.getAllLocalizacoes();

        getAllLocalizacaosCall.enqueue(new Callback<List<Localizacao>>() {
            @Override
            public void onResponse(Call<List<Localizacao>> call, Response<List<Localizacao>> response) {
                //displayLocalizacaodisplayLocalizacao(response.body().get(0));
                localizacoes = response.body();
            }

            @Override
            public void onFailure(Call<List<Localizacao>> call, Throwable t) {
                Log.e(TAG, "Error occured while fetching post.");
            }
        });
    }

    /**
     * Chamada do botão sentLocalizacao, mana a localziação para outra tela
     * @param view
     */
    public void sendLocalizacao(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
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

    /**
     * Salva a localização, se não possuir internet salva na base local,
     * caso contrário busca na base de dados localizações ainda não salvas no servidor
     * e realiza a persistência.
     * @param localizacao
     */
    private void saveLocalizacao(Localizacao localizacao) {
        LocalizacaoLocal localizacaoLocal = new LocalizacaoLocal();
        if (haveConnection()) {
            localizacaoLocal =  convertOnlineToLocal(localizacao);
            localizacaoLocal.sincronized = true;

            createLocalizacaoLocal(localizacaoLocal);
            createLocalizacaoOnServer(localizacao);

            List<LocalizacaoLocal> listaLocalizacaoLocal = getAllLolcalizacoesLocal();
            for (int i = 0; i < listaLocalizacaoLocal.size(); i++) {
                createLocalizacaoOnServer(convertLocalToOnline(listaLocalizacaoLocal.get(i)));
                listaLocalizacaoLocal.get(i).sincronized = true;
                createLocalizacaoLocal(listaLocalizacaoLocal.get(i));
            }
        } else {
            localizacaoLocal.sincronized = false;
            createLocalizacaoLocal(localizacaoLocal);
        }
    }

    /**
     * Verifica se existe conexão com a internet
     * @return connected
     */
    public  boolean haveConnection() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            connected = true;
        } else {
            connected = false;
        }
        return connected;
    }

    /**
     * Busca todas as Localizações armazendas
     * localmente que não estão sincronizadas ainda
     * @return Lista de Localizaçoes
     */
    private List<LocalizacaoLocal> getAllLolcalizacoesLocal() {
        return new Select()
                .from(LocalizacaoLocal.class)
                .where("sincronized = ?", false)
                .executeSingle();
    }

    private Localizacao convertLocalToOnline(LocalizacaoLocal localizacaoLocal) {
        Localizacao localizacao = new Localizacao();
        localizacao.setDesc(localizacaoLocal.desc);
        localizacao.setLat(localizacaoLocal.lat);
        localizacao.setLog(localizacaoLocal.log);
        localizacao.setAlt(localizacaoLocal.alt);

        return localizacao;
    }

    private  LocalizacaoLocal convertOnlineToLocal(Localizacao localizacao) {
        LocalizacaoLocal localizacaoLocal = new LocalizacaoLocal();
        localizacaoLocal.desc = localizacao.getDesc();
        localizacaoLocal.lat = localizacao.getLat();
        localizacaoLocal.log = localizacao.getLog();
        localizacaoLocal.alt = localizacao.getAlt();

        return localizacaoLocal;
    }

}
