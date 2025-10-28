package aui.coach;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coaches")
@Getter
@NoArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private String name;

    @Setter
    @Column
    private int level;

    public Coach(String name, int level) {
        this.name = name;
        this.level = level;
    }
}
