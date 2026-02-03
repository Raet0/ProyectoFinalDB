package com.example.potcast_back.model;

import java.io.Serializable;

public class Locutor implements Serializable {
    private String nickname;
    private String email;
    private String pais;
    private String fotoUrl;

    public Locutor(){}

    public Locutor(String nickname, String email, String pais, String fotoUrl) {
        this.nickname = nickname;
        this.email = email;
        this.pais = pais;
        this.fotoUrl = fotoUrl;
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

}
