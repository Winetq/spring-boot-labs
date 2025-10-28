package aui.coach.dto;

import aui.coach.Coach;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class PostCoachDto {

    private String name;
    private int level;

    public static Coach dtoToEntity(PostCoachDto coach) {
        return new Coach(coach.getName(), coach.getLevel());
    }
}
