package aui.swimmer.dto;

import aui.coach.Coach;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class POSTSwimmerWithCoachDTO {
    private String name;
    private Long coach_id;
    private SwimmingStyle specialization;

    public static Swimmer dtoTOEntity(POSTSwimmerWithCoachDTO swimmer, Coach coach) {
        return new Swimmer(swimmer.getName(), coach, swimmer.getSpecialization());
    }
}

