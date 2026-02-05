package com.example.potcast_back.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("locutor")
public class Locutor implements Serializable {
    @Id
    private String id;

    private String nickname;
    private String mail;
    private String pais;
    private String fotografiaUrl;

    public Locutor(){}

    public Locutor(String id, String nickname, String mail, String pais, String fotografiaUrl) {
        this.id = id;
        this.nickname = nickname;
        this.mail = mail;
        this.pais = pais;
        this.fotografiaUrl = fotografiaUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getFotografiaUrl() {
        return fotografiaUrl;
    }

    public void setFotografiaUrl(String fotografiaUrl) {
        this.fotografiaUrl = fotografiaUrl;
    }
}