package aui.swimmer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwimmerService {

    private final SwimmerRepository swimmerRepository;
    private final SwimmerEventRepository swimmerEventRepository;

    Optional<Swimmer> find(Long id) {
        return swimmerRepository.findById(id);
    }

    public List<Swimmer> findAll() {
        return swimmerRepository.findAll();
    }

    void create(Swimmer entity) {
        swimmerRepository.save(entity);
    }

    void delete(Swimmer entity) {
        swimmerRepository.delete(entity);
    }

    ResponseEntity<String> getSwimmerCoach(Swimmer swimmer) {
        return swimmerEventRepository.getSwimmerCoach(swimmer);
    }
}
