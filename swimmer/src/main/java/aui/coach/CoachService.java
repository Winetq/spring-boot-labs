package aui.coach;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository repository;

    public Optional<Coach> find(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void create(Coach entity) {
        repository.save(entity);
    }

    @Transactional
    public void delete(Coach entity) {
        repository.delete(entity);
    }
}
