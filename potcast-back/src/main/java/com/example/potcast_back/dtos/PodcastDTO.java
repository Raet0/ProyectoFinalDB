package com.example.potcast_back.dtos;

import java.io.Serializable;
import java.util.List;

import com.example.potcast_back.model.Locutor;

public class PodcastDTO implements Serializable{
    private String id;
    private String temaGeneral;
    private String temaDia;
    private String categoria;
    private String fecha;
    private String audioUrl;

    // queria verificar por pais me saio un error entonces chat me recomendo que agregue estas dos variables porque antes las estaba ignorando
    // json pierde los campos al dar enter
    private Locutor locutorPrincipal;
    private List<Locutor> invitados; 

    public Locutor getLocutorPrincipal() {
        return locutorPrincipal;
    }

    public void setLocutorPrincipal(Locutor locutorPrincipal) {
        this.locutorPrincipal = locutorPrincipal;
    }

    public List<Locutor> getInvitados() {
        return invitados;
    }

    public void setInvitados(List<Locutor> invitados) {
        this.invitados = invitados;
    }

    public PodcastDTO(){

    }

    public PodcastDTO(String id, String temaGeneral, String temaDia, String categoria, String fecha, String audioUrl, Locutor locutorPrincipal, List<Locutor> invitados) {
        this.id = id;
        this.temaGeneral = temaGeneral;
        this.temaDia = temaDia;
        this.categoria = categoria;
        this.fecha = fecha;
        this.audioUrl = audioUrl;
        this.locutorPrincipal = locutorPrincipal;
        this.invitados = invitados;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemaGeneral() {
        return temaGeneral;
    }

    public void setTemaGeneral(String temaGeneral) {
        this.temaGeneral = temaGeneral;
    }

    public String getTemaDia() {
        return temaDia;
    }

    public void setTemaDia(String temaDia) {
        this.temaDia = temaDia;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    
}
