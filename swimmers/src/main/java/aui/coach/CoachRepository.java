package aui.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CoachRepository extends JpaRepository<Coach, Long> {

}

