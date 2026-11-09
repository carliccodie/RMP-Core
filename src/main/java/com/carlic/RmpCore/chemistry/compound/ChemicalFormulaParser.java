package com.carlic.RmpCore.chemistry.compound;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChemicalFormulaParser {

    private final String formula;
    private int position;

    private ChemicalFormulaParser(String formula) {
        this.formula = formula;
        this.position = 0;
    }

    public static Map<String, Integer> parse(String formula) {

        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException(
                    "Chemical formula cannot be empty."
            );
        }

        ChemicalFormulaParser parser =
                new ChemicalFormulaParser(formula);

        Map<String, Integer> result = new LinkedHashMap<>();

        while (parser.position < parser.formula.length()) {

            int segmentMultiplier = parser.readNumber();

            if (segmentMultiplier == 0) {
                segmentMultiplier = 1;
            }

            Map<String, Integer> segment =
                    parser.parseSequence('\0');

            if (segment.isEmpty()) {
                throw parser.error("Empty formula segment.");
            }

            parser.merge(result, segment, segmentMultiplier);

            if (parser.position >= parser.formula.length()) {
                break;
            }

            char separator = parser.formula.charAt(parser.position);

            if (separator != '·' && separator != '.') {
                throw parser.error(
                        "Unexpected character: " + separator
                );
            }

            parser.position++;
        }

        return Map.copyOf(result);
    }

    private Map<String, Integer> parseSequence(char closingBracket) {

        Map<String, Integer> result = new LinkedHashMap<>();

        while (position < formula.length()) {

            char current = formula.charAt(position);

            if (current == '·' || current == '.') {
                break;
            }

            if (isClosingBracket(current)) {

                if (closingBracket == '\0') {
                    throw error("Unexpected closing bracket.");
                }

                if (current != closingBracket) {
                    throw error("Mismatched closing bracket.");
                }

                position++;
                return result;
            }

            if (isOpeningBracket(current)) {

                char expectedClosing = getClosingBracket(current);
                position++;

                Map<String, Integer> group =
                        parseSequence(expectedClosing);

                if (group.isEmpty()) {
                    throw error("Empty group.");
                }

                int multiplier = readNumber();

                if (multiplier == 0) {
                    multiplier = 1;
                }

                merge(result, group, multiplier);
                continue;
            }

            if (Character.isUpperCase(current)) {

                String symbol = readElementSymbol();

                int amount = readNumber();

                if (amount == 0) {
                    amount = 1;
                }

                addAmount(result, symbol, amount);
                continue;
            }

            throw error("Invalid character: " + current);
        }

        if (closingBracket != '\0') {
            throw error("Missing closing bracket: " + closingBracket);
        }

        return result;
    }

    private String readElementSymbol() {

        StringBuilder symbol = new StringBuilder();

        symbol.append(formula.charAt(position));
        position++;

        if (position < formula.length()
                && Character.isLowerCase(formula.charAt(position))) {

            symbol.append(formula.charAt(position));
            position++;
        }

        return symbol.toString();
    }

    private int readNumber() {
        int start = position;

        while (position < formula.length()
                && Character.isDigit(formula.charAt(position))) {
            position++;
        }

        // Không có chữ số: trả về 0 để các phương thức gọi
        // hiểu rằng công thức không ghi chỉ số (mặc định là 1).
        if (start == position) {
            return 0;
        }

        try {
            int number = Integer.parseInt(
                    formula.substring(start, position)
            );

            if (number <= 0) {
                throw error("Number must be greater than zero.");
            }

            return number;

        } catch (NumberFormatException e) {
            throw error("Number is too large.");
        }
    }

    private void merge(
            Map<String, Integer> target,
            Map<String, Integer> source,
            int multiplier
    ) {

        for (Map.Entry<String, Integer> entry : source.entrySet()) {

            int amount = Math.multiplyExact(
                    entry.getValue(),
                    multiplier
            );

            addAmount(target, entry.getKey(), amount);
        }
    }

    private void addAmount(
            Map<String, Integer> target,
            String symbol,
            int amount
    ) {

        target.merge(symbol, amount, Math::addExact);
    }

    private boolean isOpeningBracket(char character) {
        return character == '('
                || character == '['
                || character == '{';
    }

    private boolean isClosingBracket(char character) {
        return character == ')'
                || character == ']'
                || character == '}';
    }

    private char getClosingBracket(char openingBracket) {

        return switch (openingBracket) {
            case '(' -> ')';
            case '[' -> ']';
            case '{' -> '}';
            default -> throw error("Invalid opening bracket.");
        };
    }

    private IllegalArgumentException error(String message) {

        return new IllegalArgumentException(
                message + " At position " + position
                        + " in formula: " + formula
        );
    }

    private ChemicalFormulaParser() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated."
        );
    }
}