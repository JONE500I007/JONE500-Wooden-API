package net.wooden.tutorial;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 3: rafts instead of boats</h2>
 *
 * {@code .rafts()} makes a flat raft and raft with chest, like vanilla bamboo, instead of a normal boat. The items
 * are called {@code reed_raft} and {@code reed_chest_raft} ("Reed Raft", "Reed Raft with Chest"), so their item
 * textures are {@code textures/item/reed_raft.png} and {@code textures/item/reed_chest_raft.png}.
 *
 * <p>A raft uses the raft model, so its entity textures {@code textures/entity/boat/reed.png} and
 * {@code textures/entity/chest_boat/reed.png} must have the raft layout: start from vanilla's bamboo raft texture,
 * not the oak boat. (This tutorial reuses the maple placeholder, so it looks odd in game.)
 *
 * <p>In code, a raft is still a "boat": {@code hasBoats()} is true and {@code getBoatItem()} gives the raft item.
 * {@code isRaft()} tells you it is a raft.
 */
public final class Lesson3Rafts {
	public static final WoodSet REED = WoodSet.builder(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "reed"))
			.planks(TutorialBlocks.REED_PLANKS)
			.strippedLog(TutorialBlocks.STRIPPED_REED_LOG)
			.mapColor(MapColor.COLOR_YELLOW)
			.signs()
			.rafts()        // instead of .boats()
			.register();

	private Lesson3Rafts() {
	}

	static void init() {
	}
}
