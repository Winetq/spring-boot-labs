package aui.swimmer;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SwimmerDTO {
    private String name;
    private SwimmingStyle specialization;

    public static List<SwimmerDTO> entityToDTO(List<Swimmer> swimmers) {
        return swimmers.stream().map(swimmer -> SwimmerDTO
                .builder()
                .name(swimmer.getName())
                .specialization(swimmer.getSpecialization())
                .build())
                .collect(Collectors.toList());
    }

    public static SwimmerDTO entityToDTO(Swimmer swimmer) {
        return SwimmerDTO.builder()
                .name(swimmer.getName())
                .specialization(swimmer.getSpecialization())
                .build();
    }
}

