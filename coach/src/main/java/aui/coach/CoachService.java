package aui.coach;

import aui.coach.event.CoachEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachEventRepository eventRepository;

    Optional<Coach> find(Long id) {
        return coachRepository.findById(id);
    }

    List<Coach> findAll() {
        return coachRepository.findAll();
    }

    @Transactional
    public void create(Coach entity) {
        coachRepository.save(entity);
    }

    @Transactional
    public void delete(Coach entity) {
        eventRepository.delete(entity);
        coachRepository.delete(entity);
    }

    ResponseEntity<String> getCoachSwimmers(Long id) {
        Optional<Coach> coach = coachRepository.findById(id);
        if (coach.isEmpty()) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return eventRepository.getCoachSwimmers(id);
    }
}
