package net.wooden.testmod;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import static net.wooden.testmod.WoodenTestMod.MAPLE;

/**
 * Runs on a real dedicated server (no client code loaded).
 * Run with: gradlew runGameTest
 */
public class WoodenServerGameTest {
	@GameTest(maxTicks = 100)
	public void signsHaveBlockEntities(GameTestHelper ctx) {
		BlockPos sign = new BlockPos(1, 1, 1);
		ctx.setBlock(sign, MAPLE.getSign());
		ctx.assertTrue(ctx.getBlockEntity(sign, SignBlockEntity.class) != null, Component.literal("sign has no block entity"));

		BlockPos wallSupport = new BlockPos(3, 1, 2);
		BlockPos wallSign = new BlockPos(3, 1, 1);
		ctx.setBlock(wallSupport, Blocks.STONE);
		ctx.setBlock(wallSign, MAPLE.getWallSign().defaultBlockState().setValue(WallSignBlock.FACING, Direction.NORTH));
		ctx.assertTrue(ctx.getBlockEntity(wallSign, SignBlockEntity.class) != null, Component.literal("wall sign has no block entity"));

		BlockPos hangingSupport = new BlockPos(5, 3, 1);
		BlockPos hangingSign = new BlockPos(5, 2, 1);
		ctx.setBlock(hangingSupport, Blocks.STONE);
		ctx.setBlock(hangingSign, MAPLE.getHangingSign());
		ctx.assertTrue(ctx.getBlockEntity(hangingSign, HangingSignBlockEntity.class) != null, Component.literal("hanging sign has no block entity"));

		BlockPos wallHangingSign = new BlockPos(1, 3, 4);
		ctx.setBlock(new BlockPos(0, 3, 4), Blocks.STONE);
		ctx.setBlock(wallHangingSign, MAPLE.getWallHangingSign().defaultBlockState().setValue(WallHangingSignBlock.FACING, Direction.NORTH));
		ctx.assertTrue(ctx.getBlockEntity(wallHangingSign, HangingSignBlockEntity.class) != null, Component.literal("wall hanging sign has no block entity"));

		// Let a few ticks pass so block updates run, then make sure nothing broke off.
		ctx.runAfterDelay(10, () -> {
			ctx.assertBlockPresent(MAPLE.getSign(), sign);
			ctx.assertBlockPresent(MAPLE.getWallSign(), wallSign);
			ctx.assertBlockPresent(MAPLE.getHangingSign(), hangingSign);
			ctx.assertBlockPresent(MAPLE.getWallHangingSign(), wallHangingSign);
			ctx.succeed();
		});
	}

	@GameTest(maxTicks = 100)
	public void shelfStoresItems(GameTestHelper ctx) {
		BlockPos pos = new BlockPos(2, 1, 2);
		ctx.setBlock(pos, MAPLE.getShelf());
		ShelfBlockEntity shelf = ctx.getBlockEntity(pos, ShelfBlockEntity.class);
		ctx.assertTrue(shelf != null, Component.literal("shelf has no block entity"));
		shelf.setItem(0, new ItemStack(Items.DIAMOND));

		ctx.runAfterDelay(5, () -> {
			ShelfBlockEntity again = ctx.getBlockEntity(pos, ShelfBlockEntity.class);
			ctx.assertTrue(again.getItem(0).is(Items.DIAMOND), Component.literal("shelf lost its item"));
			ctx.succeed();
		});
	}

	@GameTest(maxTicks = 100)
	public void boatsSpawnAndTick(GameTestHelper ctx) {
		Entity boat = ctx.spawn(MAPLE.getBoatEntityType(), new BlockPos(2, 1, 2));
		Entity chestBoat = ctx.spawn(MAPLE.getChestBoatEntityType(), new BlockPos(5, 1, 5));
		ctx.assertTrue(boat.getType() == MAPLE.getBoatEntityType(), Component.literal("wrong boat type"));
		ctx.assertTrue(chestBoat.getType() == MAPLE.getChestBoatEntityType(), Component.literal("wrong chest boat type"));
		ctx.assertTrue(boat.getPickResult().is(MAPLE.getBoatItem()), Component.literal("boat does not give the maple boat item"));
		ctx.assertTrue(chestBoat.getPickResult().is(MAPLE.getChestBoatItem()), Component.literal("chest boat does not give the maple chest boat item"));

		// Tick for a while to make sure nothing crashes on the server.
		ctx.runAfterDelay(40, () -> {
			ctx.assertEntityPresent(MAPLE.getBoatEntityType());
			ctx.assertEntityPresent(MAPLE.getChestBoatEntityType());
			ctx.succeed();
		});
	}
}
