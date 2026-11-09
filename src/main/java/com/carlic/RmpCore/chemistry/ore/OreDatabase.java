package com.carlic.RmpCore.chemistry.ore;

import com.carlic.RmpCore.RmpCore;
import com.carlic.RmpCore.chemistry.mineral.MineralDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class OreDatabase {

    private static final String RESOURCE_PATH =
            "/data/rmp_core/chemistry/ores.json";

    private static final Gson GSON = new Gson();

    private final List<OreData> ores;

    private OreDatabase(List<OreData> ores) {
        this.ores = List.copyOf(ores);
    }

    public static OreDatabase load(MineralDatabase mineralDatabase) {

        if (mineralDatabase == null) {
            throw new IllegalArgumentException(
                    "MineralDatabase cannot be null."
            );
        }

        try (InputStream inputStream =
                     RmpCore.class.getResourceAsStream(RESOURCE_PATH)) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Cannot find resource: " + RESOURCE_PATH
                );
            }

            Type listType = new TypeToken<List<OreData>>() {}.getType();

            List<OreData> loadedOres = GSON.fromJson(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    ),
                    listType
            );

            if (loadedOres == null || loadedOres.isEmpty()) {
                throw new IllegalStateException(
                        "Ore database is empty."
                );
            }

            validateOres(loadedOres, mineralDatabase);

            OreDatabase database = new OreDatabase(loadedOres);

            RmpCore.LOGGER.info(
                    "RMP Ore Database loaded: {} ores.",
                    database.getOreCount()
            );

            return database;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to load RMP Ore Database.",
                    exception
            );
        }
    }

    private static void validateOres(
            List<OreData> ores,
            MineralDatabase mineralDatabase
    ) {
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();

        for (OreData ore : ores) {

            if (ore == null) {
                throw new IllegalStateException(
                        "Ore database contains a null entry."
                );
            }

            if (!ids.add(ore.id())) {
                throw new IllegalStateException(
                        "Duplicate ore ID: " + ore.id()
                );
            }

            String normalizedName =
                    ore.name().toLowerCase(Locale.ROOT);

            if (!names.add(normalizedName)) {
                throw new IllegalStateException(
                        "Duplicate ore name: " + ore.name()
                );
            }

            // Validate mineral composition
            for (String mineralId :
                    ore.mineralComposition().keySet()) {

                if (mineralDatabase.getById(mineralId) == null) {
                    throw new IllegalStateException(
                            "Unknown mineral '" + mineralId
                                    + "' in ore '" + ore.id() + "'."
                    );
                }
            }
        }
    }

    public List<OreData> getAll() {
        return ores;
    }

    public OreData getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        return ores.stream()
                .filter(ore -> ore.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public int getOreCount() {
        return ores.size();
    }
}