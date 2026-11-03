
package com.carlic.RmpCore.block;

import com.carlic.RmpCore.RmpCore;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(RmpCore.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RmpCore.MOD_ID);

    public static final DeferredBlock<Block> SILICON_ORE =
            BLOCKS.registerSimpleBlock(
                    "silicon_ore",
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
            );

    public static final DeferredItem<BlockItem> SILICON_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("silicon_ore", SILICON_ORE);

    private ModBlocks() {
    }
}
