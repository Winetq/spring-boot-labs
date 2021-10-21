package aui.coach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CoachService {
    private final CoachRepository repository;

    @Autowired
    public CoachService(CoachRepository repository) {
        this.repository = repository;
    }

    public Optional<Coach> find(UUID id) {
        return repository.findById(id);
    }

    public List<Coach> findAll() {
        return repository.findAll();
    }

    public void create(Coach entity) {
        repository.save(entity);
    }

    public void delete(Coach entity) {
        repository.delete(entity);
    }
}

