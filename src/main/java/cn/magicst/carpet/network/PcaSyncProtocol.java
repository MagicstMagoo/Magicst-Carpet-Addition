package cn.magicst.carpet.network;

import cn.magicst.carpet.ModInfo;
import cn.magicst.carpet.PcaMod;
import cn.magicst.carpet.PcaSettings;
import cn.magicst.carpet.fakefapi.PacketSender;
import cn.magicst.carpet.fakefapi.ServerPlayNetworking;
import cn.magicst.carpet.util.CarpetHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

// main class don't touch this!
public class PcaSyncProtocol {

    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);
    // ειε
    private static final Identifier ENABLE_PCA_SYNC_PROTOCOL = ModInfo.id("enable_pca_sync_protocol");
    private static final Identifier DISABLE_PCA_SYNC_PROTOCOL = ModInfo.id("disable_pca_sync_protocol");
    private static final Identifier UPDATE_ENTITY = ModInfo.id("update_entity");
    private static final Identifier UPDATE_BLOCK_ENTITY = ModInfo.id("update_block_entity");
    // εεΊε
    public static final Identifier SYNC_BLOCK_ENTITY = ModInfo.id("sync_block_entity");
    public static final Identifier SYNC_ENTITY = ModInfo.id("sync_entity");
    public static final Identifier CANCEL_SYNC_BLOCK_ENTITY = ModInfo.id("cancel_sync_block_entity");
    public static final Identifier CANCEL_SYNC_ENTITY = ModInfo.id("cancel_sync_entity");
    private static final Map<ServerPlayerEntity, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayerEntity, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayerEntity>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayerEntity>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> identifierBlockPosPair = new MutablePair<>();

    // ιη₯ε?’ζ·η«―ζε‘ε¨ε·²ε―η¨ PcaSyncProtocol
    public static void enablePcaSyncProtocol(@NotNull ServerPlayerEntity player) {
        // ε¨θΏεε¦ζζ―ε¨ BC η«―ηζε΅δΈοΌServerPlayNetworking.canSend ε¨θΏδΈͺζΆζΊθ°η¨δΌεΊη°ιθ――
        ModInfo.LOGGER.debug("Try enablePcaSyncProtocol: {}", player.getName().getString());
        // bc η«―ζ―θΎε₯ζͺοΌcanSend ε·₯δ½δΈζ­£εΈΈ
        // if (ServerPlayNetworking.canSend(player, ENABLE_PCA_SYNC_PROTOCOL)) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, ENABLE_PCA_SYNC_PROTOCOL, buf);
        ModInfo.LOGGER.debug("send enablePcaSyncProtocol to {}!", player.getName().getString());
        lock.lock();
        lock.unlock();
    }

    // ιη₯ε?’ζ·η«―ζε‘ε¨ε·²εη¨ PcaSyncProtocol
    public static void disablePcaSyncProtocol(@NotNull ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, DISABLE_PCA_SYNC_PROTOCOL, buf);
        ModInfo.LOGGER.debug("send disablePcaSyncProtocol to {}!", player.getName().getString());
    }

    // ιη₯ε?’ζ·η«―ζ΄ζ° Entity
    // εεεε« World η Identifier, entityId, entity η nbt ζ°ζ?
    // δΌ θΎ World ζ―δΈΊδΊιη₯ε?’ζ·η«―θ―₯ Entity ε±δΊεͺδΈͺ World
    public static void updateEntity(@NotNull ServerPlayerEntity player, @NotNull Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(entity.getEntityWorld().getRegistryKey().getValue());
        buf.writeInt(entity.getId());
        buf.writeNbt(entity.writeNbt(new NbtCompound()));
        ServerPlayNetworking.send(player, UPDATE_ENTITY, buf);
    }

    // ιη₯ε?’ζ·η«―ζ΄ζ° BlockEntity
    // εεεε« World η Identifier, pos, blockEntity η nbt ζ°ζ?
    // δΌ θΎ World ζ―δΈΊδΊιη₯ε?’ζ·η«―θ―₯ BlockEntity ε±δΊεͺδΈͺδΈη
    public static void updateBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockEntity blockEntity) {
        World world = blockEntity.getWorld();

        // ε¨ηζδΈηζΆε―θ½δΌδΊ§ηη©Ίζι
        if (world == null) {
            return;
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(world.getRegistryKey().getValue());
        buf.writeBlockPos(blockEntity.getPos());
        buf.writeNbt(blockEntity.createNbt());
        ServerPlayNetworking.send(player, UPDATE_BLOCK_ENTITY, buf);
    }

    public static void init() {
//        ServerPlayNetworking.registerGlobalReceiver(SYNC_BLOCK_ENTITY, PcaSyncProtocol::syncBlockEntityHandler);
//        ServerPlayNetworking.registerGlobalReceiver(SYNC_ENTITY, PcaSyncProtocol::syncEntityHandler);
//        ServerPlayNetworking.registerGlobalReceiver(CANCEL_SYNC_BLOCK_ENTITY, PcaSyncProtocol::cancelSyncBlockEntityHandler);
//        ServerPlayNetworking.registerGlobalReceiver(CANCEL_SYNC_ENTITY, PcaSyncProtocol::cancelSyncEntityHandler);
//        ServerPlayConnectionEvents.JOIN.register(PcaSyncProtocol::onJoin);
//        ServerPlayConnectionEvents.DISCONNECT.register(PcaSyncProtocol::onDisconnect);
    }

    public static void onDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        if (PcaSettings.pcaSyncProtocol) {
            ModInfo.LOGGER.debug("onDisconnect remove: {}", serverPlayNetworkHandler.player.getName().getString());
        }
        PcaSyncProtocol.clearPlayerWatchData(serverPlayNetworkHandler.player);
    }

    public static void onJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        if (PcaSettings.pcaSyncProtocol) {
            enablePcaSyncProtocol(serverPlayNetworkHandler.player);
        }
    }

    // ε?’ζ·η«―ιη₯ζε‘η«―εζΆ BlockEntity εζ­₯
    public static void cancelSyncBlockEntityHandler(MinecraftServer server, ServerPlayerEntity player,
                                                     ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                     PacketSender responseSender) {
        if (!PcaSettings.pcaSyncProtocol) {
            return;
        }
        ModInfo.LOGGER.debug("{} cancel watch blockEntity.", player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchBlock(player);
    }

    // ε?’ζ·η«―ιη₯ζε‘η«―εζΆ Entity εζ­₯
    public static void cancelSyncEntityHandler(MinecraftServer server, ServerPlayerEntity player,
                                                ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                PacketSender responseSender) {
        if (!PcaSettings.pcaSyncProtocol) {
            return;
        }
        ModInfo.LOGGER.debug("{} cancel watch entity.", player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }

    // ε?’ζ·η«―θ―·ζ±εζ­₯ BlockEntity
    // εεεε« pos
    // η±δΊζ­£εΈΈηεΊζ―δΈθ¬δΈδΌθ·¨δΈηθ―·ζ±ζ°ζ?οΌε ζ­€εεεΉΆδΈεε« WorldοΌδ»₯η©ε?Άζε¨η World δΈΊε
    public static void syncBlockEntityHandler(MinecraftServer server, ServerPlayerEntity player,
                                               ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                               PacketSender responseSender) {
        if (!PcaSettings.pcaSyncProtocol) {
            return;
        }
        BlockPos pos = buf.readBlockPos();
        ServerWorld world = player.getWorld();
        BlockState blockState = world.getBlockState(pos);
        clearPlayerWatchData(player);
        ModInfo.LOGGER.debug("{} watch blockpos {}: {}", player.getName().getString(), pos, blockState);

        BlockEntity blockEntityAdj = null;
        // δΈζ―εδΈͺη?±ε­ειθ¦ζ΄ζ°ιε£η?±ε­
        if (blockState.getBlock() instanceof ChestBlock) {
            if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                BlockPos posAdj = pos.offset(ChestBlock.getFacing(blockState));
                // The method in World now checks that the caller is from the same thread...
                blockEntityAdj = world.getWorldChunk(posAdj).getBlockEntity(posAdj);
            }
        } else if (blockState.isOf(Blocks.BARREL) && CarpetHelper.getBoolRuleValue("largeBarrel")) {
            Direction directionOpposite = blockState.get(BarrelBlock.FACING).getOpposite();
            BlockPos posAdj = pos.offset(directionOpposite);
            BlockState blockStateAdj = world.getBlockState(posAdj);
            if (blockStateAdj.isOf(Blocks.BARREL) && blockStateAdj.get(BarrelBlock.FACING) == directionOpposite) {
                blockEntityAdj = world.getWorldChunk(posAdj).getBlockEntity(posAdj);
            }
        }

        if (blockEntityAdj != null) {
            updateBlockEntity(player, blockEntityAdj);
        }

        // ζ¬ζ₯ζ³ε€ζ­δΈδΈ blockState η±»εεδΈͺη½εεηοΌθθε° client ε·²η»εδΊε€ζ­ε°±δΈε¨ζε‘η«―εε€ζ­δΊ
        // ε°±η?θ’«ζΆζζ»ε»εΊθ―₯δΈδΌι ζδ»δΉζε€±
        // ε€§δΈδΊ op η΄ζ₯ζι»
        // The method in World now checks that the caller is from the same thread...
        BlockEntity blockEntity = world.getWorldChunk(pos).getBlockEntity(pos);
        if (blockEntity != null) {
            updateBlockEntity(player, blockEntity);
        }

        Pair<Identifier, BlockPos> pair = new ImmutablePair<>(player.getEntityWorld().getRegistryKey().getValue(), pos);
        lock.lock();
        playerWatchBlockPos.put(player, pair);
        if (!blockPosWatchPlayerSet.containsKey(pair)) {
            blockPosWatchPlayerSet.put(pair, new HashSet<>());
        }
        blockPosWatchPlayerSet.get(pair).add(player);
        lock.unlock();
    }

    // ε?’ζ·η«―θ―·ζ±εζ­₯ Entity
    // εεεε« entityId
    // η±δΊζ­£εΈΈηεΊζ―δΈθ¬δΈδΌθ·¨δΈηθ―·ζ±ζ°ζ?οΌε ζ­€εεεΉΆδΈεε« WorldοΌδ»₯η©ε?Άζε¨η World δΈΊε
    public static void syncEntityHandler(MinecraftServer server, ServerPlayerEntity player,
                                          ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                          PacketSender responseSender) {
        if (!PcaSettings.pcaSyncProtocol) {
            return;
        }
        int entityId = buf.readInt();
        ServerWorld world = player.getWorld();
        Entity entity = world.getEntityById(entityId);
        if (entity == null) {
            ModInfo.LOGGER.debug("Can't find entity {}.", entityId);
        } else {
            clearPlayerWatchData(player);
            ModInfo.LOGGER.debug("{} watch entity {}: {}", player.getName().getString(), entityId, entity);
            updateEntity(player, entity);

            Pair<Identifier, Entity> pair = new ImmutablePair<>(entity.getEntityWorld().getRegistryKey().getValue(), entity);
            lock.lock();
            playerWatchEntity.put(player, pair);
            if (!entityWatchPlayerSet.containsKey(pair)) {
                entityWatchPlayerSet.put(pair, new HashSet<>());
            }
            entityWatchPlayerSet.get(pair).add(player);
            lock.unlock();
        }
    }

    private static MutablePair<Identifier, Entity> getIdentifierEntityPair(Identifier identifier, Entity entity) {
        pairLock.lock();
        identifierEntityPair.setLeft(identifier);
        identifierEntityPair.setRight(entity);
        pairLock.unlock();
        return identifierEntityPair;
    }

    private static MutablePair<Identifier, BlockPos> getIdentifierBlockPosPair(Identifier identifier, BlockPos pos) {
        pairLock.lock();
        identifierBlockPosPair.setLeft(identifier);
        identifierBlockPosPair.setRight(pos);
        pairLock.unlock();
        return identifierBlockPosPair;
    }

    // ε·₯ε·
    private static @Nullable Set<ServerPlayerEntity> getWatchPlayerList(@NotNull Entity entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(entity.getEntityWorld().getRegistryKey().getValue(), entity));
    }

    private static @Nullable Set<ServerPlayerEntity> getWatchPlayerList(@NotNull World world, @NotNull BlockPos blockPos) {
        return blockPosWatchPlayerSet.get(getIdentifierBlockPosPair(world.getRegistryKey().getValue(), blockPos));
    }

    public static boolean syncEntityToClient(@NotNull Entity entity) {
        if (entity.getEntityWorld().isClient()) {
            return false;
        }
        lock.lock();
        Set<ServerPlayerEntity> playerList = getWatchPlayerList(entity);
        boolean ret = false;
        if (playerList != null) {
            for (ServerPlayerEntity player : playerList) {
                updateEntity(player, entity);
                ret = true;
            }
        }
        lock.unlock();
        return ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity blockEntity) {
        boolean ret = false;
        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();
        // ε¨ηζδΈηζΆε―θ½δΌδΊ§ηη©Ίζι
        if (world != null) {
            if (world.isClient()) {
                return false;
            }
            BlockState blockState = world.getBlockState(pos);
            lock.lock();
            Set<ServerPlayerEntity> playerList = getWatchPlayerList(world, blockEntity.getPos());

            Set<ServerPlayerEntity> playerListAdj = null;

            if (blockState.getBlock() instanceof ChestBlock) {
                if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                    // ε¦ζζ―δΈδΈͺε€§η?±ε­ιθ¦ηΉζ?ε€η
                    // δΈι’δΈη¨ isOf ζ―δΈΊδΊθθε°ι·ι±η?±ηζε΅οΌι·ι±η?±η»§ζΏθͺη?±ε­
                    BlockPos posAdj = pos.offset(ChestBlock.getFacing(blockState));
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            } else if (blockState.isOf(Blocks.BARREL) && CarpetHelper.getBoolRuleValue("largeBarrel")) {
                Direction directionOpposite = blockState.get(BarrelBlock.FACING).getOpposite();
                BlockPos posAdj = pos.offset(directionOpposite);
                BlockState blockStateAdj = world.getBlockState(posAdj);
                if (blockStateAdj.isOf(Blocks.BARREL) && blockStateAdj.get(BarrelBlock.FACING) == directionOpposite) {
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            }
            if (playerListAdj != null) {
                if (playerList == null) {
                    playerList = playerListAdj;
                } else {
                    playerList.addAll(playerListAdj);
                }
            }

            if (playerList != null) {
                for (ServerPlayerEntity player : playerList) {
                    updateBlockEntity(player, blockEntity);
                    ret = true;
                }
            }
            lock.unlock();
        }
        return ret;
    }

    private static void clearPlayerWatchEntity(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, Entity> pair = playerWatchEntity.get(player);
        if (pair != null) {
            Set<ServerPlayerEntity> playerSet = entityWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                entityWatchPlayerSet.remove(pair);
            }
            playerWatchEntity.remove(player);
        }
        lock.unlock();
    }

    private static void clearPlayerWatchBlock(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, BlockPos> pair = playerWatchBlockPos.get(player);
        if (pair != null) {
            Set<ServerPlayerEntity> playerSet = blockPosWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                blockPosWatchPlayerSet.remove(pair);
            }
            playerWatchBlockPos.remove(player);
        }
        lock.unlock();
    }

    // εη¨ PcaSyncProtocol
    public static void disablePcaSyncProtocolGlobal() {
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();
        if (PcaMod.server != null) {
            for (ServerPlayerEntity player : PcaMod.server.getPlayerManager().getPlayerList()) {
                disablePcaSyncProtocol(player);
            }
        }
    }

    // ε―η¨ PcaSyncProtocol
    public static void enablePcaSyncProtocolGlobal() {
        if (PcaMod.server == null) {
            return;
        }
        for (ServerPlayerEntity player : PcaMod.server.getPlayerManager().getPlayerList()) {
            enablePcaSyncProtocol(player);
        }
    }


    // ε ι€η©ε?Άζ°ζ?
    public static void clearPlayerWatchData(ServerPlayerEntity player) {
        PcaSyncProtocol.clearPlayerWatchBlock(player);
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }
}
