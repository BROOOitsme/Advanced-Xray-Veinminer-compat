package pro.mikey.xray.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.mikey.xray.core.ScanController;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    public void onBlockDestroy(BlockPos arg, BlockState arg2, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            Level level = (Level) (Object) this;
            if (!level.isClientSide()) {
                return;
            }

            // Existing per-block removal (unchanged from upstream).
            ScanController.onBlockChange(level, arg, arg2);

            // Veinminer / JustHammers break many blocks in a tick. The async scanner
            // can re-add already-broken blocks from stale chunk data, leaving ghost
            // outlines. If XRay is currently active, force a full rescan so the
            // outline list reflects reality.
            if (arg2.isAir() && ScanController.INSTANCE.isXRayActive()) {
                ScanController.INSTANCE.requestBlockFinder(true);
            }
        }
    }
}
