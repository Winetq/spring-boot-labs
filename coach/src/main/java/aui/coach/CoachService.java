package aui.coach;

import aui.rabbitmq.CoachPublisher;
import aui.rabbitmq.GetCoachSwimmersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachPublisher coachPublisher;

    @Transactional(readOnly = true)
    public Optional<Coach> find(Long id) {
        return coachRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Coach> findAll() {
        return coachRepository.findAll();
    }

    @Transactional
    public Coach save(Coach entity) {
        return coachRepository.save(entity);
    }

    @Transactional
    public Coach updateLevel(Long id, int level) {
        return find(id)
                .map(coach -> {
                    coach.setLevel(level);
                    return save(coach);
                })
                .orElse(null);
    }

    @Transactional
    public boolean delete(Long id) {
        return find(id)
                .map(coach -> {
                    coachRepository.delete(coach);
                    coachPublisher.publishDeleteCoachEvent(coach.getId());
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<GetCoachSwimmersResponse> getCoachSwimmers(Long id) {
        return find(id)
                .map(coach -> coachPublisher.publishGetCoachSwimmersRequest(id))
                .orElse(null);
    }
}
