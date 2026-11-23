package net.wooden.tutorial;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

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
	public static final Block MAPLE_PLANKS = planks("maple_planks", MapColor.COLOR_ORANGE);
	public static final Block STRIPPED_MAPLE_LOG = strippedLog("stripped_maple_log", MapColor.COLOR_ORANGE);

	// Willow: green wood (Lesson 2)
	public static final Block WILLOW_PLANKS = planks("willow_planks", MapColor.COLOR_GREEN);
	public static final Block STRIPPED_WILLOW_LOG = strippedLog("stripped_willow_log", MapColor.COLOR_GREEN);

	// Reed: yellow, bamboo-like wood (Lesson 3)
	public static final Block REED_PLANKS = planks("reed_planks", MapColor.COLOR_YELLOW);
	public static final Block STRIPPED_REED_LOG = strippedLog("stripped_reed_log", MapColor.COLOR_YELLOW);

	// Ember: a Nether wood that does not burn (Lesson 4). Copies crimson planks, so it does not burn either.
	public static final Block EMBER_PLANKS = register("ember_planks", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).mapColor(MapColor.CRIMSON_STEM));
	public static final Block STRIPPED_EMBER_LOG = register("stripped_ember_log", RotatedPillarBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_CRIMSON_STEM).mapColor(MapColor.CRIMSON_STEM));

	private TutorialBlocks() {
	}

	/** Called by {@link TutorialMod}: loading this class registers the blocks above. */
	static void init() {
	}

	private static Block planks(String name, MapColor color) {
		return register(name, Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(color));
	}

	private static Block strippedLog(String name, MapColor color) {
		return register(name, RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).mapColor(color));
	}

	/** Registers a block and its item, the same way vanilla does. */
	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Identifier id = Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name);
		Block block = Blocks.register(ResourceKey.create(Registries.BLOCK, id), factory, properties);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		return block;
	}
}
