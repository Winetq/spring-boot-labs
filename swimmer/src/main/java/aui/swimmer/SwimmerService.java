package aui.swimmer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwimmerService {

    private final SwimmerRepository swimmerRepository;

    @Transactional(readOnly = true)
    public Optional<Swimmer> find(Long id) {
        return swimmerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Swimmer> findAll() {
        return swimmerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Swimmer> findByCoachId(Long coachId) {
        return swimmerRepository.findByCoachId(coachId);
    }

    @Transactional
    public Swimmer save(Swimmer entity) {
        return swimmerRepository.save(entity);
    }

    @Transactional
    public Swimmer updateSpecialization(Long id, String specialization) {
        return find(id)
                .map(swimmer -> {
                    swimmer.setSpecialization(SwimmingStyle.of(specialization));
                    return save(swimmer);
                })
                .orElse(null);
    }

    @Transactional // it has to be @Transactional because of the @Modifying (UPDATE, DELETE)
    public int unassignCoachFromSwimmers(Long coachId) {
        return swimmerRepository.unassignCoach(coachId);
    }

    @Transactional
    public boolean delete(Long id) {
        return find(id)
                .map(swimmer -> {
                    swimmerRepository.delete(swimmer);
                    return true;
                })
                .orElse(false);
    }
}
