package com.example.potcast_back.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.potcast_back.model.Locutor;
import com.example.potcast_back.service.LocutorService;

@RestController
@RequestMapping("/locutores")
@CrossOrigin(origins = "http://localhost:4200")
public class LocutorController {
    private final LocutorService service;

    public LocutorController(LocutorService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Locutor> create(
            @RequestParam String nickname,
            @RequestParam(required = false) String mail,
            @RequestParam(required = false) String pais,
            @RequestPart(value = "foto", required = false) MultipartFile foto
    ) throws IOException {
        Locutor created = service.createLocutor(nickname, mail, pais, foto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Iterable<Locutor>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}