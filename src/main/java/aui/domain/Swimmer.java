package aui.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
final class Swimmer {
    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Getter
    private Coach coach;

    @Getter
    private final SwimmingStyle specialization;

    Swimmer(String name, SwimmingStyle specialization) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.specialization = specialization;
    }

    void assignCoach(Coach coach) {
        this.coach = coach;
    }
}

