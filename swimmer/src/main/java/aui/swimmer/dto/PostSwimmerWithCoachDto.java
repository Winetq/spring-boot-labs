package aui.swimmer.dto;

import aui.coach.Coach;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class PostSwimmerWithCoachDto {

    private String name;
    private Long coachId;
    private SwimmingStyle specialization;

    public static Swimmer dtoToEntity(PostSwimmerWithCoachDto swimmer, Coach coach) {
        return new Swimmer(swimmer.getName(), coach, swimmer.getSpecialization());
    }
}
