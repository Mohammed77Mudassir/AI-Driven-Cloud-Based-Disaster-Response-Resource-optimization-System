package com.disaster.service;

import com.disaster.dto.WeatherDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Rule-based weather risk assessment.
 *
 * <p>This is deliberately NOT machine learning - it is a small, transparent
 * set of thresholds evaluated against the current weather payload. Each rule
 * produces a factor (type + severity + message) and the dominant factor
 * drives the overall {@code riskLevel}: LOW, MEDIUM, HIGH or EXTREME.
 *
 * <p>The assessment returns three human-readable collections alongside the
 * level: {@code riskReasons} (why the level was raised), and
 * {@code recommendedActions} (practical safety advice per detected hazard).
 *
 * <p>The output is an <em>assessment</em>, never an official government
 * warning - the UI labels it "Weather Risk Assessment". All thresholds are in
 * metric units (OpenWeather {@code units=metric}): temperature in Celsius,
 * wind in km/h, visibility in metres, precipitation as a probability in %.</p>
 */
@Service
public class WeatherRiskService {

    // --- Heat risk thresholds (°C) ---
    private static final double HEAT_EXTREME_C = 42.0;
    private static final double HEAT_HIGH_C = 37.0;
    private static final double HEAT_MEDIUM_C = 33.0;
    private static final double HEAT_INDEX_HUMIDITY_PCT = 60.0;

    // --- Wind risk thresholds (km/h) ---
    private static final double WIND_EXTREME_KMH = 62.0;
    private static final double WIND_HIGH_KMH = 40.0;
    private static final double WIND_MEDIUM_KMH = 25.0;

    // --- Visibility risk thresholds (metres) ---
    private static final int VISIBILITY_EXTREME_M = 200;
    private static final int VISIBILITY_HIGH_M = 1000;
    private static final int VISIBILITY_MEDIUM_M = 3000;

    // --- Precipitation risk thresholds (%) ---
    private static final double RAIN_EXTREME_PCT = 90.0;
    private static final double RAIN_HIGH_PCT = 75.0;
    private static final double RAIN_MEDIUM_PCT = 50.0;

    private static final String LOW = "LOW";
    private static final String MEDIUM = "MEDIUM";
    private static final String HIGH = "HIGH";
    private static final String EXTREME = "EXTREME";

    /**
     * Practical safety advice keyed by hazard type. Advice is generic and
     * educational; it must never be presented as an official directive.
     */
    private static final Map<String, List<String>> ACTIONS_BY_TYPE = Map.of(
            "HEAT", List.of(
                    "Stay hydrated and drink water frequently.",
                    "Avoid strenuous outdoor activity, especially during the hottest part of the day.",
                    "Check on elderly people, children and those with chronic health conditions."),
            "WIND", List.of(
                    "Secure or bring indoors any loose objects on balconies, roofs and yards.",
                    "Stay away from trees, scaffolding and large structures.",
                    "If driving, expect crosswinds and keep both hands on the wheel."),
            "STORM", List.of(
                    "Move indoors and stay away from windows.",
                    "Avoid open fields, tall objects and bodies of water until the storm passes.",
                    "Unplug sensitive electronics and avoid using wired phones during lightning."),
            "RAINFALL", List.of(
                    "Avoid flooded roads, low-lying areas and waterlogged underpasses.",
                    "Delay non-essential travel until rainfall eases.",
                    "Move valuables to higher ground if your area is prone to flooding."),
            "VISIBILITY", List.of(
                    "Reduce speed and switch on headlights or fog lights when driving.",
                    "Maintain extra distance from the vehicle ahead.",
                    "Avoid travel altogether if visibility is critically low."));

    /** Default advice used when no hazard is detected. */
    private static final List<String> DEFAULT_ACTIONS = List.of(
            "No specific safety action is required at this time.",
            "Continue monitoring local conditions and official advisories.");

    /**
     * Fills the risk fields of the supplied DTO (dominant factor + full list)
     * and returns it for chaining.
     */
    public WeatherDTO assess(WeatherDTO dto) {
        if (dto == null) {
            return null;
        }

        List<RiskFactor> factors = new ArrayList<>();
        evaluateHeat(dto, factors);
        evaluateWind(dto, factors);
        evaluateVisibility(dto, factors);
        evaluatePrecipitation(dto, factors);
        evaluateCondition(dto, factors);

        RiskFactor dominant = null;
        for (RiskFactor factor : factors) {
            if (dominant == null
                    || factor.level.score() > dominant.level.score()
                    || (factor.level.score() == dominant.level.score()
                    && factor.type.priority() < dominant.type.priority())) {
                dominant = factor;
            }
        }

        if (dominant == null) {
            dto.setRiskLevel(LOW);
            dto.setRiskType("NONE");
            dto.setRiskMessage("No significant weather-related risk detected.");
            dto.setAlert("");
        } else {
            dto.setRiskLevel(dominant.level.name());
            dto.setRiskType(dominant.type.name());
            dto.setRiskMessage(dominant.message);
            // Derive the legacy `alert` text from the assessment so it never
            // claims to be an official warning.
            dto.setAlert(dominant.message);
        }
        dto.setRiskFactors(factors.stream().map(f -> f.message).toList());
        dto.setRiskReasons(factors.stream().map(f -> f.message).toList());
        dto.setRecommendedActions(recommendedActions(factors));
        return dto;
    }

    /**
     * Builds the safety-action list from every distinct hazard type detected,
     * falling back to generic advice when conditions are benign.
     */
    private List<String> recommendedActions(List<RiskFactor> factors) {
        List<String> actions = factors.stream()
                .map(f -> f.type.name())
                .distinct()
                .flatMap(t -> ACTIONS_BY_TYPE.getOrDefault(t, List.of()).stream())
                .toList();
        return actions.isEmpty() ? DEFAULT_ACTIONS : actions;
    }

    private void evaluateHeat(WeatherDTO dto, List<RiskFactor> factors) {
        double temp = dto.getTemperature();
        if (temp >= HEAT_EXTREME_C) {
            factors.add(new RiskFactor(Level.EXTREME, Type.HEAT,
                    "Extreme heat: temperature of " + fmt(temp) + "°C - risk of heat stroke"));
        } else if (temp >= HEAT_HIGH_C) {
            factors.add(new RiskFactor(Level.HIGH, Type.HEAT,
                    "High heat: temperature of " + fmt(temp) + "°C - heat-related illness possible"));
        } else if (temp >= HEAT_MEDIUM_C) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.HEAT,
                    "Elevated temperature of " + fmt(temp) + "°C - heat risk"));
        }

        if (temp >= HEAT_MEDIUM_C && dto.getHumidity() >= HEAT_INDEX_HUMIDITY_PCT) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.HEAT,
                    "High humidity (" + dto.getHumidity() + "%) makes the heat feel more severe"));
        }
    }

    private void evaluateWind(WeatherDTO dto, List<RiskFactor> factors) {
        double wind = dto.getWindSpeed();
        if (wind >= WIND_EXTREME_KMH) {
            factors.add(new RiskFactor(Level.EXTREME, Type.WIND,
                    "Storm-force wind: " + fmt(wind) + " km/h - danger of flying debris and structural damage"));
        } else if (wind >= WIND_HIGH_KMH) {
            factors.add(new RiskFactor(Level.HIGH, Type.WIND,
                    "Strong wind: " + fmt(wind) + " km/h - secure loose objects"));
        } else if (wind >= WIND_MEDIUM_KMH) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.WIND,
                    "Moderate wind: " + fmt(wind) + " km/h"));
        }
    }

    private void evaluateVisibility(WeatherDTO dto, List<RiskFactor> factors) {
        int visibility = dto.getVisibility();
        if (visibility <= VISIBILITY_EXTREME_M) {
            factors.add(new RiskFactor(Level.EXTREME, Type.VISIBILITY,
                    "Very low visibility (" + visibility + " m) - driving is extremely hazardous"));
        } else if (visibility <= VISIBILITY_HIGH_M) {
            factors.add(new RiskFactor(Level.HIGH, Type.VISIBILITY,
                    "Low visibility (" + visibility + " m) - take extra caution on roads"));
        } else if (visibility <= VISIBILITY_MEDIUM_M) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.VISIBILITY,
                    "Reduced visibility (" + visibility + " m)"));
        }
    }

    private void evaluatePrecipitation(WeatherDTO dto, List<RiskFactor> factors) {
        double rain = dto.getRainProbability();
        if (rain >= RAIN_EXTREME_PCT) {
            factors.add(new RiskFactor(Level.EXTREME, Type.RAINFALL,
                    "Very high rainfall probability (" + fmt(rain) + "%) - flooding risk"));
        } else if (rain >= RAIN_HIGH_PCT) {
            factors.add(new RiskFactor(Level.HIGH, Type.RAINFALL,
                    "High rainfall probability (" + fmt(rain) + "%) - localised flooding possible"));
        } else if (rain >= RAIN_MEDIUM_PCT) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.RAINFALL,
                    "Moderate rainfall probability (" + fmt(rain) + "%)"));
        }
    }

    private void evaluateCondition(WeatherDTO dto, List<RiskFactor> factors) {
        String condition = dto.getWeatherCondition() == null ? "" : dto.getWeatherCondition().toLowerCase(Locale.ROOT);
        if (condition.contains("thunderstorm")) {
            factors.add(new RiskFactor(Level.EXTREME, Type.STORM,
                    "Thunderstorm conditions - risk of lightning, hail and flash flooding"));
        } else if (condition.contains("tornado") || condition.contains("hurricane") || condition.contains("cyclone")) {
            factors.add(new RiskFactor(Level.EXTREME, Type.STORM,
                    "Severe storm system detected - seek shelter immediately"));
        } else if (condition.contains("storm") || condition.contains("stormy")) {
            factors.add(new RiskFactor(Level.HIGH, Type.STORM,
                    "Storm conditions - strong gusts and heavy precipitation likely"));
        } else if (condition.contains("rain") || condition.contains("drizzle")) {
            factors.add(new RiskFactor(Level.MEDIUM, Type.RAINFALL,
                    "Rainy conditions - wet roads and reduced visibility"));
        }
    }

    private static String fmt(double value) {
        return Math.round(value * 10.0) / 10.0 + "";
    }

    private enum Level {
        LOW(0), MEDIUM(1), HIGH(2), EXTREME(3);

        private final int rank;

        Level(int rank) {
            this.rank = rank;
        }

        int score() {
            return rank;
        }
    }

    private enum Type {
        STORM(0), HEAT(1), WIND(2), RAINFALL(3), VISIBILITY(4);

        private final int priorityValue;

        Type(int priority) {
            this.priorityValue = priority;
        }

        int priority() {
            return priorityValue;
        }
    }

    private record RiskFactor(Level level, Type type, String message) {}
}
