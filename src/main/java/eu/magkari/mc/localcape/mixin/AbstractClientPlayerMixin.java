package eu.magkari.mc.localcape.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.magkari.mc.localcape.Localcape;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
	@ModifyReturnValue( at = @At("RETURN"), method = "getSkin")
	private PlayerSkin init(PlayerSkin original) {
        var patch = Localcape.loadCape((Player) (Object) this);
        if (patch != null) {
            return original.with(patch);
        }
        return original;
    }
}