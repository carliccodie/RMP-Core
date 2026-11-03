package com.carlic.RmpCore.registry;

import com.carlic.RmpCore.RmpCore;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RmpCore.MOD_ID);

    public static final DeferredItem<Item> RAW_SILICON =
            ITEMS.registerSimpleItem("raw_silicon");

    public static final DeferredItem<Item> SILICON_ORE_CHUNK =
            ITEMS.registerSimpleItem("silicon_ore_chunk");
    private ModItems() {
    }
}