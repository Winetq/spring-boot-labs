package aui.coach;

import aui.swimmer.SwimmerDTO;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CoachDTO {
    private String name;
    private List<SwimmerDTO> swimmers;
    private int level;

    public static List<CoachDTO> entityToDTO(List<Coach> coaches) {
        return coaches.stream().map(coach -> CoachDTO
                .builder()
                .name(coach.getName())
                .swimmers(SwimmerDTO.entityToDTO(coach.getSwimmers()))
                .level(coach.getLevel())
                .build())
                .collect(Collectors.toList());
    }

    public static CoachDTO entityToDTO(Coach coach) {
        return CoachDTO.builder()
                .name(coach.getName())
                .swimmers(SwimmerDTO.entityToDTO(coach.getSwimmers()))
                .level(coach.getLevel())
                .build();
    }
}

