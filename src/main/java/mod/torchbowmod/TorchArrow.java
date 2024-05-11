package mod.torchbowmod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;

public class TorchArrow extends ArrowItem {

    public TorchArrow(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        EntityTorch torch = new EntityTorch(level, livingEntity, itemStack.copyWithCount(1));
        return torch;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity owner) {
        return false;
    }
}
