package net.wooden.api.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShelfBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestRaftEntity;
import net.minecraft.entity.vehicle.RaftEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.SignItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * A set of wood-based content (signs, hanging signs, boats, chest boats, shelf) for one wood type.
 *
 * <p>Create it once, in your mod's {@code onInitialize()}, usually as a {@code static final} field:
 *
 * <pre>{@code
 * public static final WoodSet MAPLE = WoodSet.builder(Identifier.of("mymod", "maple"))
 *         .planks(() -> ModBlocks.MAPLE_PLANKS)
 *         .strippedLog(() -> ModBlocks.STRIPPED_MAPLE_LOG)
 *         .mapColor(MapColor.ORANGE)
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
 *
 * <p>Textures your mod has to provide (namespace {@code mymod}):
 * <ul>
 *   <li>{@code textures/entity/signs/maple.png}</li>
 *   <li>{@code textures/entity/signs/hanging/maple.png}</li>
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

	@Nullable private final EntityType<? extends AbstractBoatEntity> boatEntityType;
	@Nullable private final EntityType<? extends AbstractBoatEntity> chestBoatEntityType;
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
			Block standing = Blocks.register(blockKey("_sign"), s -> new SignBlock(this.woodType, s), signSettings(b.mapColor));
			Block wall = Blocks.register(blockKey("_wall_sign"), s -> new WallSignBlock(this.woodType, s),
					signSettings(copyLootTable(standing), b.mapColor));
			this.sign = standing;
			this.wallSign = wall;
			this.signItem = Items.register(standing, (block, s) -> new SignItem(block, wall, s), new Item.Settings().maxCount(16));
			BlockEntityType.SIGN.addSupportedBlock(standing);
			BlockEntityType.SIGN.addSupportedBlock(wall);
		} else {
			this.sign = null;
			this.wallSign = null;
			this.signItem = null;
		}

		// ---- Hanging signs ----
		if (b.hangingSigns) {
			Block ceiling = Blocks.register(blockKey("_hanging_sign"), s -> new HangingSignBlock(this.woodType, s), signSettings(hangingColor));
			Block wall = Blocks.register(blockKey("_wall_hanging_sign"), s -> new WallHangingSignBlock(this.woodType, s),
					signSettings(copyLootTable(ceiling), hangingColor));
			this.hangingSign = ceiling;
			this.wallHangingSign = wall;
			this.hangingSignItem = Items.register(ceiling, (block, s) -> new HangingSignItem(block, wall, s), new Item.Settings().maxCount(16));
			BlockEntityType.HANGING_SIGN.addSupportedBlock(ceiling);
			BlockEntityType.HANGING_SIGN.addSupportedBlock(wall);
		} else {
			this.hangingSign = null;
			this.wallHangingSign = null;
			this.hangingSignItem = null;
		}

		// ---- Boats / rafts ----
		if (b.boats) {
			String boatSuffix = raft ? "_raft" : "_boat";
			String chestSuffix = raft ? "_chest_raft" : "_chest_boat";
			RegistryKey<EntityType<?>> boatKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id.withSuffixedPath(boatSuffix));
			RegistryKey<EntityType<?>> chestKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id.withSuffixedPath(chestSuffix));

			Supplier<Item> boatItemSupplier = () -> this.boatItem;
			Supplier<Item> chestItemSupplier = () -> this.chestBoatItem;

			EntityType<? extends AbstractBoatEntity> boatType;
			EntityType<? extends AbstractBoatEntity> chestType;
			if (raft) {
				boatType = Registry.register(Registries.ENTITY_TYPE, boatKey, boatBuilder(
						EntityType.Builder.<RaftEntity>create((type, world) -> new RaftEntity(type, world, boatItemSupplier), SpawnGroup.MISC)).build(boatKey));
				chestType = Registry.register(Registries.ENTITY_TYPE, chestKey, boatBuilder(
						EntityType.Builder.<ChestRaftEntity>create((type, world) -> new ChestRaftEntity(type, world, chestItemSupplier), SpawnGroup.MISC)).build(chestKey));
			} else {
				boatType = Registry.register(Registries.ENTITY_TYPE, boatKey, boatBuilder(
						EntityType.Builder.<BoatEntity>create((type, world) -> new BoatEntity(type, world, boatItemSupplier), SpawnGroup.MISC)).build(boatKey));
				chestType = Registry.register(Registries.ENTITY_TYPE, chestKey, boatBuilder(
						EntityType.Builder.<ChestBoatEntity>create((type, world) -> new ChestBoatEntity(type, world, chestItemSupplier), SpawnGroup.MISC)).build(chestKey));
			}
			this.boatEntityType = boatType;
			this.chestBoatEntityType = chestType;

			this.boatItem = Items.register(itemKey(boatSuffix), s -> new BoatItem(boatType, s), new Item.Settings().maxCount(1));
			this.chestBoatItem = Items.register(itemKey(chestSuffix), s -> new BoatItem(chestType, s), new Item.Settings().maxCount(1));

			DispenserBlock.registerBehavior(this.boatItem, new BoatDispenserBehavior(boatType));
			DispenserBlock.registerBehavior(this.chestBoatItem, new BoatDispenserBehavior(chestType));
		} else {
			this.boatEntityType = null;
			this.chestBoatEntityType = null;
		}

		// ---- Shelf ----
		if (b.shelf) {
			AbstractBlock.Settings settings = AbstractBlock.Settings.create()
					.mapColor(b.mapColor)
					.instrument(NoteBlockInstrument.BASS)
					.sounds(BlockSoundGroup.SHELF)
					.strength(2.0F, 3.0F);
			if (flammable) {
				settings.burnable();
			}
			Block shelfBlock = Blocks.register(blockKey("_shelf"), ShelfBlock::new, settings);
			this.shelf = shelfBlock;
			this.shelfItem = Items.register(shelfBlock, s -> s.component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT));
			BlockEntityType.SHELF.addSupportedBlock(shelfBlock);
			if (flammable) {
				FlammableBlockRegistry.getDefaultInstance().add(shelfBlock, 30, 20);
			}
		} else {
			this.shelf = null;
			this.shelfItem = null;
		}

		// ---- Creative tabs ----
		if (b.addToCreativeTabs) {
			ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
				if (signItem != null) entries.add(signItem);
				if (hangingSignItem != null) entries.add(hangingSignItem);
				if (shelfItem != null) entries.add(shelfItem);
			});
			if (boatItem != null) {
				ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
					entries.add(boatItem);
					entries.add(chestBoatItem);
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

	public EntityType<? extends AbstractBoatEntity> getBoatEntityType() {
		return require(boatEntityType, "boats");
	}

	public EntityType<? extends AbstractBoatEntity> getChestBoatEntityType() {
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

	private RegistryKey<Block> blockKey(String suffix) {
		return RegistryKey.of(RegistryKeys.BLOCK, id.withSuffixedPath(suffix));
	}

	private RegistryKey<Item> itemKey(String suffix) {
		return RegistryKey.of(RegistryKeys.ITEM, id.withSuffixedPath(suffix));
	}

	private AbstractBlock.Settings signSettings(MapColor color) {
		return signSettings(AbstractBlock.Settings.create(), color);
	}

	private AbstractBlock.Settings signSettings(AbstractBlock.Settings settings, MapColor color) {
		settings.mapColor(color).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F);
		if (flammable) {
			settings.burnable();
		}
		return settings;
	}

	/** Same as vanilla: wall variants drop the standing item and share its name. */
	private static AbstractBlock.Settings copyLootTable(Block block) {
		return AbstractBlock.Settings.create()
				.lootTable(block.getLootTableKey())
				.overrideTranslationKey(block.getTranslationKey());
	}

	private static <T extends AbstractBoatEntity> EntityType.Builder<T> boatBuilder(EntityType.Builder<T> builder) {
		return builder.dropsNothing().dimensions(1.375F, 0.5625F).eyeHeight(0.5625F).maxTrackingRange(10);
	}

	// ------------------------------------------------------------------------------------------
	// Builder
	// ------------------------------------------------------------------------------------------

	public static final class Builder {
		private final Identifier id;
		private MapColor mapColor = MapColor.OAK_TAN;
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
