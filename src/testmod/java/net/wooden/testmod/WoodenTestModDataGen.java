package net.wooden.testmod;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
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
		ModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator gen) {
			gen.registerSimpleCubeAll(MAPLE_PLANKS);
			gen.createLogTexturePool(STRIPPED_MAPLE_LOG).log(STRIPPED_MAPLE_LOG);
		}

		@Override
		public void generateItemModels(ItemModelGenerator gen) {
		}
	}

	private static class LootProvider extends FabricBlockLootTableProvider {
		LootProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		public void generate() {
			addDrop(MAPLE_PLANKS);
			addDrop(STRIPPED_MAPLE_LOG);
		}
	}

	private static class RecipeProvider extends FabricRecipeProvider {
		RecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup lookup, RecipeExporter exporter) {
			return new RecipeGenerator(lookup, exporter) {
				@Override
				public void generate() {
					createShapeless(RecipeCategory.BUILDING_BLOCKS, MAPLE_PLANKS, 4)
							.input(STRIPPED_MAPLE_LOG)
							.group("planks")
							.criterion(hasItem(STRIPPED_MAPLE_LOG), conditionsFromItem(STRIPPED_MAPLE_LOG))
							.offerTo(exporter);
				}
			};
		}

		@Override
		public String getName() {
			return "Wooden Test Recipes";
		}
	}

	private static class EnglishProvider extends FabricLanguageProvider {
		EnglishProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(output, registries);
		}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup lookup, TranslationBuilder builder) {
			builder.add(MAPLE_PLANKS, "Maple Planks");
			builder.add(STRIPPED_MAPLE_LOG, "Stripped Maple Log");
			WoodenDatagen.generateTranslations(builder, MAPLE, "Maple");
		}
	}
}
