package com.example.potcast_back.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.potcast_back.dtos.PodcastDTO;
import com.example.potcast_back.service.PodcastService;

@RestController
@RequestMapping("/api/podcasts")
// Agregamos CrossOrigin para que Angular (puerto 4200) pueda hablar con Java (puerto 8080) sin problemas
@CrossOrigin(origins = "http://localhost:4200") 
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    // 1. LISTAR TODOS (NUEVO) - Para la lista lateral del Dashboard
    @GetMapping
    public ResponseEntity<List<PodcastDTO>> listarTodos() {
        return ResponseEntity.ok(podcastService.listarTodos());
    }

    // 2. CREAR
    @PostMapping
    public ResponseEntity<PodcastDTO> crear(@RequestBody PodcastDTO podcastDTO) {
        podcastService.crearPodcast(podcastDTO);
        return ResponseEntity.ok(podcastDTO);
    }

    // 3. OBTENER UNO (DETALLE)
    @GetMapping("/{id}")
    public ResponseEntity<PodcastDTO> obtener(@PathVariable String id) {
        PodcastDTO podcast = podcastService.obtenerPodcast(id);
        if (podcast == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(podcast);
    }

    // 4. ELIMINAR (NUEVO) - Para el botón de basura rojo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        podcastService.eliminarPodcast(id);
        return ResponseEntity.noContent().build(); // Retorna 204 (Éxito sin contenido)
    }

    // 5. REPRODUCIR (PLAY)
    @PostMapping("/{id}/play")
    public ResponseEntity<String> reproducir(@PathVariable String id) {
        podcastService.registrarReproduccion(id);
        return ResponseEntity.ok("Reproducción registrada: " + id);
    }

    // --- REPORTES ---

    // 6. TOP VIEWS (ACTUALIZADO) - Devuelve objetos completos para la tarjeta Hero
    @GetMapping("/reportes/top-views")
    public ResponseEntity<List<PodcastDTO>> getTopViews(){
        // Ahora devuelve List<PodcastDTO> en lugar de Set<Object>
        return ResponseEntity.ok(podcastService.reporteMasReproducido());
    }

    // 7. Invitados frecuentes (Solo devuelve nombres/IDs)
    @GetMapping("/reportes/top-invitados")
    public ResponseEntity<Set<Object>> getTopInvitado(){
        return ResponseEntity.ok(podcastService.reporteInvitadosMasFrecuentes());
    }

    // 8. Locutores por país
    @GetMapping("/reportes/pais/{pais}")
    public ResponseEntity<Set<Object>> getLocutoresPais(@PathVariable String pais){
        return ResponseEntity.ok(podcastService.reporteLocutoresPorPais(pais));
    }
}