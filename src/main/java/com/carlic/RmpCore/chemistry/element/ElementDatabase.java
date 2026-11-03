package com.carlic.RmpCore.chemistry.element;

import com.carlic.RmpCore.RmpCore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ElementDatabase {

    private static final String RESOURCE_PATH =
            "/data/rmp_core/chemistry/elements.json";

    private static final int EXPECTED_ELEMENT_COUNT = 118;

    private final List<ElementData> elements;
    private final Map<Integer, ElementData> elementsByAtomicNumber;
    private final Map<String, ElementData> elementsBySymbol;

    private ElementDatabase(
            List<ElementData> elements,
            Map<Integer, ElementData> elementsByAtomicNumber,
            Map<String, ElementData> elementsBySymbol
    ) {
        this.elements = List.copyOf(elements);
        this.elementsByAtomicNumber = Map.copyOf(elementsByAtomicNumber);
        this.elementsBySymbol = Map.copyOf(elementsBySymbol);
    }

    public static ElementDatabase load() {

        try (InputStream input =
                     RmpCore.class.getResourceAsStream(RESOURCE_PATH)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Cannot find chemistry resource: " + RESOURCE_PATH
                );
            }

            try (InputStreamReader reader =
                         new InputStreamReader(input, StandardCharsets.UTF_8)) {

                JsonElement json = JsonParser.parseReader(reader);

                if (!json.isJsonArray()) {
                    throw new IllegalStateException(
                            "elements.json must contain a JSON array."
                    );
                }

                ElementData[] data = new Gson().fromJson(
                        json,
                        ElementData[].class
                );

                if (data == null) {
                    throw new IllegalStateException(
                            "Failed to read chemical elements."
                    );
                }

                return createAndValidate(List.of(data));
            }

        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException(
                    "Failed to load chemical elements from JSON.",
                    e
            );
        }
    }

    private static ElementDatabase createAndValidate(
            List<ElementData> elements
    ) {

        if (elements.size() != EXPECTED_ELEMENT_COUNT) {
            throw new IllegalStateException(
                    "Expected " + EXPECTED_ELEMENT_COUNT
                            + " elements, but found " + elements.size()
            );
        }

        Map<Integer, ElementData> byAtomicNumber = new HashMap<>();
        Map<String, ElementData> bySymbol = new HashMap<>();

        Set<String> names = new HashSet<>();

        for (ElementData element : elements) {

            if (element == null) {
                throw new IllegalStateException(
                        "Found a null element in elements.json."
                );
            }

            if (element.atomicNumber() < 1
                    || element.atomicNumber() > EXPECTED_ELEMENT_COUNT) {
                throw new IllegalStateException(
                        "Invalid atomic number: " + element.atomicNumber()
                );
            }

            if (element.symbol() == null
                    || element.symbol().isBlank()) {
                throw new IllegalStateException(
                        "Element " + element.atomicNumber()
                                + " has an invalid symbol."
                );
            }

            if (element.name() == null || element.name().isBlank()) {
                throw new IllegalStateException(
                        "Element " + element.atomicNumber()
                                + " has an invalid name."
                );
            }

            if (element.vietnameseName() == null
                    || element.vietnameseName().isBlank()) {
                throw new IllegalStateException(
                        "Element " + element.atomicNumber()
                                + " has an invalid Vietnamese name."
                );
            }

            if (!Double.isFinite(element.atomicMass())
                    || element.atomicMass() <= 0) {
                throw new IllegalStateException(
                        "Invalid atomic mass for " + element.symbol()
                );
            }

            if (element.category() == null
                    || element.physicalState() == null) {
                throw new IllegalStateException(
                        "Invalid category or physical state for "
                                + element.symbol()
                );
            }

            if (byAtomicNumber.putIfAbsent(
                    element.atomicNumber(), element) != null) {
                throw new IllegalStateException(
                        "Duplicate atomic number: "
                                + element.atomicNumber()
                );
            }

            if (bySymbol.putIfAbsent(
                    element.symbol(), element) != null) {
                throw new IllegalStateException(
                        "Duplicate chemical symbol: " + element.symbol()
                );
            }

            if (!names.add(element.name())) {
                throw new IllegalStateException(
                        "Duplicate element name: " + element.name()
                );
            }
        }

        for (int number = 1;
             number <= EXPECTED_ELEMENT_COUNT;
             number++) {

            if (!byAtomicNumber.containsKey(number)) {
                throw new IllegalStateException(
                        "Missing element with atomic number: " + number
                );
            }
        }

        return new ElementDatabase(
                elements,
                byAtomicNumber,
                bySymbol
        );
    }

    public List<ElementData> getAll() {
        return elements;
    }

    public ElementData getByAtomicNumber(int atomicNumber) {
        return elementsByAtomicNumber.get(atomicNumber);
    }

    public ElementData getBySymbol(String symbol) {
        if (symbol == null) {
            return null;
        }

        return elementsBySymbol.get(symbol);
    }

    public int getElementCount() {
        return elements.size();
    }
}