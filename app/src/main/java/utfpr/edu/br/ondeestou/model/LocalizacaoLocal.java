package utfpr.edu.br.ondeestou.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by lucas on 31/10/17.
 */

@Table(name = "localizacao")
public class LocalizacaoLocal extends Model {

    @Column(name = "id")
    public Long id;

    @Column(name = "desc")
    public String desc;

    @Column(name = "lat")
    public double lat;

    @Column(name = "log")
    public double log;

    @Column(name = "alt")
    public double alt;

    @Column(name = "sincronized")
    public boolean sincronized;

}