package aui.coach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class CoachService {
    private final CoachRepository repository;

    @Autowired
    CoachService(CoachRepository repository) {
        this.repository = repository;
    }

    Optional<Coach> find(Long id) {
        return repository.findById(id);
    }

    List<Coach> findAll() {
        return repository.findAll();
    }

    void create(Coach entity) {
        repository.save(entity);
    }

    void delete(Coach entity) {
        repository.delete(entity);
    }
}

