package aui.swimmer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SwimmerService {
    private final SwimmerRepository repository;

    @Autowired
    public SwimmerService(SwimmerRepository repository) {
        this.repository = repository;
    }

    public Optional<Swimmer> find(Long id) {
        return repository.findById(id);
    }

    public List<Swimmer> findAll() {
        return repository.findAll();
    }

    public void create(Swimmer entity) {
        repository.save(entity);
    }

    public void delete(Swimmer entity) {
        repository.delete(entity);
    }
}

