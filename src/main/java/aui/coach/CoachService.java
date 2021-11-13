package aui.coach;

import aui.coach.event.CoachEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CoachService {
    private final CoachRepository coachRepository;
    private final CoachEventRepository eventRepository;

    @Autowired
    public CoachService(CoachRepository coachRepository, CoachEventRepository eventRepository) {
        this.coachRepository = coachRepository;
        this.eventRepository = eventRepository;
    }

    Optional<Coach> find(Long id) {
        return coachRepository.findById(id);
    }

    List<Coach> findAll() {
        return coachRepository.findAll();
    }

    @Transactional
    public void create(Coach entity) {
        coachRepository.save(entity); // it has to be first in order to set the ID
        eventRepository.create(entity);
    }

    @Transactional
    public void delete(Coach entity) {
        eventRepository.delete(entity);
        coachRepository.delete(entity);
    }
}

