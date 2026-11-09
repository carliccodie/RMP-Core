
package com.carlic.RmpCore.chemistry;

import com.carlic.RmpCore.chemistry.element.ElementData;
import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.carlic.RmpCore.chemistry.compound.ChemicalFormulaParser;

import java.util.Map;

public final class MolarMassCalculator {

    private final ElementDatabase elementDatabase;

    public MolarMassCalculator(ElementDatabase elementDatabase) {
        if (elementDatabase == null) {
            throw new IllegalArgumentException(
                    "ElementDatabase cannot be null."
            );
        }

        this.elementDatabase = elementDatabase;
    }

    /**
     * Calculates the molar mass of a chemical formula.
     *
     * @param formula chemical formula, for example H2O or CaCO3
     * @return molar mass in g/mol
     */
    public double calculate(String formula) {

        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException(
                    "Chemical formula cannot be blank."
            );
        }

        Map<String, Integer> composition =
                ChemicalFormulaParser.parse(formula);

        double molarMass = 0.0;

        for (Map.Entry<String, Integer> entry :
                composition.entrySet()) {

            String symbol = entry.getKey();
            int atomCount = entry.getValue();

            ElementData element =
                    elementDatabase.getBySymbol(symbol);

            if (element == null) {
                throw new IllegalArgumentException(
                        "Unknown element symbol: " + symbol
                );
            }

            molarMass += element.atomicMass() * atomCount;
        }

        if (!Double.isFinite(molarMass) || molarMass <= 0.0) {
            throw new IllegalStateException(
                    "Invalid calculated molar mass."
            );
        }

        return molarMass;
    }

    /**
     * Calculates the mass percentage of an element
     * in a chemical formula.
     *
     * @param formula chemical formula
     * @param elementSymbol element symbol
     * @return mass percentage from 0 to 100
     */
    public double calculateElementMassPercentage(
            String formula,
            String elementSymbol
    ) {

        if (elementSymbol == null || elementSymbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Element symbol cannot be blank."
            );
        }

        Map<String, Integer> composition =
                ChemicalFormulaParser.parse(formula);

        Integer atomCount = composition.get(elementSymbol);

        if (atomCount == null) {
            return 0.0;
        }

        ElementData element =
                elementDatabase.getBySymbol(elementSymbol);

        if (element == null) {
            throw new IllegalArgumentException(
                    "Unknown element symbol: " + elementSymbol
            );
        }

        double elementMass =
                element.atomicMass() * atomCount;

        double molarMass = calculate(formula);

        return elementMass / molarMass * 100.0;
    }
}
