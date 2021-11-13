package aui.coach;

import aui.swimmer.Swimmer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;

@Component
@NoArgsConstructor
@Entity
@Table(name = "coaches")
@Getter
public class Coach {
    @Id
    private Long id;

    @OneToMany(mappedBy = "coach", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Swimmer> swimmers;

    public Coach(Long id) {
        this.id = id;
    }
}

