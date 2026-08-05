package com.disaster.service.routing;

import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RoadClosureRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory implementation of {@link RoadClosureService}. Thread-safe and
 * dependency-free so the whole routing module works with the in-memory demo
 * database. Swap this bean for a database-backed implementation when real
 * closure feeds are introduced - the controller and routing engine do not
 * change.
 */
@Service
public class InMemoryRoadClosureService implements RoadClosureService {

    private final Map<String, RoadClosure> closures = new ConcurrentHashMap<>();

    private static final String[] VALID_TYPES = {
            "CLOSED", "BLOCKED", "FLOODED", "LANDSLIDE", "ACCIDENT"
    };

    @Override
    public List<RoadClosure> getActiveClosures() {
        return closures.values().stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .toList();
    }

    @Override
    public List<RoadClosure> getAllClosures() {
        return new ArrayList<>(closures.values());
    }

    @Override
    public Optional<RoadClosure> getById(String id) {
        return Optional.ofNullable(closures.get(id));
    }

    @Override
    public RoadClosure addClosure(RoadClosureRequest request) {
        String type = request.getType().trim().toUpperCase();
        boolean validType = false;
        for (String t : VALID_TYPES) {
            if (t.equals(type)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw new IllegalArgumentException("Invalid closure type: " + request.getType());
        }

        RoadClosure closure = new RoadClosure();
        closure.setId(UUID.randomUUID().toString());
        closure.setType(type);
        closure.setStatus("ACTIVE");
        closure.setLatitude(request.getLatitude());
        closure.setLongitude(request.getLongitude());
        closure.setLocationName(request.getLocationName());
        closure.setDescription(request.getDescription());
        closure.setAvoidanceRadiusKm(request.getAvoidanceRadiusKm());
        closure.setReportedAt(Instant.now().toString());
        closures.put(closure.getId(), closure);
        return closure;
    }

    @Override
    public RoadClosure removeClosure(String id) {
        return closures.remove(id);
    }
}
