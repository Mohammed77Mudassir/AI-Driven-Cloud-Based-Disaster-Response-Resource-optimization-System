package com.disaster.service.routing;

import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RoadClosureRequest;

import java.util.List;
import java.util.Optional;

/**
 * Manages active road closures consumed by the routing engine. This interface
 * is the seam where real road-closure feeds (traffic agencies, citizen
 * reports, sensor data) can be wired in later without touching the routing
 * engine or the controller layer.
 */
public interface RoadClosureService {

    /** Active (not cleared) closures. */
    List<RoadClosure> getActiveClosures();

    /** All closures, including cleared ones. */
    List<RoadClosure> getAllClosures();

    Optional<RoadClosure> getById(String id);

    RoadClosure addClosure(RoadClosureRequest request);

    /** Remove a closure; returns the removed record or null if unknown. */
    RoadClosure removeClosure(String id);
}
