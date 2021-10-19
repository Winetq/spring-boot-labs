package aui.coach;

import aui.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public class CoachRepository implements Repository<Coach, UUID> {
    private final Map<UUID, Coach> coaches = new HashMap<>();

    @Override
    public Optional<Coach> find(UUID id) {
        return Optional.ofNullable(coaches.get(id));
    }

    @Override
    public List<Coach> findAll() {
        return new ArrayList<>(coaches.values());
    }

    @Override
    public void create(Coach entity) {
        coaches.put(entity.getUuid(), entity);
    }

    @Override
    public void delete(Coach entity) {
        coaches.remove(entity.getUuid());
    }
}

