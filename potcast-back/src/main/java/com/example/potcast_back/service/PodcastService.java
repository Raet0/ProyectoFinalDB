package com.example.potcast_back.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return podcastMapper.toDTO(dataCruda);
    }

    // 3. LISTAR TODOS (NUEVO PARA EL DASHBOARD) ✅
    public List<PodcastDTO> listarTodos() {
        // Obtenemos todas las claves que empiecen con "podcast:data:"
        Set<String> keys = redisTemplate.keys(KEY_DATA + "*");
        List<PodcastDTO> lista = new ArrayList<>();

        if (keys != null && !keys.isEmpty()) {
            // Redis nos permite traer múltiples valores de golpe (más rápido)
            List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
            
            if (objects != null) {
                for (Object obj : objects) {
                    if (obj != null) {
                        lista.add(podcastMapper.toDTO(obj));
                    }
                }
            }
        }
        return lista;
    }

    // 4. ELIMINAR PODCAST (NUEVO) ✅
    public void eliminarPodcast(String id) {
        // 1. Borramos la data principal
        redisTemplate.delete(KEY_DATA + id);
        // 2. Lo sacamos del ranking de vistas para que no salga en "Más escuchados"
        redisTemplate.opsForZSet().remove(KEY_VIEWS, id);
        
        // Nota: Dejar los índices de autores (participaciones) está bien para mantener histórico,
        // o podrías borrarlos también si quisieras una limpieza total.
    }

    // 5. REGISTRAR REPRODUCCION
    public void registrarReproduccion(String id) {
        redisTemplate.opsForZSet().incrementScore(KEY_VIEWS, id, 1);
    }

    // 6. REPORTE: MAS REPRODUCIDOS (DEVUELVE OBJETOS COMPLETOS AHORA) ✅
    public List<PodcastDTO> reporteMasReproducido() {
        // 1. Obtener los IDs de los top 10
        Set<Object> topIds = redisTemplate.opsForZSet().reverseRange(KEY_VIEWS, 0, 9);
        List<PodcastDTO> topPodcasts = new ArrayList<>();

        if (topIds != null) {
            for (Object idObj : topIds) {
                String id = idObj.toString();
                // 2. Buscar la data completa de cada ID
                PodcastDTO dto = obtenerPodcast(id);
                if (dto != null) {
                    topPodcasts.add(dto);
                }
            }
        }
        return topPodcasts;
    }

    // REPORTES EXTRA (SOLO IDs/Strings)
    public Set<Object> reporteInvitadosMasFrecuentes(){
        return redisTemplate.opsForZSet().reverseRange(KEY_PARTICIPACIONES, 0, 9);
    }

    public Set<Object> reporteLocutoresPorPais(String pais){
        return redisTemplate.opsForSet().members(KEY_PAIS_LOCUTOR + pais.toLowerCase());
    }
}