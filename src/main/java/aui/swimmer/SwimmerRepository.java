package aui.swimmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwimmerRepository extends JpaRepository<Swimmer, Long> {

}

