package utfpr.edu.br.ondeestou.model;

/**
 * Created by lucas.henrique on 24/10/2017.
 */

public class Localizacao {

    private String id;
    private String desc;
    private double lat;
    private double log;
    private double alt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    @Override
    public String toString() {
        return "Localizacao: " +
                " desc: '" + desc + '\'' +
                ", lat: " + lat +
                ", log: " + log +
                ", alt: " + alt +'}';
    }
}
