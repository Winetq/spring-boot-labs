package aui.swimmer;

import aui.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public class SwimmerRepository implements Repository<Swimmer, UUID> {
    private final Map<UUID, Swimmer> swimmers = new HashMap<>();

    @Override
    public Optional<Swimmer> find(UUID id) {
        return Optional.ofNullable(swimmers.get(id));
    }

    @Override
    public List<Swimmer> findAll() {
        return new ArrayList<>(swimmers.values());
    }

    @Override
    public void create(Swimmer entity) {
        swimmers.put(entity.getUuid(), entity);
    }

    @Override
    public void delete(Swimmer entity) {
        swimmers.remove(entity.getUuid());
    }
}

