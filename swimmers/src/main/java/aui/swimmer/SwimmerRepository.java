package aui.swimmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SwimmerRepository extends JpaRepository<Swimmer, Long> {

}

