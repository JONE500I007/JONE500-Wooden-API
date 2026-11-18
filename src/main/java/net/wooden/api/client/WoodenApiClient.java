package net.wooden.api.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.RaftRenderer;
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
	public static ModelLayerLocation boatLayer(WoodSet set) {
		return new ModelLayerLocation(set.getId().withPrefix("boat/"), "main");
	}

	/** Model layer {@code <namespace>:chest_boat/<wood>} -> texture {@code textures/entity/chest_boat/<wood>.png}. */
	public static ModelLayerLocation chestBoatLayer(WoodSet set) {
		return new ModelLayerLocation(set.getId().withPrefix("chest_boat/"), "main");
	}

	private static void registerBoat(WoodSet set) {
		ModelLayerLocation boat = boatLayer(set);
		ModelLayerLocation chestBoat = chestBoatLayer(set);

		if (set.isRaft()) {
			ModelLayerRegistry.registerModelLayer(boat, RaftModel::createRaftModel);
			ModelLayerRegistry.registerModelLayer(chestBoat, RaftModel::createChestRaftModel);
			EntityRenderers.register(set.getBoatEntityType(), ctx -> new RaftRenderer(ctx, boat));
			EntityRenderers.register(set.getChestBoatEntityType(), ctx -> new RaftRenderer(ctx, chestBoat));
		} else {
			ModelLayerRegistry.registerModelLayer(boat, BoatModel::createBoatModel);
			ModelLayerRegistry.registerModelLayer(chestBoat, BoatModel::createChestBoatModel);
			EntityRenderers.register(set.getBoatEntityType(), ctx -> new BoatRenderer(ctx, boat));
			EntityRenderers.register(set.getChestBoatEntityType(), ctx -> new BoatRenderer(ctx, chestBoat));
		}
	}
}
