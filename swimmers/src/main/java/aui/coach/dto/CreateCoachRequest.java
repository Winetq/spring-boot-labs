package aui.coach.dto;

import aui.coach.Coach;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCoachRequest {
    private Long id;

    public static Coach dtoToEntity(CreateCoachRequest request) {
        return new Coach(request.getId());
    }
}
