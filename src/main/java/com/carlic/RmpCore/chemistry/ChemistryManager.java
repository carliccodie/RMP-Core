package com.carlic.RmpCore.chemistry;

import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.carlic.RmpCore.chemistry.mineral.MineralDatabase;
import com.carlic.RmpCore.chemistry.mineral.MineralCompositionCalculator;
import com.carlic.RmpCore.chemistry.compound.CompoundDatabase;
import com.carlic.RmpCore.chemistry.ore.OreDatabase;
import com.carlic.RmpCore.chemistry.ore.OreGradeCalculator;

public final class ChemistryManager {

    private final ElementDatabase elements;
    private final MineralDatabase minerals;
    private final CompoundDatabase compounds;
    private final OreDatabase ores;

    private final MolarMassCalculator molarMassCalculator;
    private final MineralCompositionCalculator mineralCompositionCalculator;
    private final OreGradeCalculator oreGradeCalculator;

    private ChemistryManager(
            ElementDatabase elements,
            MineralDatabase minerals,
            CompoundDatabase compounds,
            OreDatabase ores,
            MolarMassCalculator molarMassCalculator,
            MineralCompositionCalculator mineralCompositionCalculator,
            OreGradeCalculator oreGradeCalculator
    ) {
        this.elements = elements;
        this.minerals = minerals;
        this.compounds = compounds;
        this.ores = ores;
        this.molarMassCalculator = molarMassCalculator;
        this.mineralCompositionCalculator = mineralCompositionCalculator;
        this.oreGradeCalculator = oreGradeCalculator;
    }

    public static ChemistryManager load() {
        ElementDatabase elements = ElementDatabase.load();
        MineralDatabase minerals = MineralDatabase.load(elements);
        CompoundDatabase compounds = CompoundDatabase.load(elements);
        OreDatabase ores = OreDatabase.load(minerals);

        MolarMassCalculator molarMassCalculator =
                new MolarMassCalculator(elements);

        MineralCompositionCalculator mineralCompositionCalculator =
                new MineralCompositionCalculator(
                        minerals,
                        elements,
                        molarMassCalculator
                );

        OreGradeCalculator oreGradeCalculator =
                new OreGradeCalculator(

                        minerals,
                        elements,
                        mineralCompositionCalculator,
                        ores
                );

        return new ChemistryManager(
                elements,
                minerals,
                compounds,
                ores,
                molarMassCalculator,
                mineralCompositionCalculator,
                oreGradeCalculator
        );
    }

    public ElementDatabase getElements() {
        return elements;
    }

    public MineralDatabase getMinerals() {
        return minerals;
    }

    public CompoundDatabase getCompounds() {
        return compounds;
    }

    public OreDatabase getOres() {
        return ores;
    }

    public MolarMassCalculator getMolarMassCalculator() {
        return molarMassCalculator;
    }

    public MineralCompositionCalculator getMineralCompositionCalculator() {
        return mineralCompositionCalculator;
    }

    public OreGradeCalculator getOreGradeCalculator() {
        return oreGradeCalculator;
    }
}