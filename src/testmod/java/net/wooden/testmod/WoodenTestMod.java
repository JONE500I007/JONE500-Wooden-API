package net.wooden.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.wooden.api.wood.WoodSet;

/**
 * Example mod showing how another modder uses Wooden API.
 * Only loaded by the "Testmod Client" / "Testmod Server" runs; not part of the published jar.
 */
public class WoodenTestMod implements ModInitializer {
	public static final String MOD_ID = "wooden_test";

	// The mod's own wood blocks (Wooden API does not make these).
	public static final Block MAPLE_PLANKS = registerBlock("maple_planks", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(MapColor.COLOR_ORANGE));
	public static final Block STRIPPED_MAPLE_LOG = registerBlock("stripped_maple_log", RotatedPillarBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_ORANGE));

	// ---- This is all a modder writes to get sign, hanging sign, boat, chest boat and shelf ----
	public static final WoodSet MAPLE = WoodSet.builder(Identifier.fromNamespaceAndPath(MOD_ID, "maple"))
			.planks(MAPLE_PLANKS)
			.strippedLog(STRIPPED_MAPLE_LOG)
			.mapColor(MapColor.COLOR_ORANGE)
			.all()
			.register();

	@Override
	public void onInitialize() {
		// Static fields above are registered when this class loads.
	}

	private static Block registerBlock(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
		Block block = Blocks.register(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name)), factory, settings);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		return block;
	}
}
