package aui.swimmer;

import aui.coach.Coach;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Swimmer {
    @Getter
    private UUID uuid;

    @Getter
    private String name;

    @Getter
    @ToString.Exclude
    private Coach coach;

    @Getter
    private SwimmingStyle specialization;

    public Swimmer(String name, SwimmingStyle specialization) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.specialization = specialization;
    }

    public Swimmer(String name, Coach coach, SwimmingStyle specialization) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.coach = coach;
        this.specialization = specialization;
        coach.getSwimmers().add(this);
    }

    public void assignCoach(Coach coach) {
        this.coach = coach;
    }
}

