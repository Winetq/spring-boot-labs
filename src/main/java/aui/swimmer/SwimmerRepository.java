package aui.swimmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SwimmerRepository extends JpaRepository<Swimmer, UUID> {

}

