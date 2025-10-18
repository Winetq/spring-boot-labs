package aui.coach;

import aui.swimmer.Swimmer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "coaches")
@Getter
@NoArgsConstructor
public class Coach {

    @Id
    private Long id;

    @OneToMany(mappedBy = "coach", fetch = LAZY, cascade = REMOVE)
    private List<Swimmer> swimmers;

    public Coach(Long id) {
        this.id = id;
    }
}
