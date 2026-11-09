package com.carlic.RmpCore;

import com.carlic.RmpCore.chemistry.compound.ChemicalFormulaParser;
import com.carlic.RmpCore.registry.ModItems;
import com.carlic.RmpCore.block.ModBlocks;
import java.util.Map;
//import com.carlic.RmpCore.chemistry.ore.OreDatabase;
import com.carlic.RmpCore.chemistry.ChemistryManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(RmpCore.MOD_ID)
public class RmpCore {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "rmp_core";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "rmp_core" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    // Create a Deferred Register to hold Items which will all be registered under the "rmp_core" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "rmp_core" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // Creates a new Block with the id "rmp_core:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "rmp_core:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "rmp_core:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "rmp_core:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", ()
            -> CreativeModeTab.builder().title(Component.translatable("itemGroup.rmp_core")).withTabsBefore(CreativeModeTabs.COMBAT).icon(()
            -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(ModItems.RAW_SILICON.get());
                output.accept(ModBlocks.SILICON_ORE_ITEM.get());
                output.accept(ModItems.SILICON_ORE_CHUNK.get());
            }).build());

    private ChemistryManager chemistryManager;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public RmpCore(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (rmpcore) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        chemistryManager = ChemistryManager.load();


        LOGGER.info(
                "RMP Chemistry System initialized: {} elements, {} minerals, {} compounds, {} ores.",
                chemistryManager.getElements().getElementCount(),
                chemistryManager.getMinerals().getMineralCount(),
                chemistryManager.getCompounds().getCompoundCount(),
                chemistryManager.getOres().getOreCount()
        );
        testAllOreGrades();
        testTargetElementGrades();
        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));


    }


    private void testAllOreGrades() {
        LOGGER.info("========== RMP ORE VALIDATION ==========");

        int totalOres = chemistryManager.getOres().getAll().size();
        int passedOres = 0;
        int failedOres = 0;

        for (var ore : chemistryManager.getOres().getAll()) {
            try {
                var elementGrades =
                        chemistryManager.getOreGradeCalculator()
                                .calculateAllElementGrades(ore.id());

                boolean valuesValid = !elementGrades.isEmpty()
                        && elementGrades.values().stream()
                        .allMatch(value ->
                                value != null
                                        && Double.isFinite(value)
                                        && value >= 0.0
                        );

                double total = valuesValid
                        ? elementGrades.values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum()
                        : Double.NaN;

                boolean valid = valuesValid
                        && Double.isFinite(total)
                        && Math.abs(total - 100.0) <= 0.001;

                if (valid) {
                    passedOres++;

                    LOGGER.info(
                            "[PASS] {}: elemental mass balance = {}%",
                            ore.name(), total
                    );
                } else {
                    failedOres++;

                    LOGGER.error(
                            "[FAIL] {}: invalid elemental mass balance = {}%",
                            ore.name(), total
                    );
                }

            } catch (Exception e) {
                failedOres++;

                LOGGER.error(
                        "[FAIL] {}: could not calculate ore grade",
                        ore.name(), e
                );
            }
        }

        LOGGER.info(
                "RMP Ore Validation Result: {}/{} passed, {} failed.",
                passedOres, totalOres, failedOres
        );

        LOGGER.info("========================================");
    }

    private void testTargetElementGrades() {
        LOGGER.info("========== RMP TARGET ELEMENT GRADE TEST ==========");

        String[][] gradeTests = {
                {"iron_ore", "Fe"},
                {"copper_ore", "Cu"},
                {"lead_ore", "Pb"},
                {"zinc_ore", "Zn"},
                {"gold_ore", "Au"},
                {"silver_ore", "Ag"},
                {"nickel_ore", "Ni"},
                {"cobalt_ore", "Co"},
                {"tin_ore", "Sn"},
                {"chromium_ore", "Cr"},
                {"manganese_ore", "Mn"},
                {"titanium_ore", "Ti"},
                {"tungsten_ore", "W"},
                {"molybdenum_ore", "Mo"}
        };

        int passed = 0;
        int failed = 0;

        for (String[] test : gradeTests) {
            try {
                double grade = chemistryManager.getOreGradeCalculator()
                        .calculateElementGrade(test[0], test[1]);

                if (Double.isFinite(grade) && grade > 0.0 && grade <= 100.0) {
                    passed++;

                    LOGGER.info(
                            "[GRADE PASS] {} | {} = {}%",
                            test[0], test[1], grade
                    );
                } else {
                    failed++;

                    LOGGER.error(
                            "[GRADE FAIL] {} | {} = {}%",
                            test[0], test[1], grade
                    );
                }

            } catch (Exception e) {
                failed++;

                LOGGER.error(
                        "[GRADE FAIL] {} | {}: calculation error",
                        test[0], test[1], e
                );
            }
        }

        LOGGER.info(
                "RMP Target Grade Test Result: {}/{} passed, {} failed.",
                passed, gradeTests.length, failed
        );

        LOGGER.info("===================================================");
    }


    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID,  value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
