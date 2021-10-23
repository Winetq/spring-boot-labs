package aui.swimmer;

import aui.coach.Coach;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "swimmers")
public class Swimmer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "swimmer_name")
    @Getter
    private String name;

    @ManyToOne
    @Getter
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Coach coach;

    @Getter
    @Enumerated(EnumType.STRING)
    private SwimmingStyle specialization;

    public Swimmer(String name, SwimmingStyle specialization) {
        this.name = name;
        this.specialization = specialization;
    }

    public Swimmer(String name, Coach coach, SwimmingStyle specialization) {
        this.name = name;
        this.coach = coach;
        this.specialization = specialization;
    }

    public void assignCoach(Coach coach) {
        this.coach = coach;
    }
}

