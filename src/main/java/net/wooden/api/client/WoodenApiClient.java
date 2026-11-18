package net.wooden.api.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.RaftEntityRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.RaftEntityModel;
import net.wooden.api.wood.WoodSet;

/**
 * Client setup done automatically for every {@link WoodSet}.
 *
 * <p>Signs and hanging signs need nothing here: Fabric API already creates their model layers and
 * texture lookups for any registered {@code WoodType}. Boats need a model layer and a renderer.
 *
 * <p>Fabric runs every mod's {@code main} entrypoint before any {@code client} entrypoint, so all
 * wood sets registered in {@code onInitialize()} are known here.
 */
public class WoodenApiClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		for (WoodSet set : WoodSet.getAll()) {
			if (set.hasBoats()) {
				registerBoat(set);
			}
		}
	}

	/** Model layer {@code <namespace>:boat/<wood>} -> texture {@code textures/entity/boat/<wood>.png}. */
	public static EntityModelLayer boatLayer(WoodSet set) {
		return new EntityModelLayer(set.getId().withPrefixedPath("boat/"), "main");
	}

	/** Model layer {@code <namespace>:chest_boat/<wood>} -> texture {@code textures/entity/chest_boat/<wood>.png}. */
	public static EntityModelLayer chestBoatLayer(WoodSet set) {
		return new EntityModelLayer(set.getId().withPrefixedPath("chest_boat/"), "main");
	}

	private static void registerBoat(WoodSet set) {
		EntityModelLayer boat = boatLayer(set);
		EntityModelLayer chestBoat = chestBoatLayer(set);

		if (set.isRaft()) {
			EntityModelLayerRegistry.registerModelLayer(boat, RaftEntityModel::getTexturedModelData);
			EntityModelLayerRegistry.registerModelLayer(chestBoat, RaftEntityModel::getChestTexturedModelData);
			EntityRendererFactories.register(set.getBoatEntityType(), ctx -> new RaftEntityRenderer(ctx, boat));
			EntityRendererFactories.register(set.getChestBoatEntityType(), ctx -> new RaftEntityRenderer(ctx, chestBoat));
		} else {
			EntityModelLayerRegistry.registerModelLayer(boat, BoatEntityModel::getTexturedModelData);
			EntityModelLayerRegistry.registerModelLayer(chestBoat, BoatEntityModel::getChestTexturedModelData);
			EntityRendererFactories.register(set.getBoatEntityType(), ctx -> new BoatEntityRenderer(ctx, boat));
			EntityRendererFactories.register(set.getChestBoatEntityType(), ctx -> new BoatEntityRenderer(ctx, chestBoat));
		}
	}
}
