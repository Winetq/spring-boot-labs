package aui.swimmer.dto;

import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class PostSwimmerDto {

    private String name;
    private SwimmingStyle specialization;

    public static Swimmer dtoToEntity(PostSwimmerDto swimmer) {
        return new Swimmer(swimmer.getName(), swimmer.getSpecialization());
    }
}
