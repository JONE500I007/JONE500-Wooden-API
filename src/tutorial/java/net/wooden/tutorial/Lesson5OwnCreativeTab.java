package net.wooden.tutorial;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 5: your own creative tab</h2>
 *
 * The ember wood of Lesson 4 used {@code noCreativeTabs()}, so its items are in no vanilla tab. This lesson makes
 * a "Wooden Tutorial" tab with every item of every tutorial wood.
 *
 * <p>A {@code WoodSet} has a getter for each item ({@code getSignItem()}, {@code getHangingSignItem()},
 * {@code getBoatItem()}, {@code getChestBoatItem()}, {@code getShelfItem()}). A part you did not ask for (like
 * willow's boat) is {@code null}, so check with {@code hasSigns()}, {@code hasBoats()}, ... first.
 */
public final class Lesson5OwnCreativeTab {
	public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
			Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "woods"),
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.wooden_tutorial.woods"))
					.icon(() -> new ItemStack(Lesson4NetherWood.EMBER.getSignItem()))
					.displayItems((parameters, output) -> {
						for (WoodSet wood : new WoodSet[] {Lesson1EverythingWood.MAPLE, Lesson2PickParts.WILLOW,
								Lesson3Rafts.REED, Lesson4NetherWood.EMBER}) {
							if (wood.hasSigns()) output.accept(wood.getSignItem());
							if (wood.hasHangingSigns()) output.accept(wood.getHangingSignItem());
							if (wood.hasBoats()) {
								output.accept(wood.getBoatItem());
								output.accept(wood.getChestBoatItem());
							}
							if (wood.hasShelf()) output.accept(wood.getShelfItem());
						}
					})
					.build());

	private Lesson5OwnCreativeTab() {
	}

	static void init() {
	}
}
