package net.wooden.tutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registries;
import net.minecraft.block.Block;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 6: using a WoodSet in your own code</h2>
 *
 * After {@code register()} a {@code WoodSet} gives you everything it made, for example to use the blocks in your
 * own recipes, loot tables, advancements or block entities:
 * <pre>
 * getSign(), getWallSign(), getSignItem()
 * getHangingSign(), getWallHangingSign(), getHangingSignItem()
 * getBoatItem(), getChestBoatItem(), getBoatEntityType(), getChestBoatEntityType()
 * getShelf(), getShelfItem()
 * getWoodType()                 the wood type (for your doors, fence gates, ...)
 * getId(), isFlammable(), isRaft(), hasSigns(), hasHangingSigns(), hasBoats(), hasShelf()
 * </pre>
 *
 * {@code WoodSet.getAll()} lists every wood set registered so far, from <b>every</b> mod that uses Wooden API, for
 * example to add compatibility with other mods' woods.
 */
public final class Lesson6UsingWoodSets {
	private static final Logger LOGGER = LoggerFactory.getLogger("Wooden API Tutorial");

	private Lesson6UsingWoodSets() {
	}

	static void init() {
		for (WoodSet wood : WoodSet.getAll()) {
			StringBuilder parts = new StringBuilder();
			if (wood.hasSigns()) parts.append(" sign=").append(id(wood.getSign()));
			if (wood.hasHangingSigns()) parts.append(" hanging_sign=").append(id(wood.getHangingSign()));
			if (wood.hasBoats()) parts.append(wood.isRaft() ? " raft=" : " boat=").append(Registries.ITEM.getId(wood.getBoatItem()));
			if (wood.hasShelf()) parts.append(" shelf=").append(id(wood.getShelf()));
			LOGGER.info("Wood {} (flammable: {}):{}", wood.getId(), wood.isFlammable(), parts);
		}
	}

	private static String id(Block block) {
		return Registries.BLOCK.getId(block).toString();
	}
}
