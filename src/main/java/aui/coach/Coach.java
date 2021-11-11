package aui.coach;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Component
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "coaches")
@Getter
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "coach_name")
    private String name;

    @Column(name = "coach_level")
    private int level;

    public Coach(String name, int level) {
        this.name = name;
        this.level = level;
    }
}

