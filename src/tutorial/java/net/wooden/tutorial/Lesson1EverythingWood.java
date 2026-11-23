package net.wooden.tutorial;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 1: one wood with everything</h2>
 *
 * The easiest way to use Wooden API. These few lines give the maple wood:
 * <ul>
 *   <li>a sign and a wall sign (one item)</li>
 *   <li>a hanging sign and a wall hanging sign (one item)</li>
 *   <li>a boat and a boat with chest (items, entities and rendering)</li>
 *   <li>a shelf</li>
 * </ul>
 * All of them are also put in the vanilla creative tabs, next to the vanilla wood of the same kind, and they burn
 * as furnace fuel like vanilla wood.
 *
 * <p>Names: everything is named after the id. {@code wooden_tutorial:maple} gives {@code maple_sign},
 * {@code maple_hanging_sign}, {@code maple_boat}, {@code maple_chest_boat} and {@code maple_shelf}.
 */
public final class Lesson1EverythingWood {
	public static final WoodSet MAPLE = WoodSet.builder(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "maple"))
			.planks(TutorialBlocks.MAPLE_PLANKS)              // used for recipes and sign particles
			.strippedLog(TutorialBlocks.STRIPPED_MAPLE_LOG)   // used for the hanging sign and shelf recipes
			.mapColor(MapColor.COLOR_ORANGE)                  // how the blocks look on a map (use your planks color)
			.all()                                            // = signs().hangingSigns().boats().shelf()
			.register();                                      // registers everything; keep the WoodSet to use it later

	private Lesson1EverythingWood() {
	}

	static void init() {
	}
}
