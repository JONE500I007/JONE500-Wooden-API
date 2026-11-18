package net.wooden.testmod;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Automated in-game check. Places every Wooden API block of the maple set, spawns both boats and
 * takes screenshots (saved in build/run/clientGameTest/screenshots):
 * <ol>
 *   <li>in a singleplayer world</li>
 *   <li>on a dedicated server that the client joins (tests that everything syncs over the network)</li>
 * </ol>
 * Run with: gradlew runClientGameTest
 */
public class WoodenClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksRender();
			buildScene(world.getServer());
			world.getClientLevel().waitForChunksRender();
			context.waitTicks(40);
			context.takeScreenshot("wooden_api_world");
		}

		try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
			// Spawn is far from 0,0 on a fresh dedicated server; place things around the player instead.
			try (TestServerConnection connection = server.connect()) {
				connection.getClientLevel().waitForChunksRender();
				server.runCommand("op @a");
				buildScene(server);
				connection.getClientLevel().waitForChunksRender();
				context.waitTicks(40);
				context.takeScreenshot("wooden_api_dedicated_server");
			}
		}
	}

	private static void buildScene(TestServerContext server) {
		server.runCommand("gamerule doDaylightCycle false");
		server.runCommand("time set noon");
		server.runCommand("tp @a 0 -60 0 0 20");
		server.runCommand("fill -8 -61 -2 8 -61 10 minecraft:grass_block");
		server.runCommand("fill -8 -60 -2 8 -55 10 minecraft:air");

		// Standing sign with text
		server.runCommand("setblock 2 -60 4 wooden_test:maple_sign[rotation=8]");
		server.runCommand("data merge block 2 -60 4 {front_text:{messages:[\"Wooden\",\"API\",\"\",\"\"]}}");
		// Wall sign on planks
		server.runCommand("setblock 0 -60 5 wooden_test:maple_planks");
		server.runCommand("setblock 0 -60 4 wooden_test:maple_wall_sign[facing=north]");
		server.runCommand("data merge block 0 -60 4 {front_text:{messages:[\"\",\"Wall\",\"\",\"\"]}}");
		// Ceiling hanging sign under planks
		server.runCommand("setblock -2 -58 4 wooden_test:maple_planks");
		server.runCommand("setblock -2 -59 4 wooden_test:maple_hanging_sign[rotation=8]");
		server.runCommand("data merge block -2 -59 4 {front_text:{messages:[\"\",\"Hang\",\"\",\"\"]}}");
		// Wall hanging sign next to a log
		server.runCommand("setblock 4 -59 5 wooden_test:stripped_maple_log");
		server.runCommand("setblock 4 -59 4 wooden_test:maple_wall_hanging_sign[facing=north]");
		// Two shelves next to each other, with items inside
		server.runCommand("setblock -4 -60 4 wooden_test:maple_shelf[facing=north]");
		server.runCommand("setblock -5 -60 4 wooden_test:maple_shelf[facing=north]");
		server.runCommand("item replace block -4 -60 4 container.0 with minecraft:diamond");
		server.runCommand("item replace block -4 -60 4 container.1 with minecraft:apple");
		server.runCommand("item replace block -5 -60 4 container.2 with wooden_test:maple_sign");
		// Boats
		server.runCommand("kill @e[type=!player]");
		server.runCommand("summon wooden_test:maple_boat 1.5 -60 2.5");
		server.runCommand("summon wooden_test:maple_chest_boat -1.5 -60 2.5");

		// Items in the hotbar so their icons are visible
		server.runCommand("clear @a");
		for (String item : new String[]{"maple_sign", "maple_hanging_sign", "maple_boat", "maple_chest_boat", "maple_shelf", "maple_planks", "stripped_maple_log"}) {
			server.runCommand("give @a wooden_test:" + item);
		}
	}
}
