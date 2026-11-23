package net.wooden.tutorial;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;
import net.wooden.api.datagen.WoodenDatagen;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 7b: data generation (option 2, inside your own providers)</h2>
 *
 * If your mod already has a model provider, a recipe provider, tag providers and so on, don't add Wooden API's
 * providers next to them. Call one {@code WoodenDatagen.generate...} method in each of your providers instead.
 * The result is the same files as option 1 ({@link TutorialDataGen}).
 *
 * <p>This class is a complete example, but it is <b>not used</b> by the tutorial (only one of the two may run,
 * because both write the same files). To use it, put {@code net.wooden.tutorial.TutorialDataGenOption2} instead
 * of {@code TutorialDataGen} under {@code "fabric-datagen"} in {@code fabric.mod.json}. In your own mod, add these
 * lines to the providers you already have (and keep your own blocks in them, like {@link TutorialDataGen} does).
 */
public class TutorialDataGenOption2 implements DataGeneratorEntrypoint {
	/** The woods this mod made with Wooden API. */
	private static final WoodSet[] WOODS = {Lesson1EverythingWood.MAPLE, Lesson2PickParts.WILLOW, Lesson3Rafts.REED,
			Lesson4NetherWood.EMBER};

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(Models::new);
		pack.addProvider(Recipes::new);
		pack.addProvider(LootTables::new);
		pack.addProvider(BlockTags::new);
		pack.addProvider(ItemTags::new);
		pack.addProvider(EntityTypeTags::new);
		pack.addProvider(English::new);
	}

	private static class Models extends FabricModelProvider {
		Models(FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators gen) {
			// signs, hanging signs, shelves: blockstates + block models
			WoodenDatagen.generateBlockStateModels(gen, WOODS);
		}

		@Override
		public void generateItemModels(ItemModelGenerators gen) {
			// boat and chest boat item models (flat textures/item/<wood>_boat.png)
			WoodenDatagen.generateItemModels(gen, WOODS);
		}
	}

	private static class Recipes extends FabricRecipeProvider {
		Recipes(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider lookup,
				BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
			return new net.minecraft.data.recipes.RecipeProvider(recipes, advancements) {
				@Override
				public void buildRecipes() {
					// vanilla-style recipes: sign, hanging sign, boat, chest boat, shelf
					WoodenDatagen.generateRecipes(this, output, WOODS);
				}
			};
		}

		@Override
		public String getName() {
			return "Wooden Tutorial Recipes";
		}
	}

	private static class LootTables extends FabricBlockLootSubProvider {
		LootTables(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		public void generate() {
			// each block drops itself (wall signs reuse the standing sign's loot table, like vanilla)
			WoodenDatagen.generateLootTables(this, WOODS);
		}
	}

	private static class BlockTags extends FabricTagsProvider.BlockTagsProvider {
		BlockTags(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider lookup) {
			// minecraft:standing_signs, wall_signs, ceiling_hanging_signs, ..., also makes them mineable with an axe
			WoodenDatagen.generateBlockTags((tag, block) -> builder(tag).add(block.builtInRegistryHolder().key()), WOODS);
		}
	}

	private static class ItemTags extends FabricTagsProvider.ItemTagsProvider {
		ItemTags(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider lookup) {
			// minecraft:signs, hanging_signs, boats, chest_boats, shelves
			WoodenDatagen.generateItemTags((tag, item) -> builder(tag).add(item.builtInRegistryHolder().key()), WOODS);
		}
	}

	private static class EntityTypeTags extends FabricTagsProvider.EntityTypeTagsProvider {
		EntityTypeTags(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider lookup) {
			// the boat entity in #minecraft:boat
			WoodenDatagen.generateEntityTypeTags((tag, type) -> builder(tag).add(type.builtInRegistryHolder().key()), WOODS);
		}
	}

	private static class English extends FabricLanguageProvider {
		English(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider lookup, TranslationBuilder builder) {
			for (WoodSet wood : WOODS) {
				WoodenDatagen.generateTranslations(builder, wood);
			}
		}
	}
}
