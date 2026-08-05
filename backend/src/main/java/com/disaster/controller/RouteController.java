package com.disaster.controller;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.RouteResponse;
import com.disaster.dto.routing.OptimizeResponse;
import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RoadClosureRequest;
import com.disaster.service.RouteOptimizationService;
import com.disaster.service.routing.RoadClosureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route Optimization API. The original {@code POST /api/routes/calculate}
 * endpoint is kept unchanged and now returns real road routes plus
 * alternatives. Additional endpoints add multi-destination order optimization
 * and road-closure management without breaking existing clients.
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteOptimizationService routeService;
    private final RoadClosureService closureService;

    public RouteController(RouteOptimizationService routeService, RoadClosureService closureService) {
        this.routeService = routeService;
        this.closureService = closureService;
    }

    @PostMapping("/calculate")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'ROUTE_VIEW')")
    public ResponseEntity<RouteResponse> calculateRoute(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.calculateRoute(request));
    }

    @PostMapping("/optimize")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'ROUTE_VIEW')")
    public ResponseEntity<OptimizeResponse> optimizeOrder(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.optimizeOrder(request));
    }

    @GetMapping("/closures")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'ROUTE_VIEW')")
    public ResponseEntity<List<RoadClosure>> getClosures() {
        return ResponseEntity.ok(closureService.getActiveClosures());
    }

    @PostMapping("/closures")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'ROUTE_VIEW')")
    public ResponseEntity<RoadClosure> addClosure(@Valid @RequestBody RoadClosureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(closureService.addClosure(request));
    }

    @DeleteMapping("/closures/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'ROUTE_VIEW')")
    public ResponseEntity<Void> removeClosure(@PathVariable String id) {
        RoadClosure removed = closureService.removeClosure(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
