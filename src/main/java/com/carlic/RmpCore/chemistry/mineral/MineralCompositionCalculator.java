package com.carlic.RmpCore.chemistry.mineral;

import com.carlic.RmpCore.chemistry.MolarMassCalculator;
import com.carlic.RmpCore.chemistry.compound.ChemicalFormulaParser;
import com.carlic.RmpCore.chemistry.element.ElementDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MineralCompositionCalculator {

    private final MineralDatabase mineralDatabase;
    private final ElementDatabase elementDatabase;
    private final MolarMassCalculator molarMassCalculator;

    public MineralCompositionCalculator(
            MineralDatabase mineralDatabase,
            ElementDatabase elementDatabase,
            MolarMassCalculator molarMassCalculator
    ) {
        if (mineralDatabase == null
                || elementDatabase == null
                || molarMassCalculator == null) {
            throw new IllegalArgumentException(
                    "Mineral calculator dependencies cannot be null."
            );
        }

        this.mineralDatabase = mineralDatabase;
        this.elementDatabase = elementDatabase;
        this.molarMassCalculator = molarMassCalculator;
    }

    /**
     * Calculates the mass percentage of every element in a mineral.
     *
     * @param mineralId mineral database ID
     * @return map of element symbols to mass percentages
     */
    public Map<String, Double> calculateAllElementMassPercentages(
            String mineralId
    ) {
        if (mineralId == null || mineralId.isBlank()) {
            throw new IllegalArgumentException(
                    "Mineral ID cannot be null or blank."
            );
        }

        MineralData mineral = mineralDatabase.getById(mineralId);

        if (mineral == null) {
            throw new IllegalArgumentException(
                    "Unknown mineral ID: " + mineralId
            );
        }
        if (mineral.formulaType() != MineralFormulaType.FIXED) {
            throw new UnsupportedOperationException(
                    "Cannot calculate exact elemental mass percentages for mineral '"
                            + mineral.name()
                            + "' because formula type is "
                            + mineral.formulaType()
                            + ". Only FIXED formulas are supported."
            );
        }

        Map<String, Integer> composition;

        try {
            composition = ChemicalFormulaParser.parse(
                    mineral.chemicalFormula()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Cannot calculate mineral composition for '"
                            + mineral.name()
                            + "' because its formula is variable or unsupported: "
                            + mineral.chemicalFormula(),
                    exception
            );
        }

        double molarMass = molarMassCalculator.calculate(
                mineral.chemicalFormula()
        );

        Map<String, Double> percentages = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : composition.entrySet()) {
            String symbol = entry.getKey();
            int atomCount = entry.getValue();

            if (elementDatabase.getBySymbol(symbol) == null) {
                throw new IllegalArgumentException(
                        "Unknown element symbol in mineral formula: " + symbol
                );
            }

            double elementMass =
                    elementDatabase.getBySymbol(symbol).atomicMass() * atomCount;

            double percentage = elementMass / molarMass * 100.0;

            percentages.put(symbol, percentage);
        }

        return Map.copyOf(percentages);
    }

    /**
     * Calculates the mass percentage of one element in a mineral.
     *
     * @param mineralId mineral database ID
     * @param elementSymbol chemical element symbol
     * @return mass percentage, or 0 if the element is absent
     */
    public double calculateElementMassPercentage(
            String mineralId,
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

        Map<String, Double> percentages =
                calculateAllElementMassPercentages(mineralId);

        return percentages.getOrDefault(elementSymbol, 0.0);
    }
}