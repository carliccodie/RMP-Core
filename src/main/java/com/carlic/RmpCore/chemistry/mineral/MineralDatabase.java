package com.carlic.RmpCore.chemistry.mineral;

import com.carlic.RmpCore.RmpCore;
import com.carlic.RmpCore.chemistry.compound.ChemicalFormulaParser;
import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MineralDatabase {

    private static final String RESOURCE_PATH =
            "/data/rmp_core/chemistry/minerals.json";

    private static final Gson GSON = new Gson();

    private final List<MineralData> minerals;

    private MineralDatabase(List<MineralData> minerals) {
        this.minerals = List.copyOf(minerals);
    }

    public static MineralDatabase load(ElementDatabase elementDatabase) {

        if (elementDatabase == null) {
            throw new IllegalArgumentException(
                    "ElementDatabase cannot be null."
            );
        }

        try (InputStream inputStream =
                     RmpCore.class.getResourceAsStream(RESOURCE_PATH)) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Cannot find resource: " + RESOURCE_PATH
                );
            }

            Type listType =
                    new TypeToken<List<MineralData>>() {}.getType();

            List<MineralData> loadedMinerals = GSON.fromJson(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    ),
                    listType
            );

            if (loadedMinerals == null || loadedMinerals.isEmpty()) {
                throw new IllegalStateException(
                        "Mineral database is empty."
                );
            }

            validateMinerals(loadedMinerals, elementDatabase);

            MineralDatabase database = new MineralDatabase(loadedMinerals);

            RmpCore.LOGGER.info(
                    "RMP Mineral Database loaded: {} minerals.",
                    database.getMineralCount()
            );

            return database;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to load RMP Mineral Database.",
                    exception
            );
        }
    }

    private static void validateMinerals(
            List<MineralData> minerals,
            ElementDatabase elementDatabase
    ) {
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();

        for (MineralData mineral : minerals) {

            if (mineral == null) {
                throw new IllegalStateException(
                        "Mineral database contains a null entry."
                );
            }

            if (!ids.add(mineral.id())) {
                throw new IllegalStateException(
                        "Duplicate mineral ID: " + mineral.id()
                );
            }

            String normalizedName =
                    mineral.name().toLowerCase(Locale.ROOT);

            if (!names.add(normalizedName)) {
                throw new IllegalStateException(
                        "Duplicate mineral name: " + mineral.name()
                );
            }

            Set<String> elementSymbols = new HashSet<>();

            for (String symbol : mineral.elementSymbols()) {

                if (symbol == null || symbol.isBlank()) {
                    throw new IllegalStateException(
                            "Blank element symbol in mineral: "
                                    + mineral.id()
                    );
                }

                if (elementDatabase.getBySymbol(symbol) == null) {
                    throw new IllegalStateException(
                            "Unknown element '" + symbol
                                    + "' in mineral '" + mineral.id() + "'."
                    );
                }

                if (!elementSymbols.add(symbol)) {
                    throw new IllegalStateException(
                            "Duplicate element symbol '" + symbol
                                    + "' in mineral '" + mineral.id() + "'."
                    );
                }
            }

            if (mineral.formulaType() == MineralFormulaType.FIXED) {
                validateFixedFormula(mineral, elementSymbols);
            }
        }
    }

    private static void validateFixedFormula(
            MineralData mineral,
            Set<String> declaredSymbols
    ) {
        Map<String, Integer> composition;

        try {
            composition = ChemicalFormulaParser.parse(
                    mineral.chemicalFormula()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Invalid fixed chemical formula for mineral '"
                            + mineral.id() + "': "
                            + mineral.chemicalFormula(),
                    exception
            );
        }

        if (!composition.keySet().equals(declaredSymbols)) {
            throw new IllegalStateException(
                    "Element symbols do not match chemical formula for mineral '"
                            + mineral.id()
                            + "'. Formula elements: "
                            + composition.keySet()
                            + ", declared elements: "
                            + declaredSymbols
            );
        }
    }

    public List<MineralData> getAll() {
        return minerals;
    }

    public MineralData getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        return minerals.stream()
                .filter(mineral -> mineral.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public int getMineralCount() {
        return minerals.size();
    }
}