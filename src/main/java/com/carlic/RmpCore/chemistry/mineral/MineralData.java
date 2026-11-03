package com.carlic.RmpCore.chemistry.mineral;

import java.util.List;

public record MineralData(
        String id,
        String name,
        String vietnameseName,
        String chemicalFormula,
        MineralCategory category,
        double mohsHardness,
        List<String> elementSymbols
) {

    public MineralData {
        elementSymbols = List.copyOf(elementSymbols);
    }
}