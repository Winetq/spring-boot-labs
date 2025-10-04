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
@Getter
public class Swimmer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "swimmer_name")
    private String name;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Coach coach;

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

    public void updateSwimmerSpecialization(SwimmingStyle specialization) {
        this.specialization = specialization;
    }
}

