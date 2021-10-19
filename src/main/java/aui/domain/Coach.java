package aui.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
final class Coach {
    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Getter
    private final List<Swimmer> swimmers;

    @Getter
    private int level;

    Coach(String name, List<Swimmer> swimmers, int level) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.swimmers = swimmers;
        this.level = level;
        bindSwimmers();
    }

    private void bindSwimmers() {
        swimmers.forEach(x -> x.assignCoach(this));
    }

    void incCoachLevel() {
        level++;
    }

    void decCoachLevel() {
        level--;
    }

    void addSwimmer(Swimmer swimmer) {
        swimmers.add(swimmer);
        swimmer.assignCoach(this);
    }
}

