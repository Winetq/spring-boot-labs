package aui.swimmer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "swimmers")
@Getter
@NoArgsConstructor
public class Swimmer {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "coach_id")
    private Long coachId;

    @Setter
    @Enumerated(STRING)
    private SwimmingStyle specialization;

    public Swimmer(String name, Long coachId, SwimmingStyle specialization) {
        this.name = name;
        this.coachId = coachId;
        this.specialization = specialization;
    }
}
