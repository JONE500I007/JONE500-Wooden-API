package net.wooden.tutorial;

import net.fabricmc.api.ModInitializer;

/**
 * <h1>Wooden API tutorial</h1>
 *
 * This is a small, complete mod that <b>uses</b> Wooden API. Read the files in this order; every file is one
 * lesson and explains itself in its comments:
 *
 * <ol>
 *   <li>{@link TutorialBlocks} - your own planks and stripped logs (Wooden API does not make these)</li>
 *   <li>{@link Lesson1EverythingWood} - the easiest way: one wood with everything ({@code .all()})</li>
 *   <li>{@link Lesson2PickParts} - only the parts you want, and other sounds</li>
 *   <li>{@link Lesson3Rafts} - bamboo-style rafts instead of boats</li>
 *   <li>{@link Lesson4NetherWood} - wood that does not burn, map colors, no vanilla creative tabs</li>
 *   <li>{@link Lesson5OwnCreativeTab} - your own creative tab, filled with the {@code WoodSet} getters</li>
 *   <li>{@link Lesson6UsingWoodSets} - reading a {@code WoodSet} in your own code</li>
 *   <li>{@link TutorialDataGen} - data generation: models, recipes, loot tables, tags and names</li>
 * </ol>
 *
 * <p>Textures are in {@code src/tutorial/resources/assets/wooden_tutorial/textures/} (the README explains which
 * files every part needs). The tutorial woods reuse the same placeholder textures; draw your own.
 *
 * <p>Try it in the dev environment with the <b>Tutorial Client</b> run (Gradle task {@code runTutorialClient}).
 * This code is not part of the published Wooden API jar.
 */
public class TutorialMod implements ModInitializer {
	/** Your mod id. Every block, item and texture of your mod uses it as the namespace. */
	public static final String MOD_ID = "wooden_tutorial";

	@Override
	public void onInitialize() {
		// Wooden API registers things when you call WoodSet.Builder#register(). Do that while your mod initializes:
		// here, each lesson does it in its init() (loading the class runs its static fields).
		// The order matters only in one way: blocks you pass to planks(...) / strippedLog(...) must exist first.
		TutorialBlocks.init();
		Lesson1EverythingWood.init();
		Lesson2PickParts.init();
		Lesson3Rafts.init();
		Lesson4NetherWood.init();
		Lesson5OwnCreativeTab.init();
		Lesson6UsingWoodSets.init();
	}
}
