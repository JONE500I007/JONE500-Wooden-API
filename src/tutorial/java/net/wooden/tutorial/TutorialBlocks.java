package net.wooden.tutorial;

import java.util.function.Function;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * <h2>Lesson 0: your own planks and stripped logs</h2>
 *
 * Wooden API makes the <b>signs, hanging signs, boats and shelves</b> of a wood. Planks, logs, stairs, doors and
 * so on are normal blocks that you register yourself, like in any mod.
 *
 * <p>You give Wooden API your planks and stripped log (see {@code planks(...)} and {@code strippedLog(...)} in
 * the lessons). It uses them for the recipes (sign = 6 planks + 1 stick, hanging sign = 6 stripped logs + 2
 * chains, ...) and for the particles when a sign breaks.
 */
public final class TutorialBlocks {
	// Maple: orange wood (Lesson 1)
	public static final Block MAPLE_PLANKS = planks("maple_planks", MapColor.ORANGE);
	public static final Block STRIPPED_MAPLE_LOG = strippedLog("stripped_maple_log", MapColor.ORANGE);

	// Willow: green wood (Lesson 2)
	public static final Block WILLOW_PLANKS = planks("willow_planks", MapColor.GREEN);
	public static final Block STRIPPED_WILLOW_LOG = strippedLog("stripped_willow_log", MapColor.GREEN);

	// Reed: yellow, bamboo-like wood (Lesson 3)
	public static final Block REED_PLANKS = planks("reed_planks", MapColor.YELLOW);
	public static final Block STRIPPED_REED_LOG = strippedLog("stripped_reed_log", MapColor.YELLOW);

	// Ember: a Nether wood that does not burn (Lesson 4). Copies crimson planks, so it does not burn either.
	public static final Block EMBER_PLANKS = register("ember_planks", Block::new,
			AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS).mapColor(MapColor.DULL_PINK));
	public static final Block STRIPPED_EMBER_LOG = register("stripped_ember_log", PillarBlock::new,
			AbstractBlock.Settings.copy(Blocks.STRIPPED_CRIMSON_STEM).mapColor(MapColor.DULL_PINK));

	private TutorialBlocks() {
	}

	/** Called by {@link TutorialMod}: loading this class registers the blocks above. */
	static void init() {
	}

	private static Block planks(String name, MapColor color) {
		return register(name, Block::new, AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).mapColor(color));
	}

	private static Block strippedLog(String name, MapColor color) {
		return register(name, PillarBlock::new, AbstractBlock.Settings.copy(Blocks.STRIPPED_OAK_LOG).mapColor(color));
	}

	/** Registers a block and its item, the same way vanilla does. */
	private static Block register(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
		Block block = Blocks.register(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(TutorialMod.MOD_ID, name)), factory, settings);
		Items.register(block);
		return block;
	}
}
