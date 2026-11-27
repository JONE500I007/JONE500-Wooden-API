package net.wooden.api.datagen;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.wooden.api.wood.WoodSet;

/**
 * Data generation for {@link WoodSet}s.
 *
 * <h2>Option 1: one line (easiest)</h2>
 * In your {@code DataGeneratorEntrypoint}:
 * <pre>{@code
 * FabricDataGenerator.Pack pack = generator.createPack();
 * WoodenDatagen.addProviders(pack, ModWoods.MAPLE, ModWoods.REDWOOD);
 * }</pre>
 * This generates blockstates, models, recipes, loot tables, tags and English names.
 * If your mod already has its own language provider, use
 * {@link #addProviders(FabricDataGenerator.Pack, boolean, WoodSet...)} with {@code false} instead
 * and call {@link #generateTranslations} from your own provider (two providers cannot write the same
 * {@code en_us.json}).
 *
 * <h2>Option 2: inside your own providers</h2>
 * Call the {@code generate...} methods from the providers you already have:
 * <pre>{@code
 * // FabricModelProvider
 * public void generateBlockStateModels(BlockModelGenerators gen) { WoodenDatagen.generateBlockStateModels(gen, MAPLE); }
 * public void generateItemModels(ItemModelGenerators gen)             { WoodenDatagen.generateItemModels(gen, MAPLE); }
 * // FabricRecipeProvider -> RecipeProvider#buildRecipes()
 * WoodenDatagen.generateRecipes(this, output, MAPLE);
 * // FabricBlockLootSubProvider#generate()
 * WoodenDatagen.generateLootTables(this, MAPLE);
 * // FabricTagsProvider.BlockTagsProvider#addTags()
 * WoodenDatagen.generateBlockTags((tag, block) -> builder(tag).add(block.builtInRegistryHolder().key()), MAPLE);
 * // FabricTagsProvider.ItemTagsProvider#addTags()
 * WoodenDatagen.generateItemTags((tag, item) -> builder(tag).add(item.builtInRegistryHolder().key()), MAPLE);
 * // FabricLanguageProvider#generateTranslations()
 * WoodenDatagen.generateTranslations(builder, MAPLE, "Maple");
 * }</pre>
 */
public final class WoodenDatagen {
	private WoodenDatagen() {
	}

	// ==========================================================================================
	// Option 1: ready-made providers
	// ==========================================================================================

	/** Adds every Wooden API provider (including English names) to the pack. */
	public static void addProviders(FabricDataGenerator.Pack pack, WoodSet... sets) {
		addProviders(pack, true, sets);
	}

	/**
	 * Adds every Wooden API provider to the pack.
	 *
	 * @param includeEnglish {@code false} if your mod has its own {@code en_us} language provider
	 */
	public static void addProviders(FabricDataGenerator.Pack pack, boolean includeEnglish, WoodSet... sets) {
		List<WoodSet> list = List.copyOf(Arrays.asList(sets));
		WoodSet[] array = list.toArray(WoodSet[]::new);

		pack.addProvider((FabricPackOutput output) -> new FabricModelProvider(output) {
			@Override
			public void generateBlockStateModels(BlockModelGenerators gen) {
				WoodenDatagen.generateBlockStateModels(gen, array);
			}

			@Override
			public void generateItemModels(ItemModelGenerators gen) {
				WoodenDatagen.generateItemModels(gen, array);
			}

			@Override
			public String getName() {
				return "Wooden API Models";
			}
		});

		pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricRecipeProvider(output, registries) {
			@Override
			protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput exporter) {
				return new RecipeProvider(lookup, exporter) {
					@Override
					public void buildRecipes() {
						WoodenDatagen.generateRecipes(this, output, array);
					}
				};
			}

			@Override
			public String getName() {
				return "Wooden API Recipes";
			}
		});

		pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricBlockLootSubProvider(output, registries) {
			@Override
			public void generate() {
				WoodenDatagen.generateLootTables(this, array);
			}

			@Override
			public String getName() {
				return "Wooden API Block Loot Tables";
			}
		});

		pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricTagsProvider.BlockTagsProvider(output, registries) {
			@Override
			protected void addTags(HolderLookup.Provider lookup) {
				WoodenDatagen.generateBlockTags((tag, block) -> builder(tag).add(block.builtInRegistryHolder().key()), array);
			}

			@Override
			public String getName() {
				return "Wooden API Block Tags";
			}
		});

		pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricTagsProvider.ItemTagsProvider(output, registries) {
			@Override
			protected void addTags(HolderLookup.Provider lookup) {
				WoodenDatagen.generateItemTags((tag, item) -> builder(tag).add(item.builtInRegistryHolder().key()), array);
			}

			@Override
			public String getName() {
				return "Wooden API Item Tags";
			}
		});

		pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricTagsProvider.EntityTypeTagsProvider(output, registries) {
			@Override
			protected void addTags(HolderLookup.Provider lookup) {
				WoodenDatagen.generateEntityTypeTags((tag, type) -> builder(tag).add(type.builtInRegistryHolder().key()), array);
			}

			@Override
			public String getName() {
				return "Wooden API Entity Type Tags";
			}
		});

		if (includeEnglish) {
			pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) -> new FabricLanguageProvider(output, registries) {
				@Override
				public void generateTranslations(HolderLookup.Provider lookup, TranslationBuilder builder) {
					for (WoodSet set : array) {
						WoodenDatagen.generateTranslations(builder, set);
					}
				}

				@Override
				public String getName() {
					return "Wooden API English";
				}
			});
		}
	}

	// ==========================================================================================
	// Option 2: helpers for your own providers
	// ==========================================================================================

	/**
	 * Blockstates + block models (and the sign/shelf item models).
	 * Needs {@code planks} for signs and {@code strippedLog} for hanging signs and shelves.
	 */
	public static void generateBlockStateModels(BlockModelGenerators gen, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasSigns()) {
				generateSign(gen, set.getSign(), set.getWallSign(), planks(set), blockTexture(set, "_sign"));
			}
			if (set.hasHangingSigns()) {
				generateHangingSign(gen, set.getHangingSign(), set.getWallHangingSign(), strippedLog(set), blockTexture(set, "_hanging_sign"));
			}
			if (set.hasShelf()) {
				gen.createShelf(set.getShelf(), strippedLog(set));
			}
		}
	}

	/**
	 * Same as vanilla's {@code BlockFamilyProvider.sign}: since 26.2 signs are block models.
	 * Texture {@code textures/block/<wood>_sign.png}, particles from the planks.
	 */
	private static void generateSign(BlockModelGenerators gen, Block sign, Block wallSign, Block planks, Material texture) {
		TextureMapping mapping = new TextureMapping()
				.put(TextureSlot.ALL, texture)
				.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(planks));
		gen.blockStateOutput.accept(BlockModelGenerators.createSign(sign,
				variant(gen, ModelTemplates.SIGN_ROT_0, sign, "_rot_0", mapping),
				variant(gen, ModelTemplates.SIGN_ROT_1, sign, "_rot_1", mapping),
				variant(gen, ModelTemplates.SIGN_ROT_2, sign, "_rot_2", mapping),
				variant(gen, ModelTemplates.SIGN_ROT_3, sign, "_rot_3", mapping)));
		MultiVariant wallModel = BlockModelGenerators.plainVariant(ModelTemplates.WALL_SIGN.create(wallSign, mapping, gen.modelOutput));
		gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(wallSign, wallModel).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING_ALT));
		gen.registerSimpleFlatItemModel(sign.asItem());
	}

	/**
	 * Same as vanilla's {@code BlockFamilyProvider.hangingSign}.
	 * Texture {@code textures/block/<wood>_hanging_sign.png}, particles from the stripped log.
	 */
	private static void generateHangingSign(BlockModelGenerators gen, Block hangingSign, Block wallHangingSign, Block strippedLog, Material texture) {
		TextureMapping mapping = new TextureMapping()
				.put(TextureSlot.ALL, texture)
				.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(strippedLog));
		gen.blockStateOutput.accept(BlockModelGenerators.createHangingSign(hangingSign,
				variant(gen, ModelTemplates.HANGING_SIGN_ROT_0, hangingSign, "_rot_0", mapping),
				variant(gen, ModelTemplates.HANGING_SIGN_ROT_1, hangingSign, "_rot_1", mapping),
				variant(gen, ModelTemplates.HANGING_SIGN_ROT_2, hangingSign, "_rot_2", mapping),
				variant(gen, ModelTemplates.HANGING_SIGN_ROT_3, hangingSign, "_rot_3", mapping),
				variant(gen, ModelTemplates.ATTACHED_HANGING_SIGN_ROT_0, hangingSign, "_attached_rot_0", mapping),
				variant(gen, ModelTemplates.ATTACHED_HANGING_SIGN_ROT_1, hangingSign, "_attached_rot_1", mapping),
				variant(gen, ModelTemplates.ATTACHED_HANGING_SIGN_ROT_2, hangingSign, "_attached_rot_2", mapping),
				variant(gen, ModelTemplates.ATTACHED_HANGING_SIGN_ROT_3, hangingSign, "_attached_rot_3", mapping)));
		MultiVariant wallModel = BlockModelGenerators.plainVariant(ModelTemplates.WALL_HANGING_SIGN.create(wallHangingSign, mapping, gen.modelOutput));
		gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(wallHangingSign, wallModel).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING_ALT));
		gen.registerSimpleFlatItemModel(hangingSign.asItem());
	}

	/** {@code <namespace>:block/<wood><suffix>}: named after the wood, also when the sign blocks have custom ids. */
	private static Material blockTexture(WoodSet set, String suffix) {
		return new Material(set.getId().withPath(path -> "block/" + path + suffix));
	}

	private static MultiVariant variant(BlockModelGenerators gen, ModelTemplate template, Block block, String suffix, TextureMapping mapping) {
		return BlockModelGenerators.plainVariant(template.create(ModelLocationUtils.getModelLocation(block, suffix), mapping, gen.modelOutput));
	}

	/** Item models for boats and chest boats (flat item texture {@code textures/item/<id>.png}). */
	public static void generateItemModels(ItemModelGenerators gen, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasBoats()) {
				gen.generateFlatItem(set.getBoatItem(), ModelTemplates.FLAT_ITEM);
				gen.generateFlatItem(set.getChestBoatItem(), ModelTemplates.FLAT_ITEM);
			}
		}
	}

	/** Vanilla-style crafting recipes for everything in the sets. */
	public static void generateRecipes(RecipeProvider gen, RecipeOutput exporter, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasSigns()) {
				Block planks = planks(set);
				gen.signBuilder(set.getSignItem(), Ingredient.of(planks))
						.group("wooden_sign")
						.unlockedBy(RecipeProvider.getHasName(planks), gen.has(planks))
						.save(exporter);
			}
			if (set.hasHangingSigns()) {
				Block strippedLog = strippedLog(set);
				gen.hangingSignBuilder(set.getHangingSignItem(), Ingredient.of(strippedLog))
						.group("wooden_hanging_sign")
						.unlockedBy(RecipeProvider.getHasName(strippedLog), gen.has(strippedLog))
						.save(exporter);
			}
			if (set.hasBoats()) {
				gen.woodenBoat(set.getBoatItem(), planks(set));
				gen.chestBoat(set.getChestBoatItem(), set.getBoatItem());
			}
			if (set.hasShelf()) {
				gen.shelf(set.getShelfItem(), strippedLog(set));
			}
		}
	}

	/** Block loot tables (wall signs reuse the standing sign's table, like vanilla). */
	public static void generateLootTables(BlockLootSubProvider gen, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasSigns()) {
				gen.dropSelf(set.getSign());
			}
			if (set.hasHangingSigns()) {
				gen.dropSelf(set.getHangingSign());
			}
			if (set.hasShelf()) {
				gen.dropSelf(set.getShelf());
			}
		}
	}

	/** Adds blocks to the vanilla sign/shelf block tags (these also make them mineable with an axe). */
	public static void generateBlockTags(BiConsumer<TagKey<Block>, Block> tags, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasSigns()) {
				tags.accept(BlockTags.STANDING_SIGNS, set.getSign());
				tags.accept(BlockTags.WALL_SIGNS, set.getWallSign());
			}
			if (set.hasHangingSigns()) {
				tags.accept(BlockTags.CEILING_HANGING_SIGNS, set.getHangingSign());
				tags.accept(BlockTags.WALL_HANGING_SIGNS, set.getWallHangingSign());
			}
			if (set.hasShelf()) {
				tags.accept(BlockTags.WOODEN_SHELVES, set.getShelf());
			}
		}
	}

	/** Adds items to the vanilla sign/boat/shelf item tags (also used for fuel). */
	public static void generateItemTags(BiConsumer<TagKey<Item>, Item> tags, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasSigns()) {
				tags.accept(ItemTags.SIGNS, set.getSignItem());
			}
			if (set.hasHangingSigns()) {
				tags.accept(ItemTags.HANGING_SIGNS, set.getHangingSignItem());
			}
			if (set.hasBoats()) {
				tags.accept(ItemTags.BOATS, set.getBoatItem());
				tags.accept(ItemTags.CHEST_BOATS, set.getChestBoatItem());
			}
			if (set.hasShelf()) {
				tags.accept(ItemTags.WOODEN_SHELVES, set.getShelfItem());
			}
			if (!set.isFlammable()) {
				if (set.hasSigns()) tags.accept(ItemTags.NON_FLAMMABLE_WOOD, set.getSignItem());
				if (set.hasHangingSigns()) tags.accept(ItemTags.NON_FLAMMABLE_WOOD, set.getHangingSignItem());
				if (set.hasShelf()) tags.accept(ItemTags.NON_FLAMMABLE_WOOD, set.getShelfItem());
			}
		}
	}

	/** Adds the (non-chest) boat to the vanilla {@code #minecraft:boat} entity type tag. */
	public static void generateEntityTypeTags(BiConsumer<TagKey<EntityType<?>>, EntityType<?>> tags, WoodSet... sets) {
		for (WoodSet set : sets) {
			if (set.hasBoats()) {
				tags.accept(EntityTypeTags.BOAT, set.getBoatEntityType());
			}
		}
	}

	/** English names, with the wood name made from the id ({@code dark_maple} -> {@code Dark Maple}). */
	public static void generateTranslations(FabricLanguageProvider.TranslationBuilder builder, WoodSet set) {
		generateTranslations(builder, set, toTitleCase(set.getId().getPath()));
	}

	/** Names using the given wood name, e.g. {@code "Maple"} -> "Maple Sign", "Maple Boat with Chest". */
	public static void generateTranslations(FabricLanguageProvider.TranslationBuilder builder, WoodSet set, String woodName) {
		if (set.hasSigns()) {
			builder.add(set.getSign(), woodName + " Sign");
		}
		if (set.hasHangingSigns()) {
			builder.add(set.getHangingSign(), woodName + " Hanging Sign");
		}
		if (set.hasBoats()) {
			String boat = woodName + (set.isRaft() ? " Raft" : " Boat");
			String chestBoat = boat + " with Chest";
			builder.add(set.getBoatItem(), boat);
			builder.add(set.getChestBoatItem(), chestBoat);
			builder.add(set.getBoatEntityType(), boat);
			builder.add(set.getChestBoatEntityType(), chestBoat);
		}
		if (set.hasShelf()) {
			builder.add(set.getShelf(), woodName + " Shelf");
		}
	}





	private static Block planks(WoodSet set) {
		Block planks = set.getPlanks();
		if (planks == null) {
			throw new IllegalStateException("Wood set " + set.getId() + " needs .planks(...) for datagen");
		}
		return planks;
	}

	private static Block strippedLog(WoodSet set) {
		Block log = set.getStrippedLog();
		if (log == null) {
			throw new IllegalStateException("Wood set " + set.getId() + " needs .strippedLog(...) for datagen");
		}
		return log;
	}

	private static String toTitleCase(String path) {
		StringBuilder sb = new StringBuilder();
		for (String word : path.split("_")) {
			if (word.isEmpty()) continue;
			if (!sb.isEmpty()) sb.append(' ');
			sb.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return sb.toString();
	}
}
