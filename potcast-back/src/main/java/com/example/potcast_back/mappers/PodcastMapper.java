package com.example.potcast_back.mappers;

import org.springframework.stereotype.Component;

import com.example.potcast_back.dtos.PodcastDTO;
import com.example.potcast_back.model.PodCast;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component

public class PodcastMapper {
    private final ObjectMapper mapper;

    // inyectamos el object mapper en springboot

    public PodcastMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // 1. dto a entidad
    public PodCast toEntity(PodcastDTO dto) {
        // convierte el dto a clase podcast
        return mapper.convertValue(dto, PodCast.class);
    }

    // el objeto generico a dto
    // evita nuestro viejo error 500

    public PodcastDTO toDTO(Object obj) {
        if (obj == null) {
            return null;
        }
        return mapper.convertValue(obj, PodcastDTO.class);
    }

}
