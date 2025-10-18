package aui.coach;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coaches")
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column
    private String name;

    @Column
    private int level;

    public Coach(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public void updateCoachLevel(int level) {
        this.level = level;
    }
}
