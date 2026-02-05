package com.example.potcast_back.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.example.potcast_back.dtos.PodcastDTO;
import com.example.potcast_back.mappers.PodcastMapper;
import com.example.potcast_back.model.Locutor;
import com.example.potcast_back.model.PodCast;

@Service
public class PodcastService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PodcastMapper podcastMapper;

    // Claves maestras
    private static final String KEY_DATA = "podcast:data:";
    private static final String KEY_VIEWS = "ranking:reproducciones:";
    private static final String KEY_PARTICIPACIONES = "ranking:invitados:";
    private static final String KEY_PAIS_LOCUTOR = "indice:pais:";

    // 1. GUARDAR (CREAR O ACTUALIZAR)
    public void crearPodcast(PodcastDTO podcastDTO) {
        PodCast entidad = podcastMapper.toEntity(podcastDTO);
        
        // Guardar la data principal
        redisTemplate.opsForValue().set(KEY_DATA + entidad.getId(), entidad);
        
        // Inicializar ranking en 0 si no existe
        redisTemplate.opsForZSet().addIfAbsent(KEY_VIEWS, entidad.getId(), 0);
        
        // Indexar invitados
        if (entidad.getInvitados() != null ){
            for(Locutor invitado : entidad.getInvitados()){
                redisTemplate.opsForZSet().incrementScore(KEY_PARTICIPACIONES, invitado.getNickname(), 1);
                String keyPais = KEY_PAIS_LOCUTOR + invitado.getPais().toLowerCase();
                redisTemplate.opsForSet().add(keyPais, invitado.getNickname());
            }
        }
        
        // Indexar locutor principal
        if(entidad.getLocutorPrincipal() != null){
            String keyPais = KEY_PAIS_LOCUTOR + entidad.getLocutorPrincipal().getPais().toLowerCase();
            redisTemplate.opsForSet().add(keyPais, entidad.getLocutorPrincipal().getNickname());
        }
    }

    // 2. OBTENER UNO POR ID
    public PodcastDTO obtenerPodcast(String id) {
        Object dataCruda = redisTemplate.opsForValue().get(KEY_DATA + id);
        if (dataCruda == null) return null;
        
        PodcastDTO dto = podcastMapper.toDTO(dataCruda);
        
        // IMPORTANTE: Agregar las vistas desde el ranking
        Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, id);
        dto.setVistas(score != null ? score.intValue() : 0);
        
        return dto;
    }

    // 3. LISTAR TODOS
    public List<PodcastDTO> listarTodos() {
        Set<String> keys = redisTemplate.keys(KEY_DATA + "*");
        List<PodcastDTO> lista = new ArrayList<>();

        if (keys != null && !keys.isEmpty()) {
            List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
            if (objects != null) {
                for (Object obj : objects) {
                    if (obj != null) {
                        try {
                            PodcastDTO dto = podcastMapper.toDTO(obj);
                            if (dto != null) {
                                // Agregar las vistas de Redis ZSet
                                Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, dto.getId());
                                dto.setVistas(score != null ? score.intValue() : 0);
                                
                                lista.add(dto);
                            }
                        } catch (Exception e) {
                            System.err.println("Error al mapear podcast del objeto Redis: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return lista;
    }

    // 4. ELIMINAR PODCAST ✅ CORREGIDO
    public void eliminarPodcast(String id) {
        // 1. Obtener el podcast ANTES de eliminarlo para limpiar índices
        Object dataCruda = redisTemplate.opsForValue().get(KEY_DATA + id);
        if (dataCruda != null) {
            PodCast podcast = podcastMapper.toEntity(podcastMapper.toDTO(dataCruda));
            
            // 2. Limpiar participaciones de invitados
            if (podcast.getInvitados() != null) {
                for (Locutor invitado : podcast.getInvitados()) {
                    redisTemplate.opsForZSet().incrementScore(KEY_PARTICIPACIONES, invitado.getNickname(), -1);
                    // Si llega a 0, eliminarlo completamente
                    Double score = redisTemplate.opsForZSet().score(KEY_PARTICIPACIONES, invitado.getNickname());
                    if (score != null && score <= 0) {
                        redisTemplate.opsForZSet().remove(KEY_PARTICIPACIONES, invitado.getNickname());
                    }
                }
            }
            
            // Nota: NO eliminamos del índice de país porque otros podcasts pueden usar el mismo locutor
        }
        
        // 3. Eliminar datos principales
        redisTemplate.delete(KEY_DATA + id);
        redisTemplate.opsForZSet().remove(KEY_VIEWS, id);
    }

    // 4.1 ACTUALIZAR PODCAST (solo data principal)
    public PodcastDTO actualizarPodcast(String id, PodcastDTO podcastDTO) {
        podcastDTO.setId(id);
        PodCast entidad = podcastMapper.toEntity(podcastDTO);

        redisTemplate.opsForValue().set(KEY_DATA + id, entidad);

        // Mantener ranking existente, inicializar si no existe
        Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, id);
        if (score == null) {
            redisTemplate.opsForZSet().add(KEY_VIEWS, id, 0);
        }

        return obtenerPodcast(id);
    }

    // 5. REGISTRAR REPRODUCCION
    public void registrarReproduccion(String id) {
        redisTemplate.opsForZSet().incrementScore(KEY_VIEWS, id, 1);
    }

    // 6. REPORTE: MAS REPRODUCIDOS - CON VISTAS ✅
    public List<PodcastDTO> reporteMasReproducido() {
        Set<Object> topIds = redisTemplate.opsForZSet().reverseRange(KEY_VIEWS, 0, 9);
        List<PodcastDTO> topPodcasts = new ArrayList<>();

        if (topIds != null) {
            for (Object idObj : topIds) {
                try {
                    String id = idObj.toString();
                    PodcastDTO dto = obtenerPodcast(id); // Este método YA incluye las vistas
                    if (dto != null) {
                        topPodcasts.add(dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener top podcast: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return topPodcasts;
    }

    // 7. REPORTES EXTRA
    public Set<Object> reporteInvitadosMasFrecuentes(){
        return redisTemplate.opsForZSet().reverseRange(KEY_PARTICIPACIONES, 0, 9);
    }

    public Set<Object> reporteLocutoresPorPais(String pais){
        return redisTemplate.opsForSet().members(KEY_PAIS_LOCUTOR + pais.toLowerCase());
    }

    // 8. REPORTE: LISTAR POR FECHA ✅ NUEVO
    public List<PodcastDTO> listarPorFecha(String orden) {
        List<PodcastDTO> todos = listarTodos();
        
        if ("desc".equalsIgnoreCase(orden)) {
            // Descendente: Más nuevos primero
            todos.sort((a, b) -> {
                try {
                    LocalDate fechaA = a.getFecha() != null ? LocalDate.parse(a.getFecha()) : LocalDate.MIN;
                    LocalDate fechaB = b.getFecha() != null ? LocalDate.parse(b.getFecha()) : LocalDate.MIN;
                    return fechaB.compareTo(fechaA);
                } catch (Exception e) {
                    System.err.println("Error al parsear fechas: " + a.getFecha() + ", " + b.getFecha());
                    return 0;
                }
            });
        } else {
            // Ascendente: Más antiguos primero
            todos.sort((a, b) -> {
                try {
                    LocalDate fechaA = a.getFecha() != null ? LocalDate.parse(a.getFecha()) : LocalDate.MIN;
                    LocalDate fechaB = b.getFecha() != null ? LocalDate.parse(b.getFecha()) : LocalDate.MIN;
                    return fechaA.compareTo(fechaB);
                } catch (Exception e) {
                    System.err.println("Error al parsear fechas: " + a.getFecha() + ", " + b.getFecha());
                    return 0;
                }
            });
        }
        
        return todos;
    }

    // 9. REPORTE: LISTAR POR REPRODUCCIONES ✅ NUEVO
    public List<PodcastDTO> listarPorReproduciones(String orden) {
        List<PodcastDTO> todos = listarTodos();
        
        if ("desc".equalsIgnoreCase(orden)) {
            // Descendente: Más vistas primero
            todos.sort((a, b) -> {
                Integer vistasA = a.getVistas();
                Integer vistasB = b.getVistas();
                int vA = (vistasA != null) ? vistasA.intValue() : 0;
                int vB = (vistasB != null) ? vistasB.intValue() : 0;
                return Integer.compare(vB, vA);
            });
        } else {
            // Ascendente: Menos vistas primero
            todos.sort((a, b) -> {
                Integer vistasA = a.getVistas();
                Integer vistasB = b.getVistas();
                int vA = (vistasA != null) ? vistasA.intValue() : 0;
                int vB = (vistasB != null) ? vistasB.intValue() : 0;
                return Integer.compare(vA, vB);
            });
        }
        
        return todos;
    }
}