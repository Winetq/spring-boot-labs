package aui.swimmer.dto;

import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class GetSwimmerDto {

    private String name;
    private SwimmingStyle specialization;

    public static List<GetSwimmerDto> entityToDto(List<Swimmer> swimmers) {
        return swimmers.stream()
                .map(GetSwimmerDto::entityToDto)
                .collect(toList());
    }

    public static GetSwimmerDto entityToDto(Swimmer swimmer) {
        return GetSwimmerDto.builder()
                .name(swimmer.getName())
                .specialization(swimmer.getSpecialization())
                .build();
    }
}
