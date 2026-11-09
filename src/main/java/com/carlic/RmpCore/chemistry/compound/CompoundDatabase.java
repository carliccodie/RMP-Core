package com.carlic.RmpCore.chemistry.compound;

import com.carlic.RmpCore.RmpCore;
import com.carlic.RmpCore.chemistry.element.ElementDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.carlic.RmpCore.chemistry.compound.ChemicalFormulaParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompoundDatabase {

    private static final String RESOURCE_PATH =
            "/data/rmp_core/chemistry/compounds.json";

    private final List<CompoundData> compounds;
    private final Map<String, CompoundData> compoundsById;

    private CompoundDatabase(
            List<CompoundData> compounds,
            Map<String, CompoundData> compoundsById
    ) {
        this.compounds = List.copyOf(compounds);
        this.compoundsById = Map.copyOf(compoundsById);
    }

    public static CompoundDatabase load(ElementDatabase elementDatabase) {

        if (elementDatabase == null) {
            throw new IllegalArgumentException(
                    "ElementDatabase cannot be null."
            );
        }

        try (InputStream input =
                     RmpCore.class.getResourceAsStream(RESOURCE_PATH)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Cannot find compound resource: " + RESOURCE_PATH
                );
            }

            try (InputStreamReader reader =
                         new InputStreamReader(input, StandardCharsets.UTF_8)) {

                JsonElement json = JsonParser.parseReader(reader);

                if (!json.isJsonArray()) {
                    throw new IllegalStateException(
                            "compounds.json must contain a JSON array."
                    );
                }

                CompoundData[] data = new Gson().fromJson(
                        json,
                        CompoundData[].class
                );

                if (data == null) {
                    throw new IllegalStateException(
                            "Failed to read compounds.json."
                    );
                }

                return createAndValidate(
                        List.of(data),
                        elementDatabase
                );
            }

        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException(
                    "Failed to load compound database.",
                    e
            );
        }
    }

    private static CompoundDatabase createAndValidate(
            List<CompoundData> compounds,
            ElementDatabase elementDatabase
    ) {

        Map<String, CompoundData> byId = new HashMap<>();
        Set<String> names = new HashSet<>();

        for (CompoundData compound : compounds) {

            if (compound == null) {
                throw new IllegalStateException(
                        "Found a null compound in compounds.json."
                );
            }

            if (compound.id() == null || compound.id().isBlank()) {
                throw new IllegalStateException(
                        "Compound has an invalid ID."
                );
            }

            if (compound.name() == null || compound.name().isBlank()) {
                throw new IllegalStateException(
                        "Compound " + compound.id() + " has no name."
                );
            }

            if (compound.vietnameseName() == null
                    || compound.vietnameseName().isBlank()) {
                throw new IllegalStateException(
                        "Compound " + compound.id()
                                + " has no Vietnamese name."
                );
            }

            if (compound.chemicalFormula() == null
                    || compound.chemicalFormula().isBlank()) {
                throw new IllegalStateException(
                        "Compound " + compound.id()
                                + " has no chemical formula."
                );
            }

            if (compound.category() == null) {
                throw new IllegalStateException(
                        "Compound " + compound.id()
                                + " has an invalid category."
                );
            }

            if (compound.composition() == null
                    || compound.composition().isEmpty()) {
                throw new IllegalStateException(
                        "Compound " + compound.id()
                                + " has no composition."
                );
            }

            for (Map.Entry<String, Integer> entry
                    : compound.composition().entrySet()) {

                String symbol = entry.getKey();
                Integer amount = entry.getValue();

                if (symbol == null || symbol.isBlank()) {
                    throw new IllegalStateException(
                            "Invalid element symbol in compound "
                                    + compound.id()
                    );
                }

                if (amount == null || amount <= 0) {
                    throw new IllegalStateException(
                            "Invalid atom count for " + symbol
                                    + " in compound " + compound.id()
                    );
                }

                if (elementDatabase.getBySymbol(symbol) == null) {
                    throw new IllegalStateException(
                            "Unknown element " + symbol
                                    + " in compound " + compound.id()
                    );
                }
            }
            Map<String, Integer> parsedComposition =
                    ChemicalFormulaParser.parse(compound.chemicalFormula());

            if (!parsedComposition.equals(compound.composition())) {

                throw new IllegalStateException(
                        "Chemical formula does not match composition for "
                                + compound.id()
                                + ". Formula: " + parsedComposition
                                + ", JSON: " + compound.composition()
                );
            }

            if (byId.putIfAbsent(compound.id(), compound) != null) {
                throw new IllegalStateException(
                        "Duplicate compound ID: " + compound.id()
                );
            }

            if (!names.add(compound.name())) {
                throw new IllegalStateException(
                        "Duplicate compound name: " + compound.name()
                );
            }
        }

        return new CompoundDatabase(compounds, byId);
    }

    public List<CompoundData> getAll() {
        return compounds;
    }

    public CompoundData getById(String id) {
        if (id == null) {
            return null;
        }

        return compoundsById.get(id);
    }

    public int getCompoundCount() {
        return compounds.size();
    }
}