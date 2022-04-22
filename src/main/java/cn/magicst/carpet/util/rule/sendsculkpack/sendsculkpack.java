package cn.magicst.carpet.sendsculkpack;

import carpet.CarpetServer;
import cn.magicst.carpet.ModInfo;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mojang.maven;
import net.fabricmc.api.ModInitializer;
import carpet.patches.EntityPlayerMPFake;
import cn.magicst.carpet.ModInfo;
import cn.magicst.carpet.WnsMod;
import cn.magicst.carpet.WnsSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public static final boolean CarpetLoaded = FabricLoader.getInstance().isModLoaded("Carpet");
public static final boolean MagiclibLoader = FabricLoader.getInstance().isModLoaded("Magiclib");

public class sendsculkpack {
    
    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);
    // Senddataack
    private static final Identifier ENABLE_WNS_SYNC_PROTOCOL = ModInfo.id("enable_wns_sync_protocol");
    private static final Identifier DISABLE_WNS_SYNC_PROTOCOL = ModInfo.id("disable_wns_sync_protocol");
    private static final Identifier UPDATE_ENTITY = ModInfo.id("update_entity");
    private static final Identifier UPDATE_BLOCK_ENTITY = ModInfo.id("update_block_entity");
    // regen datapack
    private static final Identifier SYNC_BLOCK_ENTITY = ModInfo.id("sync_block_entity");
    private static final Identifier SYNC_ENTITY = ModInfo.id("sync_entity");
    private static final Identifier CANCEL_SYNC_BLOCK_ENTITY = ModInfo.id("cancel_sync_block_entity");
    private static final Identifier CANCEL_SYNC_ENTITY = ModInfo.id("cancel_sync_entity");
    private static final Map<ServerPlayerEntity, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayerEntity, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayerEntity>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayerEntity>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> identifierBlockPosPair = new MutablePair<>();

    // 通知客户端服务器数据包发送已启用
    public static void enablesendsculkpack(@NotNull ServerPlayerEntity player) {
        // 在这写如果是在 BC 端的情况下，ServerPlayNetworking.canSend 在这个时机调用会出现错误
        ModInfo.LOGGER.debug("Try enableWnsSyncProtocol: {}", player.getName().asString());
        // bc端canSend 工作不正常
        // if (ServerPlayNetworking.canSend(player, ENABLE_RES_SYNC_PROTOCOL)) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, ENABLE_WNS_SYNC_PROTOCOL, buf);
        ModInfo.LOGGER.debug("send enableSendDataPack to {}!", player.getName().asString());
        lock.lock();
        lock.unlock();
}