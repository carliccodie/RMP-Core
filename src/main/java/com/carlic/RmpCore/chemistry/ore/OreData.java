
package com.carlic.RmpCore.chemistry.ore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record OreData(
        String id,
        String name,
        String vietnameseName,
        OreCategory category,
        Map<String, Double> mineralComposition,
        String description
) {
    private static final double TOLERANCE = 0.001;

    public OreData {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Ore ID cannot be blank."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Ore name cannot be blank."
            );
        }

        if (vietnameseName == null || vietnameseName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ore Vietnamese name cannot be blank."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Ore category cannot be null."
            );
        }

        if (mineralComposition == null
                || mineralComposition.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ore mineral composition cannot be empty."
            );
        }

        Map<String, Double> compositionCopy =
                new LinkedHashMap<>();

        double totalPercentage = 0.0;

        for (Map.Entry<String, Double> entry
                : mineralComposition.entrySet()) {

            String mineralId = entry.getKey();
            Double percentage = entry.getValue();

            if (mineralId == null || mineralId.isBlank()) {
                throw new IllegalArgumentException(
                        "Mineral ID cannot be blank."
                );
            }

            if (percentage == null
                    || !Double.isFinite(percentage)
                    || percentage <= 0.0
                    || percentage > 100.0) {
                throw new IllegalArgumentException(
                        "Invalid mineral percentage: " + mineralId
                );
            }

            compositionCopy.put(mineralId, percentage);
            totalPercentage += percentage;
        }

        if (!Double.isFinite(totalPercentage)
                || Math.abs(totalPercentage - 100.0) > TOLERANCE) {
            throw new IllegalArgumentException(
                    "Mineral composition must total 100%. Current total: "
                            + totalPercentage
            );
        }

        mineralComposition =
                Collections.unmodifiableMap(compositionCopy);

        if (description == null) {
            description = "";
        }
    }
}
