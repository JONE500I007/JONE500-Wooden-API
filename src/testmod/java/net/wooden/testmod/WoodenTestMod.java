package net.wooden.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.wooden.api.wood.WoodSet;

/**
 * Example mod showing how another modder uses Wooden API.
 * Only loaded by the "Testmod Client" / "Testmod Server" runs; not part of the published jar.
 */
public class WoodenTestMod implements ModInitializer {
	public static final String MOD_ID = "wooden_test";

	// The mod's own wood blocks (Wooden API does not make these).
	public static final Block MAPLE_PLANKS = registerBlock("maple_planks", Block::new,
			AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).mapColor(MapColor.ORANGE));
	public static final Block STRIPPED_MAPLE_LOG = registerBlock("stripped_maple_log", PillarBlock::new,
			AbstractBlock.Settings.copy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.ORANGE));

	// ---- This is all a modder writes to get sign, hanging sign, boat, chest boat and shelf ----
	public static final WoodSet MAPLE = WoodSet.builder(Identifier.of(MOD_ID, "maple"))
			.planks(MAPLE_PLANKS)
			.strippedLog(STRIPPED_MAPLE_LOG)
			.mapColor(MapColor.ORANGE)
			.all()
			.register();

	@Override
	public void onInitialize() {
		// Static fields above are registered when this class loads.
	}

	private static Block registerBlock(String name, java.util.function.Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
		Block block = Blocks.register(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name)), factory, settings);
		Items.register(block);
		return block;
	}
}
