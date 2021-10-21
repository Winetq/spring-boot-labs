package aui.coach;

import aui.swimmer.Swimmer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "coaches")
public class Coach {
    @Id
    @Getter
    private UUID uuid;

    @Column(name = "coach_name")
    @Getter
    private String name;

    @OneToMany(mappedBy = "coach")
    @Getter
    private List<Swimmer> swimmers;

    @Column(name = "coach_level")
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

