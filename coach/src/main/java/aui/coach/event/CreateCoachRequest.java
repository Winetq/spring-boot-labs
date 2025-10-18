package aui.coach.event;

import aui.coach.Coach;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class CreateCoachRequest {

    private Long id;

    public static CreateCoachRequest entityToDto(Coach coach) {
        return CreateCoachRequest.builder()
                .id(coach.getId())
                .build();
    }
}
