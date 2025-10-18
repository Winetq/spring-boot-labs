package aui.coach.dto;

import aui.coach.Coach;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class CreateCoachRequest {

    private Long id;

    public static Coach dtoToEntity(CreateCoachRequest request) {
        return new Coach(request.getId());
    }
}
