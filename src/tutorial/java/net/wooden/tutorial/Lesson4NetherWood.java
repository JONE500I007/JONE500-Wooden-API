package net.wooden.tutorial;

import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.WoodType;
import net.minecraft.block.MapColor;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 4: a Nether wood (and other settings)</h2>
 *
 * Every setting of the builder in one place:
 * <ul>
 *   <li>{@code nonFlammable()}: the signs, boats and shelf don't burn and are not furnace fuel, like crimson and
 *       warped wood. Also make your planks/logs non-flammable (see {@link TutorialBlocks#EMBER_PLANKS}).</li>
 *   <li>{@code mapColor(...)}: map color of signs and shelf. {@code hangingSignMapColor(...)}: of hanging signs
 *       (vanilla uses the log color there). If you leave it out it is the same as {@code mapColor}.</li>
 *   <li>{@code woodType(...)}: sounds. Here the crimson (Nether wood) sounds.</li>
 *   <li>{@code blockSetType(...)}: the block set type used to make the wood type (sounds of buttons, doors,
 *       pressure plates). Default: oak.</li>
 *   <li>{@code noCreativeTabs()}: don't put the items into the vanilla creative tabs. Use it when your mod has its
 *       own tab; Lesson 5 makes one with these items.</li>
 * </ul>
 */
public final class Lesson4NetherWood {
	public static final WoodSet EMBER = WoodSet.builder(Identifier.of(TutorialMod.MOD_ID, "ember"))
			.planks(TutorialBlocks.EMBER_PLANKS)
			.strippedLog(TutorialBlocks.STRIPPED_EMBER_LOG)
			.mapColor(MapColor.DULL_PINK)
			.hangingSignMapColor(MapColor.DARK_CRIMSON)
			.woodType(WoodTypeBuilder.copyOf(WoodType.CRIMSON))
			.blockSetType(BlockSetType.CRIMSON)
			.nonFlammable()
			.noCreativeTabs()
			.all()
			.register();

	private Lesson4NetherWood() {
	}

	static void init() {
	}
}
