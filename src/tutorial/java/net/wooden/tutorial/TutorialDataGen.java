package net.wooden.tutorial;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.wooden.api.datagen.WoodenDatagen;

import static net.wooden.tutorial.Lesson1EverythingWood.MAPLE;
import static net.wooden.tutorial.Lesson2PickParts.WILLOW;
import static net.wooden.tutorial.Lesson3Rafts.REED;
import static net.wooden.tutorial.Lesson4NetherWood.EMBER;

/**
 * <h2>Lesson 7: data generation (option 1, the easy way)</h2>
 *
 * Wooden API can write every JSON file its parts need: blockstates, block and item models, recipes, loot tables,
 * tags (signs, boats, shelves, so they are mineable with an axe and work as fuel) and English names.
 *
 * <p><b>One line</b> does all of that for your woods:
 * <pre>{@code
 * WoodenDatagen.addProviders(pack, MAPLE, WILLOW, REED, EMBER);
 * }</pre>
 * This mod also has its own providers (for the planks and logs from {@link TutorialBlocks}). Two providers can't
 * write the same file, and both would write {@code en_us.json}, so it passes {@code false} ("don't write English")
 * and adds the wood names in its own language provider with {@code WoodenDatagen.generateTranslations}.
 *
 * <p>Already have providers for everything? Then call the {@code WoodenDatagen.generate...} methods inside them
 * instead: see {@link TutorialDataGenOption2}.
 *
 * <p>Run it with the Gradle task {@code runDatagen}; the files go to {@code src/tutorial/generated/}.
 * The entrypoint is registered in {@code fabric.mod.json} under {@code "fabric-datagen"}.
 */
public class TutorialDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();

		// Everything for the four woods (without English, see above).
		WoodenDatagen.addProviders(pack, false, MAPLE, WILLOW, REED, EMBER);

		// This mod's own content: planks and stripped logs, plus all names.
		pack.addProvider(OwnModels::new);
		pack.addProvider(OwnLootTables::new);
		pack.addProvider(OwnRecipes::new);
		pack.addProvider(English::new);
	}

	private static final Block[][] PLANKS_AND_LOGS = {
			{TutorialBlocks.MAPLE_PLANKS, TutorialBlocks.STRIPPED_MAPLE_LOG},
			{TutorialBlocks.WILLOW_PLANKS, TutorialBlocks.STRIPPED_WILLOW_LOG},
			{TutorialBlocks.REED_PLANKS, TutorialBlocks.STRIPPED_REED_LOG},
			{TutorialBlocks.EMBER_PLANKS, TutorialBlocks.STRIPPED_EMBER_LOG},
	};

	private static class OwnModels extends FabricModelProvider {
		OwnModels(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator gen) {
			for (Block[] wood : PLANKS_AND_LOGS) {
				gen.registerSimpleCubeAll(wood[0]);
				gen.createLogTexturePool(wood[1]).log(wood[1]);
			}
		}

		@Override
		public void generateItemModels(ItemModelGenerator gen) {
		}
	}

	private static class OwnLootTables extends FabricBlockLootTableProvider {
		OwnLootTables(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		public void generate() {
			for (Block[] wood : PLANKS_AND_LOGS) {
				addDrop(wood[0]);
				addDrop(wood[1]);
			}
		}
	}

	private static class OwnRecipes extends FabricRecipeProvider {
		OwnRecipes(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup lookup, RecipeExporter exporter) {
			return new RecipeGenerator(lookup, exporter) {
				@Override
				public void generate() {
					// 1 stripped log -> 4 planks, like vanilla
					for (Block[] wood : PLANKS_AND_LOGS) {
						createShapeless(RecipeCategory.BUILDING_BLOCKS, wood[0], 4)
								.input(wood[1])
								.group("planks")
								.criterion(hasItem(wood[1]), conditionsFromItem(wood[1]))
								.offerTo(exporter);
					}
				}
			};
		}

		@Override
		public String getName() {
			return "Wooden Tutorial Recipes";
		}
	}

	private static class English extends FabricLanguageProvider {
		English(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup lookup, TranslationBuilder builder) {
			builder.add(TutorialBlocks.MAPLE_PLANKS, "Maple Planks");
			builder.add(TutorialBlocks.STRIPPED_MAPLE_LOG, "Stripped Maple Log");
			builder.add(TutorialBlocks.WILLOW_PLANKS, "Willow Planks");
			builder.add(TutorialBlocks.STRIPPED_WILLOW_LOG, "Stripped Willow Log");
			builder.add(TutorialBlocks.REED_PLANKS, "Reed Planks");
			builder.add(TutorialBlocks.STRIPPED_REED_LOG, "Stripped Reed Log");
			builder.add(TutorialBlocks.EMBER_PLANKS, "Ember Planks");
			builder.add(TutorialBlocks.STRIPPED_EMBER_LOG, "Stripped Ember Log");
			builder.add("itemGroup.wooden_tutorial.woods", "Wooden Tutorial");

			// Wood names made from the id: "maple" -> "Maple Sign", "Maple Boat with Chest", ... (rafts: "Reed Raft")
			WoodenDatagen.generateTranslations(builder, MAPLE);
			WoodenDatagen.generateTranslations(builder, WILLOW);
			WoodenDatagen.generateTranslations(builder, REED);
			// ... or with a wood name you choose, e.g. for an id like "ember_wood" that should read "Ember"
			WoodenDatagen.generateTranslations(builder, EMBER, "Ember");
		}
	}
}
