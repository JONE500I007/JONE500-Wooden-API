package net.wooden.testmod;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.ShelfBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.wooden.testmod.WoodenTestMod.MAPLE;

/**
 * Runs on a real dedicated server (no client code loaded).
 * Run with: gradlew runGameTest
 */
public class WoodenServerGameTest {
	@GameTest(maxTicks = 100)
	public void signsHaveBlockEntities(TestContext ctx) {
		BlockPos sign = new BlockPos(1, 1, 1);
		ctx.setBlockState(sign, MAPLE.getSign());
		ctx.assertTrue(ctx.getBlockEntity(sign, SignBlockEntity.class) != null, Text.literal("sign has no block entity"));

		BlockPos wallSupport = new BlockPos(3, 1, 2);
		BlockPos wallSign = new BlockPos(3, 1, 1);
		ctx.setBlockState(wallSupport, Blocks.STONE);
		ctx.setBlockState(wallSign, MAPLE.getWallSign().getDefaultState().with(WallSignBlock.FACING, Direction.NORTH));
		ctx.assertTrue(ctx.getBlockEntity(wallSign, SignBlockEntity.class) != null, Text.literal("wall sign has no block entity"));

		BlockPos hangingSupport = new BlockPos(5, 3, 1);
		BlockPos hangingSign = new BlockPos(5, 2, 1);
		ctx.setBlockState(hangingSupport, Blocks.STONE);
		ctx.setBlockState(hangingSign, MAPLE.getHangingSign());
		ctx.assertTrue(ctx.getBlockEntity(hangingSign, HangingSignBlockEntity.class) != null, Text.literal("hanging sign has no block entity"));

		BlockPos wallHangingSign = new BlockPos(1, 3, 4);
		ctx.setBlockState(new BlockPos(0, 3, 4), Blocks.STONE);
		ctx.setBlockState(wallHangingSign, MAPLE.getWallHangingSign().getDefaultState().with(WallHangingSignBlock.FACING, Direction.NORTH));
		ctx.assertTrue(ctx.getBlockEntity(wallHangingSign, HangingSignBlockEntity.class) != null, Text.literal("wall hanging sign has no block entity"));

		// Let a few ticks pass so block updates run, then make sure nothing broke off.
		ctx.waitAndRun(10, () -> {
			ctx.expectBlock(MAPLE.getSign(), sign);
			ctx.expectBlock(MAPLE.getWallSign(), wallSign);
			ctx.expectBlock(MAPLE.getHangingSign(), hangingSign);
			ctx.expectBlock(MAPLE.getWallHangingSign(), wallHangingSign);
			ctx.complete();
		});
	}

	@GameTest(maxTicks = 100)
	public void shelfStoresItems(TestContext ctx) {
		BlockPos pos = new BlockPos(2, 1, 2);
		ctx.setBlockState(pos, MAPLE.getShelf());
		ShelfBlockEntity shelf = ctx.getBlockEntity(pos, ShelfBlockEntity.class);
		ctx.assertTrue(shelf != null, Text.literal("shelf has no block entity"));
		shelf.setStack(0, new ItemStack(Items.DIAMOND));

		ctx.waitAndRun(5, () -> {
			ShelfBlockEntity again = ctx.getBlockEntity(pos, ShelfBlockEntity.class);
			ctx.assertTrue(again.getStack(0).isOf(Items.DIAMOND), Text.literal("shelf lost its item"));
			ctx.complete();
		});
	}

	@GameTest(maxTicks = 100)
	public void boatsSpawnAndTick(TestContext ctx) {
		Entity boat = ctx.spawnEntity(MAPLE.getBoatEntityType(), new BlockPos(2, 1, 2));
		Entity chestBoat = ctx.spawnEntity(MAPLE.getChestBoatEntityType(), new BlockPos(5, 1, 5));
		ctx.assertTrue(boat.getType() == MAPLE.getBoatEntityType(), Text.literal("wrong boat type"));
		ctx.assertTrue(chestBoat.getType() == MAPLE.getChestBoatEntityType(), Text.literal("wrong chest boat type"));
		ctx.assertTrue(boat.getPickBlockStack().isOf(MAPLE.getBoatItem()), Text.literal("boat does not give the maple boat item"));
		ctx.assertTrue(chestBoat.getPickBlockStack().isOf(MAPLE.getChestBoatItem()), Text.literal("chest boat does not give the maple chest boat item"));

		// Tick for a while to make sure nothing crashes on the server.
		ctx.waitAndRun(40, () -> {
			ctx.expectEntity(MAPLE.getBoatEntityType());
			ctx.expectEntity(MAPLE.getChestBoatEntityType());
			ctx.complete();
		});
	}
}
