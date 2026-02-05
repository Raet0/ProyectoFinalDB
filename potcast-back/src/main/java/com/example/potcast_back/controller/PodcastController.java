package com.example.potcast_back.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.potcast_back.dtos.PodcastDTO;
import com.example.potcast_back.service.PodcastService;

@RestController
@RequestMapping("/api/podcasts")
@CrossOrigin(origins = "http://localhost:4200") 
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    // 1. LISTAR TODOS - Para la lista lateral del Dashboard
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

    // 4. ELIMINAR - Para el botón de basura rojo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        podcastService.eliminarPodcast(id);
        return ResponseEntity.noContent().build();
    }

    // 4.1 ACTUALIZAR - Modificar podcast
    @PutMapping("/{id}")
    public ResponseEntity<PodcastDTO> actualizar(@PathVariable String id, @RequestBody PodcastDTO podcastDTO) {
        PodcastDTO actualizado = podcastService.actualizarPodcast(id, podcastDTO);
        return ResponseEntity.ok(actualizado);
    }

    // 5. REPRODUCIR (PLAY) - Registra una reproducción
    @PostMapping("/{id}/play")
    public ResponseEntity<String> reproducir(@PathVariable String id) {
        podcastService.registrarReproduccion(id);
        return ResponseEntity.ok("Reproducción registrada: " + id);
    }

    // 6. TOP VIEWS - Devuelve los podcasts más reproducidos
    @GetMapping("/reportes/top-views")
    public ResponseEntity<List<PodcastDTO>> getTopViews(){
        return ResponseEntity.ok(podcastService.reporteMasReproducido());
    }

    // 7. Invitados frecuentes - Devuelve lista de strings con nombres
    @GetMapping("/reportes/top-invitados")
    public ResponseEntity<List<String>> getTopInvitado(){
        Set<Object> resultado = podcastService.reporteInvitadosMasFrecuentes();
        List<String> lista = resultado.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // 8. Locutores por país - Devuelve lista de strings con nicknames
    @GetMapping("/reportes/pais/{pais}")
    public ResponseEntity<List<String>> getLocutoresPais(@PathVariable String pais){
        Set<Object> resultado = podcastService.reporteLocutoresPorPais(pais);
        List<String> lista = resultado.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // 9. Ordenar por FECHA ✅ NUEVO
    @GetMapping("/reportes/ordenado-por-fecha")
    public ResponseEntity<List<PodcastDTO>> ordenarPorFecha(
            @RequestParam(defaultValue = "desc") String orden) {
        return ResponseEntity.ok(podcastService.listarPorFecha(orden));
    }

    // 10. Ordenar por REPRODUCCIONES ✅ NUEVO
    @GetMapping("/reportes/ordenado-por-reproducciones")
    public ResponseEntity<List<PodcastDTO>> ordenarPorReproduciones(
            @RequestParam(defaultValue = "desc") String orden) {
        return ResponseEntity.ok(podcastService.listarPorReproduciones(orden));
    }
}