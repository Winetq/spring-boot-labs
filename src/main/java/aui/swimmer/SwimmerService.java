package aui.swimmer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class SwimmerService {
    private final SwimmerRepository repository;

    @Autowired
    SwimmerService(SwimmerRepository repository) {
        this.repository = repository;
    }

    Optional<Swimmer> find(Long id) {
        return repository.findById(id);
    }

    List<Swimmer> findAll() {
        return repository.findAll();
    }

    void create(Swimmer entity) {
        repository.save(entity);
    }

    void delete(Swimmer entity) {
        repository.delete(entity);
    }
}

