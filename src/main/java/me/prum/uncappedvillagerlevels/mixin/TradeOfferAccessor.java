package me.prum.uncappedvillagerlevels.mixin;

import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TradeOffer.class)
public interface TradeOfferAccessor {
    @Accessor
    void setUses(int uses);

    @Mutable
    @Accessor
    void setMaxUses(int maxUses);
}
