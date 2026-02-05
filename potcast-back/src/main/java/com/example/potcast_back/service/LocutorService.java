package com.example.potcast_back.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.potcast_back.model.Locutor;
import com.example.potcast_back.repository.LocutorRepository;


@Service
public class LocutorService {
    private final LocutorRepository repository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public LocutorService(LocutorRepository repository) {
        this.repository = repository;
    }

    public Locutor createLocutor(String nickname, String mail, String pais, MultipartFile foto) throws IOException {
        String fotoUrl = null;

        if (foto != null && !foto.isEmpty()) {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String ext = getExtension(foto.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + ext;
            Path target = dir.resolve(filename);

            try (InputStream in = foto.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            fotoUrl = "/uploads/locutores/" + filename;
        }

        String safeMail = (mail == null || mail.trim().isEmpty()) ? "desconocido@podcast.local" : mail;
        String safePais = (pais == null || pais.trim().isEmpty()) ? "Desconocido" : pais;

        Locutor locutor = new Locutor();
        locutor.setId(UUID.randomUUID().toString());
        locutor.setNickname(nickname);
        locutor.setMail(safeMail);
        locutor.setPais(safePais);
        locutor.setFotografiaUrl(fotoUrl);

        return repository.save(locutor);
    }

    public Iterable<Locutor> getAll() {
        return repository.findAll();
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}