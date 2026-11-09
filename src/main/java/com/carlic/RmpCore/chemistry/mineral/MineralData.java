package com.carlic.RmpCore.chemistry.mineral;

import java.util.List;

public record MineralData(
        String id,
        String name,
        String vietnameseName,
        String chemicalFormula,
        MineralCategory category,
        MineralFormulaType formulaType,
        double mohsHardness,
        List<String> elementSymbols
) {

    public MineralData {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Mineral ID cannot be blank."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Mineral name cannot be blank."
            );
        }

        if (vietnameseName == null || vietnameseName.isBlank()) {
            throw new IllegalArgumentException(
                    "Mineral Vietnamese name cannot be blank."
            );
        }

        if (chemicalFormula == null || chemicalFormula.isBlank()) {
            throw new IllegalArgumentException(
                    "Chemical formula cannot be blank."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Mineral category cannot be null."
            );
        }

        if (formulaType == null) {
            throw new IllegalArgumentException(
                    "Mineral formula type cannot be null."
            );
        }

        if (!Double.isFinite(mohsHardness)
                || mohsHardness <= 0.0
                || mohsHardness > 10.0) {
            throw new IllegalArgumentException(
                    "Invalid Mohs hardness: " + mohsHardness
            );
        }

        if (elementSymbols == null || elementSymbols.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mineral element symbols cannot be empty."
            );
        }

        elementSymbols = List.copyOf(elementSymbols);
    }
}