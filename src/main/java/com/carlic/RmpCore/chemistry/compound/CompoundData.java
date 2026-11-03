package com.carlic.RmpCore.chemistry.compound;

import java.util.Map;

public record CompoundData(
        String id,
        String name,
        String vietnameseName,
        String chemicalFormula,
        CompoundCategory category,
        Map<String, Integer> composition
) {

    public CompoundData {
        composition = Map.copyOf(composition);
    }
}