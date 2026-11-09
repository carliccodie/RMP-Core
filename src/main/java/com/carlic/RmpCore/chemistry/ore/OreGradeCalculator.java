
package com.carlic.RmpCore.chemistry.ore;

import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.carlic.RmpCore.chemistry.mineral.MineralCompositionCalculator;
import com.carlic.RmpCore.chemistry.mineral.MineralData;
import com.carlic.RmpCore.chemistry.mineral.MineralDatabase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OreGradeCalculator {

    private static final double TOLERANCE = 0.001;

    private final MineralDatabase mineralDatabase;
    private final ElementDatabase elementDatabase;
    private final MineralCompositionCalculator mineralCompositionCalculator;
    private final OreDatabase oreDatabase;

    public OreGradeCalculator(
            MineralDatabase mineralDatabase,
            ElementDatabase elementDatabase,
            MineralCompositionCalculator mineralCompositionCalculator,
            OreDatabase oreDatabase
    ) {
        if (mineralDatabase == null
                || elementDatabase == null
                || oreDatabase == null
                || mineralCompositionCalculator == null) {
            throw new IllegalArgumentException(
                    "Ore grade calculator dependencies cannot be null."
            );
        }

        this.oreDatabase = oreDatabase;
        this.mineralDatabase = mineralDatabase;
        this.elementDatabase = elementDatabase;
        this.mineralCompositionCalculator =
                mineralCompositionCalculator;
    }

    /**
     * Calculates elemental mass grades from mineral composition.
     *
     * @param oreId ore database ID
     * @return immutable map of element symbols to mass percentages
     */
    public Map<String, Double> calculateAllElementGrades(String oreId) {

        if (oreId == null || oreId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ore ID cannot be null or blank."
            );
        }

        OreData ore = findOre(oreId);

        Map<String, Double> grades = new LinkedHashMap<>();

        double totalMineralPercentage = 0.0;

        for (Map.Entry<String, Double> entry
                : ore.mineralComposition().entrySet()) {

            String mineralId = entry.getKey();
            Double percentage = entry.getValue();

            if (mineralId == null || mineralId.isBlank()
                    || percentage == null
                    || !Double.isFinite(percentage)
                    || percentage <= 0.0
                    || percentage > 100.0) {
                throw new IllegalArgumentException(
                        "Invalid mineral composition in ore: " + oreId
                );
            }

            totalMineralPercentage += percentage;

            MineralData mineral = mineralDatabase.getById(mineralId);

            if (mineral == null) {
                throw new IllegalArgumentException(
                        "Unknown mineral ID in ore "
                                + oreId + ": " + mineralId
                );
            }

            Map<String, Double> mineralElementPercentages =
                    mineralCompositionCalculator
                            .calculateAllElementMassPercentages(mineralId);

            if (mineralElementPercentages == null
                    || mineralElementPercentages.isEmpty()) {
                throw new IllegalArgumentException(
                        "Mineral has no elemental composition: "
                                + mineralId
                );
            }

            double totalElementPercentage = 0.0;

            for (Map.Entry<String, Double> elementEntry
                    : mineralElementPercentages.entrySet()) {

                String elementSymbol = elementEntry.getKey();
                Double elementPercentage = elementEntry.getValue();

                if (elementSymbol == null
                        || elementSymbol.isBlank()
                        || elementPercentage == null
                        || !Double.isFinite(elementPercentage)
                        || elementPercentage < 0.0
                        || elementPercentage > 100.0) {
                    throw new IllegalArgumentException(
                            "Invalid elemental composition in mineral: "
                                    + mineralId
                    );
                }

                if (elementDatabase.getBySymbol(elementSymbol) == null) {
                    throw new IllegalArgumentException(
                            "Unknown element symbol: " + elementSymbol
                    );
                }

                totalElementPercentage += elementPercentage;

                double contribution =
                        percentage * elementPercentage / 100.0;

                grades.merge(
                        elementSymbol,
                        contribution,
                        Double::sum
                );
            }

            if (!Double.isFinite(totalElementPercentage)
                    || Math.abs(totalElementPercentage - 100.0)
                    > TOLERANCE) {
                throw new IllegalArgumentException(
                        "Mineral elemental mass balance is invalid: "
                                + mineralId
                                + " = " + totalElementPercentage + "%"
                );
            }
        }

        if (!Double.isFinite(totalMineralPercentage)
                || Math.abs(totalMineralPercentage - 100.0)
                > TOLERANCE) {
            throw new IllegalArgumentException(
                    "Ore mineral composition must total 100%: "
                            + oreId + " = " + totalMineralPercentage + "%"
            );
        }

        double totalGrade = grades.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (!Double.isFinite(totalGrade)
                || Math.abs(totalGrade - 100.0) > TOLERANCE) {
            throw new IllegalStateException(
                    "Ore elemental mass balance is invalid: "
                            + oreId + " = " + totalGrade + "%"
            );
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(grades)
        );
    }

    /**
     * Calculates the mass percentage of one element in an ore.
     */
    public double calculateElementGrade(
            String oreId,
            String elementSymbol
    ) {
        if (elementSymbol == null || elementSymbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Element symbol cannot be null or blank."
            );
        }

        if (elementDatabase.getBySymbol(elementSymbol) == null) {
            throw new IllegalArgumentException(
                    "Unknown element symbol: " + elementSymbol
            );
        }

        return calculateAllElementGrades(oreId)
                .getOrDefault(elementSymbol, 0.0);
    }

    private OreData findOre(String oreId) {
        OreData ore = oreDatabase.getById(oreId);

        if (ore == null) {
            throw new IllegalArgumentException(
                    "Unknown ore ID: " + oreId
            );
        }

        return ore;
    }
}
