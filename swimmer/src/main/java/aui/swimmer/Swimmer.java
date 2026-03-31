package aui.swimmer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "swimmers")
@Getter
@NoArgsConstructor
@EqualsAndHashCode
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
