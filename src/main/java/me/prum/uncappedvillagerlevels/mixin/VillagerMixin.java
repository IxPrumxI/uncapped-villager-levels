package me.prum.uncappedvillagerlevels.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin extends MerchantEntity {

	// From VillagerEntity.java
	private final static int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 10, 70, 150, 250};

	public VillagerMixin(EntityType<? extends VillagerEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract VillagerData getVillagerData();
	@Shadow public abstract void setVillagerData(VillagerData villagerData);

	@Overwrite()
	public boolean canLevelUp() {
		int i = this.getVillagerData().getLevel();
		return this.getExperience() >= getExperienceForLevel(i + 1);
	}

	private static int getExperienceForLevel(int level) {
		// Calculate the experience required for each level
		// Starting values are 0, 10, 70, 150, 250
		// After that, each level requires 50% more experience than the previous level.

		int experience = 0;
		if (level < 0) {
			return 0;
		} else if (level < 5) {
			experience = LEVEL_BASE_EXPERIENCE[level];
		} else {
			experience = 250;
			for (int i = 5; i < level; i++) {
				experience *= 1.5;
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

		for (TradeOffer offer : currentOffers) {
			NbtCompound nbt = offer.toNbt();
			nbt.putInt("maxUses", nbt.getInt("maxUses") + 1);

			TradeOffer newOffer = new TradeOffer(nbt);
			newOffers.add(newOffer);
		}

		// this.setOffersFromServer(newOffers); //Doesn't work
		this.offers = newOffers;
	}
}