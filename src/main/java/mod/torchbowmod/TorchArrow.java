package mod.torchbowmod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TorchArrow extends ArrowItem {

    public TorchArrow(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, ItemStack itemStack, @NotNull LivingEntity livingEntity, @Nullable ItemStack weaponStack) {
        return new EntityTorch(level, livingEntity, itemStack.copyWithCount(1),weaponStack);
    }
}
