package aui.swimmer.dto;

import aui.swimmer.Swimmer;
import aui.swimmer.SwimmingStyle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class POSTSwimmerDTO {
    private String name;
    private SwimmingStyle specialization;

    public static Swimmer dtoTOEntity(POSTSwimmerDTO swimmer) {
        return new Swimmer(swimmer.getName(), swimmer.getSpecialization());
    }
}

