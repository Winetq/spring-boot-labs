package aui.coach;

import aui.swimmer.Swimmer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Coach {
    @Getter
    private UUID uuid;

    @Getter
    private String name;

    @Getter
    private List<Swimmer> swimmers;

    @Getter
    private int level;

    public Coach(String name, List<Swimmer> swimmers, int level) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.swimmers = swimmers;
        this.level = level;
        bindSwimmers();
    }

    private void bindSwimmers() {
        swimmers.forEach(x -> x.assignCoach(this));
    }

    public void addSwimmer(Swimmer swimmer) {
        swimmer.getCoach().getSwimmers().remove(swimmer);
        swimmers.add(swimmer);
        swimmer.assignCoach(this);
    }
}

