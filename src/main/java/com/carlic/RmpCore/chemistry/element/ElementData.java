package com.carlic.RmpCore.chemistry.element;

public record ElementData(
        int atomicNumber,
        String symbol,
        String name,
        String vietnameseName,
        double atomicMass,
        ElementCategory category,
        PhysicalState physicalState
) {
}