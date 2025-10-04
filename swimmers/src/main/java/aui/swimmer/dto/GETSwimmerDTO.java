package aui.swimmer.dto;

import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GETSwimmerDTO {
    private String name;
    private SwimmingStyle specialization;

    public static List<GETSwimmerDTO> entityToDTO(List<Swimmer> swimmers) {
        return swimmers.stream().map(swimmer -> GETSwimmerDTO
                .builder()
                .name(swimmer.getName())
                .specialization(swimmer.getSpecialization())
                .build())
                .collect(Collectors.toList());
    }

    public static GETSwimmerDTO entityToDTO(Swimmer swimmer) {
        return GETSwimmerDTO.builder()
                .name(swimmer.getName())
                .specialization(swimmer.getSpecialization())
                .build();
    }
}

