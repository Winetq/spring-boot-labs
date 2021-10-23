package aui.coach;

import aui.swimmer.Swimmer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "coaches")
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "coach_name")
    @Getter
    private String name;

    @OneToMany(mappedBy = "coach", fetch = FetchType.EAGER)
    @Getter
    private List<Swimmer> swimmers;

    @Column(name = "coach_level")
    @Getter
    private int level;

    public Coach(String name, List<Swimmer> swimmers, int level) {
        this.name = name;
        this.swimmers = swimmers;
        this.level = level;
    }

    public void addSwimmer(Swimmer swimmer) {
        swimmer.getCoach().getSwimmers().remove(swimmer);
        swimmers.add(swimmer);
        swimmer.assignCoach(this);
    }
}

