package aui.coach.dto;

import aui.coach.Coach;
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
public class GetCoachDto {

    private String name;
    private int level;

    public static List<GetCoachDto> entityToDto(List<Coach> coaches) {
        return coaches.stream()
                .map(GetCoachDto::entityToDto)
                .collect(toList());
    }

    public static GetCoachDto entityToDto(Coach coach) {
        return GetCoachDto.builder()
                .name(coach.getName())
                .level(coach.getLevel())
                .build();
    }
}
