package cn.magicst.carpet.mixin.rule.pcaSyncProtocol.block;

import cn.magicst.carpet.ModInfo;
import cn.magicst.carpet.PcaSettings;
import cn.magicst.carpet.network.PcaSyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispenserBlockEntity.class)
public abstract class MixinDispenserBlockEntity extends LootableContainerBlockEntity {

    protected MixinDispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PcaSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            ModInfo.LOGGER.debug("update DispenserBlockEntity: {}", this.pos);
        }
    }
}