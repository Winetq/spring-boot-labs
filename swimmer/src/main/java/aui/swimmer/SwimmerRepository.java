package aui.swimmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface SwimmerRepository extends JpaRepository<Swimmer, Long> {
    List<Swimmer> findByCoachId(Long coachId);

    @Modifying
    @Query("UPDATE Swimmer s SET s.coachId = NULL WHERE s.coachId = :coachId")
    int unassignCoach(@Param("coachId") Long coachId);
}
