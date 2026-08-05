package com.disaster.service;

import com.disaster.dto.EnhancedPredictionDTO;
import com.disaster.dto.PredictionDTO;

public interface PredictionEngine {
    PredictionDTO predict(String disasterType, String severity, String location, double latitude, double longitude);
    EnhancedPredictionDTO predict(EnhancedPredictionDTO input);
}
