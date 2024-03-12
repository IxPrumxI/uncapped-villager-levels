package me.prum.uncappedvillagerlevels.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin extends VillagerEntity {
	public VillagerMixin(EntityType<? extends VillagerEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract VillagerData getVillagerData();
	@Shadow public abstract void setVillagerData(VillagerData villagerData);

	@Overwrite()
	public boolean canLevelUp() {
		int i = this.getVillagerData().getLevel();
		return this.getExperience() >= getUpperExperienceForLevel(i);
	}

	private static int getUpperExperienceForLevel(int level) {
		// Calculate the experience required for each level
		// Starting values are 0, 10, 70, 150, 250
		// After that, each level requires 50% more experience than the previous level.

		int experience;
		if(level < 0) level = 0;
		switch (level) {
			case 0 -> experience = 0;
			case 1 -> experience = 10;
			case 2 -> experience = 70;
			case 3 -> experience = 150;
			case 4 -> experience = 250;
			default -> {
				experience = 250;
				for (int i = 5; i < level; i++) {
					experience *= 1.5;
				}
			}
		}

		return experience;
	}

	// I can't figure out how to use @Inject to add a call to increaseMaxUses() to the end of this method.
	// So I'm just overwriting the method entirely.
	@Overwrite()
	public void levelUp() {
		this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().getLevel() + 1));
		this.fillRecipes();
		increaseMaxUses();
	}

	private void increaseMaxUses() {
		TradeOfferList currentOffers = this.getOffers();
		TradeOfferList newOffers = new TradeOfferList();

		// Increase the max uses of each offer
		// For a starting 16 max uses we increase every level.
		// For a starting 12 max uses that are books we increase every 3 levels. ( level % 3 == 0 )
		// For any 12 starting max uses left we increase every 2 levels. ( level % 2 == 0 )
		// For a starting 3 max uses we increase every 5 levels. ( level % 5 == 0 )
		// 3 max uses are enchanted but not books.

		for (TradeOffer offer : currentOffers) {
			NbtCompound nbt = offer.toNbt();
			int maxUses = nbt.getInt("maxUses");

			// Check if the item is enchanted but not a book
			ItemStack sellItem = offer.getSellItem();

			if(sellItem.getItem().equals(Items.ENCHANTED_BOOK)){
				// Item is an enchanted book.
				maxUses++; // every level
			} else if(sellItem.hasEnchantments()) {
				// Item is enchanted but not a book (I.E. tools, armor, etc.).
				if(this.getVillagerData().getLevel() % 5 == 0) maxUses++;
			} else if(sellItem.isFood()) {
				// Item is food.
				maxUses++; // every level
			} else if(sellItem.getItem().equals(Items.EMERALD) ) {
				// Item is sold for emeralds. (I.E. sticks, wheat, carrots, etc.)
				maxUses++; // every level
			} else {
				// Item is anything else.
				if(this.getVillagerData().getLevel() % 2 == 0) maxUses++;
			}

			nbt.putInt("maxUses", maxUses);
			TradeOffer newOffer = new TradeOffer(nbt);
			newOffer.resetUses();

			newOffers.add(newOffer);
		}

		// this.setOffersFromServer(newOffers); //Doesn't work
		this.offers = newOffers;
	}
}