package net.wooden.api;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of Wooden API.
 *
 * <p>Modders do not need to call anything here. Use {@link net.wooden.api.wood.WoodSet} to
 * register your wood content, and {@link net.wooden.api.datagen.WoodenDatagen} for data generation.
 */
public class WoodenApi implements ModInitializer {
	public static final String MOD_ID = "jone500_wooden_api";
	public static final Logger LOGGER = LoggerFactory.getLogger("Wooden API");

	@Override
	public void onInitialize() {
		LOGGER.info("Wooden API loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
