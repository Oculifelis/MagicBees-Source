package com.magicbees.mixin;

import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.accbdd.complicated_bees.block.entity.BaseBeeHousing;
import com.magicbees.HousingBoosts;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets blocks beside a housing add modifiers; Complicated Bees itself only reads its frame slots. */
@Mixin(value = BaseBeeHousing.class, remap = false)
public abstract class BaseBeeHousingMixin {
    @Inject(method = "getHousingModifiers", at = @At("RETURN"))
    private void magicbees$addBoosts(CallbackInfoReturnable<List<BeeHousingModifier>> cir) {
        HousingBoosts.apply((BlockEntity) (Object) this, cir.getReturnValue());
    }
}
