package utfpr.edu.br.ondeestou.service;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import utfpr.edu.br.ondeestou.model.Localizacao;

/**
 * Created by lucas.henrique on 24/10/2017.
 */

public interface LocalizacaoService {
    @GET("/localizacao")
    Call<List<Localizacao>> getAllLocalizacoes();

    @GET("/localizacao/{id}")
    Call<List<Localizacao>> getLocalizacaoById(@Path("id") int id);

    @POST
    Call<Localizacao> createLocalizacao(@Body Localizacao location);
}
