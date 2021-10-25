package aui.coach.dto;

import aui.coach.Coach;
import aui.swimmer.dto.GETSwimmerDTO;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GETCoachDTO {
    private String name;
    private List<GETSwimmerDTO> swimmers;
    private int level;

    public static List<GETCoachDTO> entityToDTO(List<Coach> coaches) {
        return coaches.stream().map(coach -> GETCoachDTO
                .builder()
                .name(coach.getName())
                .swimmers(GETSwimmerDTO.entityToDTO(coach.getSwimmers()))
                .level(coach.getLevel())
                .build())
                .collect(Collectors.toList());
    }

    public static GETCoachDTO entityToDTO(Coach coach) {
        return GETCoachDTO.builder()
                .name(coach.getName())
                .swimmers(GETSwimmerDTO.entityToDTO(coach.getSwimmers()))
                .level(coach.getLevel())
                .build();
    }
}

