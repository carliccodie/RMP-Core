package com.carlic.RmpCore.chemistry.mineral;

import com.carlic.RmpCore.RmpCore;
import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MineralDatabase {

    private static final String RESOURCE_PATH =
            "/data/rmp_core/chemistry/minerals.json";

    private final List<MineralData> minerals;
    private final Map<String, MineralData> mineralsById;

    private MineralDatabase(
            List<MineralData> minerals,
            Map<String, MineralData> mineralsById
    ) {
        this.minerals = List.copyOf(minerals);
        this.mineralsById = Map.copyOf(mineralsById);
    }

    public static MineralDatabase load(ElementDatabase elementDatabase) {

        if (elementDatabase == null) {
            throw new IllegalArgumentException(
                    "ElementDatabase cannot be null."
            );
        }

        try (InputStream input =
                     RmpCore.class.getResourceAsStream(RESOURCE_PATH)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Cannot find mineral resource: " + RESOURCE_PATH
                );
            }

            try (InputStreamReader reader =
                         new InputStreamReader(input, StandardCharsets.UTF_8)) {

                JsonElement json = JsonParser.parseReader(reader);

                if (!json.isJsonArray()) {
                    throw new IllegalStateException(
                            "minerals.json must contain a JSON array."
                    );
                }

                MineralData[] data = new Gson().fromJson(
                        json,
                        MineralData[].class
                );

                if (data == null) {
                    throw new IllegalStateException(
                            "Failed to read minerals.json."
                    );
                }

                return createAndValidate(
                        List.of(data),
                        elementDatabase
                );
            }

        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException(
                    "Failed to load mineral database.",
                    e
            );
        }
    }

    private static MineralDatabase createAndValidate(
            List<MineralData> minerals,
            ElementDatabase elementDatabase
    ) {

        Map<String, MineralData> byId = new HashMap<>();
        Set<String> names = new HashSet<>();

        for (MineralData mineral : minerals) {

            if (mineral == null) {
                throw new IllegalStateException(
                        "Found a null mineral in minerals.json."
                );
            }

            if (mineral.id() == null || mineral.id().isBlank()) {
                throw new IllegalStateException(
                        "Mineral has an invalid ID."
                );
            }

            if (mineral.name() == null || mineral.name().isBlank()) {
                throw new IllegalStateException(
                        "Mineral " + mineral.id() + " has no name."
                );
            }

            if (mineral.vietnameseName() == null
                    || mineral.vietnameseName().isBlank()) {
                throw new IllegalStateException(
                        "Mineral " + mineral.id()
                                + " has no Vietnamese name."
                );
            }

            if (mineral.chemicalFormula() == null
                    || mineral.chemicalFormula().isBlank()) {
                throw new IllegalStateException(
                        "Mineral " + mineral.id()
                                + " has no chemical formula."
                );
            }

            if (mineral.category() == null) {
                throw new IllegalStateException(
                        "Mineral " + mineral.id()
                                + " has an invalid category."
                );
            }

            if (!Double.isFinite(mineral.mohsHardness())
                    || mineral.mohsHardness() <= 0
                    || mineral.mohsHardness() > 10) {
                throw new IllegalStateException(
                        "Invalid Mohs hardness for " + mineral.id()
                );
            }

            if (mineral.elementSymbols() == null
                    || mineral.elementSymbols().isEmpty()) {
                throw new IllegalStateException(
                        "Mineral " + mineral.id()
                                + " has no element symbols."
                );
            }

            Set<String> checkedSymbols = new HashSet<>();

            for (String symbol : mineral.elementSymbols()) {

                if (symbol == null || symbol.isBlank()) {
                    throw new IllegalStateException(
                            "Invalid element symbol in mineral "
                                    + mineral.id()
                    );
                }

                if (!checkedSymbols.add(symbol)) {
                    throw new IllegalStateException(
                            "Duplicate element symbol " + symbol
                                    + " in mineral " + mineral.id()
                    );
                }

                if (elementDatabase.getBySymbol(symbol) == null) {
                    throw new IllegalStateException(
                            "Unknown element symbol " + symbol
                                    + " in mineral " + mineral.id()
                    );
                }
            }

            if (byId.putIfAbsent(mineral.id(), mineral) != null) {
                throw new IllegalStateException(
                        "Duplicate mineral ID: " + mineral.id()
                );
            }

            if (!names.add(mineral.name())) {
                throw new IllegalStateException(
                        "Duplicate mineral name: " + mineral.name()
                );
            }
        }

        return new MineralDatabase(minerals, byId);
    }

    public List<MineralData> getAll() {
        return minerals;
    }

    public MineralData getById(String id) {
        if (id == null) {
            return null;
        }

        return mineralsById.get(id);
    }

    public int getMineralCount() {
        return minerals.size();
    }
}