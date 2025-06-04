package ru.utalieva.victorina.model.dto.quiz;

import lombok.Data;
import ru.utalieva.victorina.model.entity.Option;
import java.util.Map;

@Data
public class OptionDTO {
    private String type;
    private String content;
    private String image;
    private Map<String, Integer> traits;

    public static OptionDTO fromEntity(Option option) {
        OptionDTO dto = new OptionDTO();
        dto.setType(option.getType());
        dto.setContent(option.getContent());
        dto.setImage(option.getImage());
        dto.setTraits(option.getTraits());
        return dto;
    }
} 