package org.blackhole.extended_crafting_t.other;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.block.ExtendedCraftingTable;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExtendedCraftingT.MODID);

    public static final DeferredBlock<Block> ExtendedCratingTable =
        BLOCKS.register("extended_crafting_table",() -> new ExtendedCraftingTable(
                BlockBehaviour.Properties.of()
                        .strength(3.0f)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
        )
    );


}
