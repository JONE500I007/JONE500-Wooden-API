package net.wooden.api.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/**
 * A set of wood-based content (signs, hanging signs, boats, chest boats, shelf) for one wood type.
 *
 * <p>Create it once, in your mod's {@code onInitialize()}, usually as a {@code static final} field:
 *
 * <pre>{@code
 * public static final WoodSet MAPLE = WoodSet.builder(Identifier.fromNamespaceAndPath("mymod", "maple"))
 *         .planks(() -> ModBlocks.MAPLE_PLANKS)
 *         .strippedLog(() -> ModBlocks.STRIPPED_MAPLE_LOG)
 *         .mapColor(MapColor.COLOR_ORANGE)
 *         .all()          // signs + hanging signs + boats + shelf
 *         .register();
 * }</pre>
 *
 * <p>Registered ids (for wood {@code mymod:maple}):
 * <ul>
 *   <li>Blocks: {@code maple_sign}, {@code maple_wall_sign}, {@code maple_hanging_sign},
 *       {@code maple_wall_hanging_sign}, {@code maple_shelf}</li>
 *   <li>Items: {@code maple_sign}, {@code maple_hanging_sign}, {@code maple_boat},
 *       {@code maple_chest_boat}, {@code maple_shelf} (or {@code maple_raft} / {@code maple_chest_raft})</li>
 *   <li>Entities: {@code maple_boat}, {@code maple_chest_boat} (or the raft ids)</li>
 * </ul>
 * Moving signs your mod already had? {@link Builder#signIds} and {@link Builder#hangingSignIds} keep their old
 * block ids, so they don't disappear from existing worlds.
 *
 * <p>Textures your mod has to provide (namespace {@code mymod}):
 * <ul>
 *   <li>{@code textures/block/maple_sign.png}, {@code textures/gui/signs/maple.png}</li>
 *   <li>{@code textures/block/maple_hanging_sign.png}</li>
 *   <li>{@code textures/gui/hanging_signs/maple.png}</li>
 *   <li>{@code textures/entity/boat/maple.png}, {@code textures/entity/chest_boat/maple.png}</li>
 *   <li>{@code textures/item/maple_sign.png}, {@code maple_hanging_sign.png}, {@code maple_boat.png},
 *       {@code maple_chest_boat.png}</li>
 *   <li>{@code textures/block/maple_shelf.png}</li>
 * </ul>
 * Models, blockstates, recipes, loot tables, tags and translations can be generated with
 * {@link net.wooden.api.datagen.WoodenDatagen}.
 */
public final class WoodSet {
	private static final List<WoodSet> ALL = new ArrayList<>();
	private static final Set<Identifier> IDS = new HashSet<>();

	private final Identifier id;
	private final WoodType woodType;
	private final boolean flammable;
	private final boolean raft;
	@Nullable private final Supplier<Block> planks;
	@Nullable private final Supplier<Block> strippedLog;

	@Nullable private final Block sign;
	@Nullable private final Block wallSign;
	@Nullable private final Item signItem;

	@Nullable private final Block hangingSign;
	@Nullable private final Block wallHangingSign;
	@Nullable private final Item hangingSignItem;

	@Nullable private final EntityType<? extends AbstractBoat> boatEntityType;
	@Nullable private final EntityType<? extends AbstractBoat> chestBoatEntityType;
	@Nullable private Item boatItem;
	@Nullable private Item chestBoatItem;

	@Nullable private final Block shelf;
	@Nullable private final Item shelfItem;

	private WoodSet(Builder b) {
		this.id = b.id;
		this.flammable = b.flammable;
		this.raft = b.raft;
		this.planks = b.planks;
		this.strippedLog = b.strippedLog;
		this.woodType = b.woodType != null ? b.woodType : b.woodTypeBuilder.register(b.id, b.blockSetType);

		MapColor hangingColor = b.hangingSignMapColor != null ? b.hangingSignMapColor : b.mapColor;

		// ---- Signs ----
		if (b.signs) {
			Block standing = Blocks.register(blockKey(b.signId, "_sign"), s -> new StandingSignBlock(this.woodType, s), signSettings(b.mapColor));
			Block wall = Blocks.register(blockKey(b.wallSignId, "_wall_sign"), s -> new WallSignBlock(this.woodType, s),
					signSettings(copyLootTable(standing), b.mapColor));
			this.sign = standing;
			this.wallSign = wall;
			this.signItem = registerBlockItem(itemKey("_sign"), standing, (block, s) -> new StandingAndWallBlockItem(block, wall, Direction.DOWN, s),
					fuel(new Item.Properties().stacksTo(16).signText(), ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE));
			BlockEntityTypes.SIGN.addValidBlock(standing);
			BlockEntityTypes.SIGN.addValidBlock(wall);
		} else {
			this.sign = null;
			this.wallSign = null;
			this.signItem = null;
		}

		// ---- Hanging signs ----
		if (b.hangingSigns) {
			Block ceiling = Blocks.register(blockKey(b.hangingSignId, "_hanging_sign"), s -> new CeilingHangingSignBlock(this.woodType, s), signSettings(hangingColor));
			Block wall = Blocks.register(blockKey(b.wallHangingSignId, "_wall_hanging_sign"), s -> new WallHangingSignBlock(this.woodType, s),
					signSettings(copyLootTable(ceiling), hangingColor));
			this.hangingSign = ceiling;
			this.wallHangingSign = wall;
			this.hangingSignItem = registerBlockItem(itemKey("_hanging_sign"), ceiling, (block, s) -> new HangingSignItem(block, wall, s),
					fuel(new Item.Properties().stacksTo(16).signText(), ContextIntProviders.COOKING_TIME_HANGING_SIGNS));
			BlockEntityTypes.HANGING_SIGN.addValidBlock(ceiling);
			BlockEntityTypes.HANGING_SIGN.addValidBlock(wall);
		} else {
			this.hangingSign = null;
			this.wallHangingSign = null;
			this.hangingSignItem = null;
		}

		// ---- Boats / rafts ----
		if (b.boats) {
			String boatSuffix = raft ? "_raft" : "_boat";
			String chestSuffix = raft ? "_chest_raft" : "_chest_boat";
			ResourceKey<EntityType<?>> boatKey = ResourceKey.create(Registries.ENTITY_TYPE, id.withSuffix(boatSuffix));
			ResourceKey<EntityType<?>> chestKey = ResourceKey.create(Registries.ENTITY_TYPE, id.withSuffix(chestSuffix));

			Supplier<Item> boatItemSupplier = () -> this.boatItem;
			Supplier<Item> chestItemSupplier = () -> this.chestBoatItem;

			EntityType<? extends AbstractBoat> boatType;
			EntityType<? extends AbstractBoat> chestType;
			if (raft) {
				boatType = Registry.register(BuiltInRegistries.ENTITY_TYPE, boatKey, boatBuilder(
						EntityType.Builder.<Raft>of((type, world) -> new Raft(type, world, boatItemSupplier), MobCategory.MISC)).build(boatKey));
				chestType = Registry.register(BuiltInRegistries.ENTITY_TYPE, chestKey, boatBuilder(
						EntityType.Builder.<ChestRaft>of((type, world) -> new ChestRaft(type, world, chestItemSupplier), MobCategory.MISC)).build(chestKey));
			} else {
				boatType = Registry.register(BuiltInRegistries.ENTITY_TYPE, boatKey, boatBuilder(
						EntityType.Builder.<Boat>of((type, world) -> new Boat(type, world, boatItemSupplier), MobCategory.MISC)).build(boatKey));
				chestType = Registry.register(BuiltInRegistries.ENTITY_TYPE, chestKey, boatBuilder(
						EntityType.Builder.<ChestBoat>of((type, world) -> new ChestBoat(type, world, chestItemSupplier), MobCategory.MISC)).build(chestKey));
			}
			this.boatEntityType = boatType;
			this.chestBoatEntityType = chestType;

			this.boatItem = registerItem(itemKey(boatSuffix), s -> new BoatItem(boatType, s), fuel(new Item.Properties().stacksTo(1), ContextIntProviders.COOKING_TIME_BOATS));
			this.chestBoatItem = registerItem(itemKey(chestSuffix), s -> new BoatItem(chestType, s), fuel(new Item.Properties().stacksTo(1), ContextIntProviders.COOKING_TIME_BOATS));

			DispenserBlock.registerBehavior(this.boatItem, new BoatDispenseItemBehavior(boatType));
			DispenserBlock.registerBehavior(this.chestBoatItem, new BoatDispenseItemBehavior(chestType));
		} else {
			this.boatEntityType = null;
			this.chestBoatEntityType = null;
		}

		// ---- Shelf ----
		if (b.shelf) {
			BlockBehaviour.Properties settings = BlockBehaviour.Properties.of()
					.mapColor(b.mapColor)
					.instrument(NoteBlockInstrument.BASS)
					.sound(SoundType.SHELF)
					.strength(2.0F, 3.0F);
			if (flammable) {
				settings.ignitedByLava();
			}
			Block shelfBlock = Blocks.register(blockKey("_shelf"), ShelfBlock::new, settings);
			this.shelf = shelfBlock;
			this.shelfItem = registerBlockItem(shelfBlock, BlockItem::new,
					fuel(new Item.Properties().component(DataComponents.CONTAINER, ItemContainerContents.EMPTY), ContextIntProviders.COOKING_TIME_WOOD_BLOCKS));
			BlockEntityTypes.SHELF.addValidBlock(shelfBlock);
			if (flammable) {
				FlammableBlockRegistry.getDefaultInstance().add(shelfBlock, 30, 20);
			}
		} else {
			this.shelf = null;
			this.shelfItem = null;
		}

		// ---- Creative tabs ----
		if (b.addToCreativeTabs) {
			CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
				if (signItem != null) entries.accept(signItem);
				if (hangingSignItem != null) entries.accept(hangingSignItem);
				if (shelfItem != null) entries.accept(shelfItem);
			});
			if (boatItem != null) {
				CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
					entries.accept(boatItem);
					entries.accept(chestBoatItem);
				});
			}
		}
	}

	// ------------------------------------------------------------------------------------------
	// Public API
	// ------------------------------------------------------------------------------------------

	/** Starts building a wood set. The id is the wood name, e.g. {@code mymod:maple}. */
	public static Builder builder(Identifier id) {
		return new Builder(id);
	}

	/** All wood sets registered so far, from every mod. */
	public static List<WoodSet> getAll() {
		return Collections.unmodifiableList(ALL);
	}

	/** The wood id, e.g. {@code mymod:maple}. */
	public Identifier getId() {
		return id;
	}

	public WoodType getWoodType() {
		return woodType;
	}

	public boolean isFlammable() {
		return flammable;
	}

	/** True if the boats of this wood are bamboo-style rafts. */
	public boolean isRaft() {
		return raft;
	}

	public boolean hasSigns() {
		return sign != null;
	}

	public boolean hasHangingSigns() {
		return hangingSign != null;
	}

	public boolean hasBoats() {
		return boatEntityType != null;
	}

	public boolean hasShelf() {
		return shelf != null;
	}

	/** The planks block given in {@link Builder#planks}, or {@code null}. Used by datagen. */
	@Nullable
	public Block getPlanks() {
		return planks != null ? planks.get() : null;
	}

	/** The stripped log block given in {@link Builder#strippedLog}, or {@code null}. Used by datagen. */
	@Nullable
	public Block getStrippedLog() {
		return strippedLog != null ? strippedLog.get() : null;
	}

	public Block getSign() {
		return require(sign, "signs");
	}

	public Block getWallSign() {
		return require(wallSign, "signs");
	}

	public Item getSignItem() {
		return require(signItem, "signs");
	}

	public Block getHangingSign() {
		return require(hangingSign, "hangingSigns");
	}

	public Block getWallHangingSign() {
		return require(wallHangingSign, "hangingSigns");
	}

	public Item getHangingSignItem() {
		return require(hangingSignItem, "hangingSigns");
	}

	public EntityType<? extends AbstractBoat> getBoatEntityType() {
		return require(boatEntityType, "boats");
	}

	public EntityType<? extends AbstractBoat> getChestBoatEntityType() {
		return require(chestBoatEntityType, "boats");
	}

	public Item getBoatItem() {
		return require(boatItem, "boats");
	}

	public Item getChestBoatItem() {
		return require(chestBoatItem, "boats");
	}

	public Block getShelf() {
		return require(shelf, "shelf");
	}

	public Item getShelfItem() {
		return require(shelfItem, "shelf");
	}

	@Override
	public String toString() {
		return "WoodSet[" + id + "]";
	}

	// ------------------------------------------------------------------------------------------
	// Internals
	// ------------------------------------------------------------------------------------------

	private <T> T require(@Nullable T value, String feature) {
		if (value == null) {
			throw new IllegalStateException("Wood set " + id + " was registered without ." + feature + "()");
		}
		return value;
	}

	private ResourceKey<Block> blockKey(String suffix) {
		return ResourceKey.create(Registries.BLOCK, id.withSuffix(suffix));
	}

	/** The block id given with {@code signIds(...)} / {@code hangingSignIds(...)}, or {@code <wood><suffix>}. */
	private ResourceKey<Block> blockKey(@Nullable Identifier custom, String suffix) {
		return custom != null ? ResourceKey.create(Registries.BLOCK, custom) : blockKey(suffix);
	}

	private ResourceKey<Item> itemKey(String suffix) {
		return ResourceKey.create(Registries.ITEM, id.withSuffix(suffix));
	}

	/** Since 26.3 furnace fuel is an item component. Like vanilla, non-flammable wood is not fuel. */
	private Item.Properties fuel(Item.Properties properties, ResourceKey<ContextIntProvider> burnTime) {
		return flammable ? properties.cookingFuel(burnTime) : properties;
	}

	/** Same as vanilla's (private) {@code Items.registerItem}. */
	private static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties properties) {
		Item item = factory.apply(properties.setId(key));
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	/** Same as vanilla's (private) {@code Items.registerBlock}: the item gets the block's id and name. */
	private static Item registerBlockItem(Block block, BiFunction<Block, Item.Properties, Item> factory, Item.Properties properties) {
		return registerBlockItem(ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier()), block, factory, properties);
	}

	/** Like {@link #registerBlockItem(Block, BiFunction, Item.Properties)}, with an item id of its own (for custom sign block ids) and the block's name. */
	private static Item registerBlockItem(ResourceKey<Item> key, Block block, BiFunction<Block, Item.Properties, Item> factory, Item.Properties properties) {
		return registerItem(key, s -> factory.apply(block, s), properties.overrideDescription(block.getDescriptionId()).requiredFeatures(block.requiredFeatures()));
	}

	private BlockBehaviour.Properties signSettings(MapColor color) {
		return signSettings(BlockBehaviour.Properties.of(), color);
	}

	private BlockBehaviour.Properties signSettings(BlockBehaviour.Properties settings, MapColor color) {
		settings.mapColor(color).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F);
		if (flammable) {
			settings.ignitedByLava();
		}
		return settings;
	}

	/** Same as vanilla: wall variants drop the standing item and share its name. */
	private static BlockBehaviour.Properties copyLootTable(Block block) {
		return BlockBehaviour.Properties.of()
				.overrideLootTable(block.getLootTable())
				.overrideDescription(block.getDescriptionId());
	}

	private static <T extends AbstractBoat> EntityType.Builder<T> boatBuilder(EntityType.Builder<T> builder) {
		return builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10);
	}

	// ------------------------------------------------------------------------------------------
	// Builder
	// ------------------------------------------------------------------------------------------

	public static final class Builder {
		private final Identifier id;
		private MapColor mapColor = MapColor.WOOD;
		@Nullable private MapColor hangingSignMapColor;
		@Nullable private WoodType woodType;
		private WoodTypeBuilder woodTypeBuilder = new WoodTypeBuilder();
		private BlockSetType blockSetType = BlockSetType.OAK;
		@Nullable private Supplier<Block> planks;
		@Nullable private Supplier<Block> strippedLog;
		private boolean flammable = true;
		private boolean raft = false;
		private boolean addToCreativeTabs = true;
		private boolean signs;
		private boolean hangingSigns;
		private boolean boats;
		private boolean shelf;
		@Nullable private Identifier signId;
		@Nullable private Identifier wallSignId;
		@Nullable private Identifier hangingSignId;
		@Nullable private Identifier wallHangingSignId;

		private Builder(Identifier id) {
			this.id = Objects.requireNonNull(id, "id");
		}

		/** Adds a sign (standing + wall). */
		public Builder signs() {
			this.signs = true;
			return this;
		}

		/** Adds a hanging sign (ceiling + wall). */
		public Builder hangingSigns() {
			this.hangingSigns = true;
			return this;
		}

		/** Adds a boat and a chest boat. */
		public Builder boats() {
			this.boats = true;
			return this;
		}

		/** Adds a bamboo-style raft and chest raft instead of normal boats. */
		public Builder rafts() {
			this.boats = true;
			this.raft = true;
			return this;
		}

		/** Adds a shelf (the 1.21.9 shelf block). */
		public Builder shelf() {
			this.shelf = true;
			return this;
		}

		/** Shortcut for {@code signs().hangingSigns().boats().shelf()}. */
		public Builder all() {
			return signs().hangingSigns().boats().shelf();
		}

		/** Planks of this wood. Used for the sign/boat/shelf recipes and sign particles in datagen. */
		public Builder planks(Supplier<Block> planks) {
			this.planks = planks;
			return this;
		}

		public Builder planks(Block planks) {
			return planks(() -> planks);
		}

		/** Stripped log of this wood. Used for the hanging sign/shelf recipes and particles in datagen. */
		public Builder strippedLog(Supplier<Block> strippedLog) {
			this.strippedLog = strippedLog;
			return this;
		}

		public Builder strippedLog(Block strippedLog) {
			return strippedLog(() -> strippedLog);
		}

		/** Map color of the signs and shelf (usually the planks color). Default: oak. */
		public Builder mapColor(MapColor color) {
			this.mapColor = color;
			return this;
		}

		/** Map color of the hanging signs (vanilla uses the log color). Default: same as {@link #mapColor}. */
		public Builder hangingSignMapColor(MapColor color) {
			this.hangingSignMapColor = color;
			return this;
		}

		/** Sounds of the wood type (e.g. {@code WoodTypeBuilder.copyOf(WoodType.CHERRY)}). */
		public Builder woodType(WoodTypeBuilder builder) {
			this.woodTypeBuilder = builder;
			return this;
		}

		/** Uses an existing wood type instead of creating a new one (e.g. one you already made for doors). */
		public Builder woodType(WoodType woodType) {
			this.woodType = woodType;
			return this;
		}

		/** Block set type used when creating the wood type. Default: oak. */
		public Builder blockSetType(BlockSetType blockSetType) {
			this.blockSetType = blockSetType;
			return this;
		}

		/** Makes the content not burn (like crimson/warped wood). */
		public Builder nonFlammable() {
			this.flammable = false;
			return this;
		}

		/** Don't add the items to the vanilla creative tabs (if you have your own tab). */
		public Builder noCreativeTabs() {
			this.addToCreativeTabs = false;
			return this;
		}

		/**
		 * Gives the sign blocks your own ids instead of {@code <wood>_sign} and {@code <wood>_wall_sign}.
		 * Also turns on {@link #signs()}.
		 *
		 * <p>Only needed when you move signs that your mod <b>already had</b> to Wooden API: a block whose id
		 * changes disappears from existing worlds, so give the blocks their old ids here. The item keeps its
		 * normal id ({@code <wood>_sign}), and the textures keep their normal names.
		 *
		 * <pre>{@code
		 * .signIds(Identifier.fromNamespaceAndPath("mymod", "apple_standing_sign"),
		 *          Identifier.fromNamespaceAndPath("mymod", "apple_wall_sign"))
		 * }</pre>
		 */
		public Builder signIds(Identifier standing, Identifier wall) {
			this.signId = Objects.requireNonNull(standing, "standing");
			this.wallSignId = Objects.requireNonNull(wall, "wall");
			return signs();
		}

		/**
		 * Like {@link #signIds}, for the hanging sign: your own ids instead of {@code <wood>_hanging_sign} and
		 * {@code <wood>_wall_hanging_sign}. Also turns on {@link #hangingSigns()}. The item keeps its normal id.
		 */
		public Builder hangingSignIds(Identifier ceiling, Identifier wall) {
			this.hangingSignId = Objects.requireNonNull(ceiling, "ceiling");
			this.wallHangingSignId = Objects.requireNonNull(wall, "wall");
			return hangingSigns();
		}

		/** Registers everything. Call this during your mod's {@code onInitialize()}. */
		public WoodSet register() {
			if (!signs && !hangingSigns && !boats && !shelf) {
				throw new IllegalStateException("Wood set " + id + " has nothing to register; call signs(), hangingSigns(), boats(), shelf() or all()");
			}
			if (!IDS.add(id)) {
				throw new IllegalStateException("Wood set " + id + " is already registered");
			}
			WoodSet set = new WoodSet(this);
			ALL.add(set);
			return set;
		}
	}
}
