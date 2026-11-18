package net.wooden.testmod;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.wooden.api.datagen.WoodenDatagen;

import static net.wooden.testmod.WoodenTestMod.MAPLE;
import static net.wooden.testmod.WoodenTestMod.MAPLE_PLANKS;
import static net.wooden.testmod.WoodenTestMod.STRIPPED_MAPLE_LOG;

public class WoodenTestModDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();

		// One line: models, blockstates, recipes, loot tables and tags for the whole MAPLE wood set.
		// "false" = this mod has its own en_us provider below, so Wooden API should not write one.
		WoodenDatagen.addProviders(pack, false, MAPLE);

		// The mod's own content (planks + stripped log), plus the names.
		pack.addProvider(ModelProvider::new);
		pack.addProvider(LootProvider::new);
		pack.addProvider(RecipeProvider::new);
		pack.addProvider(EnglishProvider::new);
	}

	private static class ModelProvider extends FabricModelProvider {
		ModelProvider(FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators gen) {
			gen.createTrivialCube(MAPLE_PLANKS);
			gen.woodProvider(STRIPPED_MAPLE_LOG).logWithHorizontal(STRIPPED_MAPLE_LOG);
		}

		@Override
		public void generateItemModels(ItemModelGenerators gen) {
		}
	}

	private static class LootProvider extends FabricBlockLootSubProvider {
		LootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		public void generate() {
			dropSelf(MAPLE_PLANKS);
			dropSelf(STRIPPED_MAPLE_LOG);
		}
	}

	private static class RecipeProvider extends FabricRecipeProvider {
		RecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput exporter) {
			return new net.minecraft.data.recipes.RecipeProvider(lookup, exporter) {
				@Override
				public void buildRecipes() {
					shapeless(RecipeCategory.BUILDING_BLOCKS, MAPLE_PLANKS, 4)
							.requires(STRIPPED_MAPLE_LOG)
							.group("planks")
							.unlockedBy(getHasName(STRIPPED_MAPLE_LOG), has(STRIPPED_MAPLE_LOG))
							.save(output);
				}
			};
		}

		@Override
		public String getName() {
			return "Wooden Test Recipes";
		}
	}

	private static class EnglishProvider extends FabricLanguageProvider {
		EnglishProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider lookup, TranslationBuilder builder) {
			builder.add(MAPLE_PLANKS, "Maple Planks");
			builder.add(STRIPPED_MAPLE_LOG, "Stripped Maple Log");
			WoodenDatagen.generateTranslations(builder, MAPLE, "Maple");
		}
	}
}
