package net.wooden.tutorial;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.wooden.api.wood.WoodSet;

/**
 * <h2>Lesson 5: your own creative tab</h2>
 *
 * The ember wood of Lesson 4 used {@code noCreativeTabs()}, so its items are in no vanilla tab. This lesson makes
 * a "Wooden Tutorial" tab (an item group) with every item of every tutorial wood.
 *
 * <p>A {@code WoodSet} has a getter for each item ({@code getSignItem()}, {@code getHangingSignItem()},
 * {@code getBoatItem()}, {@code getChestBoatItem()}, {@code getShelfItem()}). A part you did not ask for (like
 * willow's boat) is {@code null}, so check with {@code hasSigns()}, {@code hasBoats()}, ... first.
 */
public final class Lesson5OwnCreativeTab {
	public static final ItemGroup TAB = Registry.register(Registries.ITEM_GROUP,
			Identifier.of(TutorialMod.MOD_ID, "woods"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.wooden_tutorial.woods"))
					.icon(() -> new ItemStack(Lesson4NetherWood.EMBER.getSignItem()))
					.entries((context, entries) -> {
						for (WoodSet wood : new WoodSet[] {Lesson1EverythingWood.MAPLE, Lesson2PickParts.WILLOW,
								Lesson3Rafts.REED, Lesson4NetherWood.EMBER}) {
							if (wood.hasSigns()) entries.add(wood.getSignItem());
							if (wood.hasHangingSigns()) entries.add(wood.getHangingSignItem());
							if (wood.hasBoats()) {
								entries.add(wood.getBoatItem());
								entries.add(wood.getChestBoatItem());
							}
							if (wood.hasShelf()) entries.add(wood.getShelfItem());
						}
					})
					.build());

	private Lesson5OwnCreativeTab() {
	}

	static void init() {
	}
}
