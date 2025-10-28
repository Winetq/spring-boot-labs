package aui.coach;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachEventRepository eventRepository;

    public Optional<Coach> find(Long id) {
        return coachRepository.findById(id);
    }

    public List<Coach> findAll() {
        return coachRepository.findAll();
    }

    public Coach save(Coach entity) {
        return coachRepository.save(entity);
    }

    public Coach updateLevel(Long id, int level) {
        return find(id)
                .map(coach -> {
                    coach.setLevel(level);
                    return save(coach);
                })
                .orElse(null);
    }

    public ResponseEntity<String> delete(Long id) {
        return find(id)
                .map(coach -> {
                    coachRepository.delete(coach);
                    return eventRepository.delete(coach);
                })
                .orElse(null);
    }

    public ResponseEntity<String> getCoachSwimmers(Long id) {
        return find(id)
                .map(coach -> eventRepository.getCoachSwimmers(id))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }
}
