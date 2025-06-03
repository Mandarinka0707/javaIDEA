package ru.utalieva.victorina.model.dto;

import lombok.Data;
import ru.utalieva.victorina.model.entity.Option;
import java.util.Map;

@Data
public class OptionDTO {
    private String content;
    private String type;
    private Map<String, Integer> traits;

    public static OptionDTO fromEntity(Option option) {
        OptionDTO dto = new OptionDTO();
        dto.setContent(option.getContent());
        dto.setType(option.getType());
        dto.setTraits(option.getTraits());
        return dto;
    }
} 