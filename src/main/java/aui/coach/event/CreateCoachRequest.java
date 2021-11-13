package aui.coach.event;

import aui.coach.Coach;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCoachRequest {
    private Long id;

    public static CreateCoachRequest entityToDto(Coach coach) {
        return CreateCoachRequest.builder()
                .id(coach.getId())
                .build();
    }
}

