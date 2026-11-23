package net.wooden.tutorial;

import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.block.WoodType;
import net.minecraft.block.MapColor;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 2: only the parts you want, and other sounds</h2>
 *
 * Instead of {@code .all()} you can pick the parts one by one. Willow here only gets signs and hanging signs, so
 * no boat or shelf is registered (and you don't have to draw their textures):
 * <pre>
 * .signs()          sign + wall sign
 * .hangingSigns()   hanging sign + wall hanging sign
 * .boats()          boat + boat with chest
 * .rafts()          raft + raft with chest (see Lesson 3)
 * .shelf()          shelf
 * </pre>
 *
 * <p><b>Sounds:</b> a wood has a "wood type", which decides the sounds of its signs (and of doors, fence gates and
 * so on if you make them). {@code WoodTypeBuilder.copyOf(WoodType.CHERRY)} copies the cherry sounds. Default: oak.
 */
public final class Lesson2PickParts {
	public static final WoodSet WILLOW = WoodSet.builder(Identifier.of(TutorialMod.MOD_ID, "willow"))
			.planks(TutorialBlocks.WILLOW_PLANKS)
			.strippedLog(TutorialBlocks.STRIPPED_WILLOW_LOG)
			.mapColor(MapColor.GREEN)
			.woodType(WoodTypeBuilder.copyOf(WoodType.CHERRY))   // cherry sounds instead of oak
			.signs()
			.hangingSigns()
			.register();

	/*
	 * Already have your own WoodType (for example because you made doors and fence gates with it)? Pass it
	 * instead, so the signs share it:
	 *
	 *     .woodType(ModWoodTypes.WILLOW)
	 */

	private Lesson2PickParts() {
	}

	static void init() {
	}
}
