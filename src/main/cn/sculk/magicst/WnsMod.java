package cn.sculk.magicst;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.network.CarpetClient;
import carpet.network.ClientNetworkHandler;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import cn.sculk.magicst.network.PcaSyncProtocol;
import cn.sculk.magicst.util.rule.dispenserCollectXp.GlassBottleDispenserBehavior;
import cn.sculk.magicst.util.rule.dispenserFixIronGolem.IronIngotDispenserBehavior;
import cn.sculk.magicst.util.rule.flippingTotemOfUndying.FlipCooldown;
import cn.sculk.magicst.util.rule.gravestone.GravestoneUtil;
import cn.sculk.magicst.util.rule.potionRecycle.PotionDispenserBehavior;
import cn.sculk.magicst.util.rule.sleepingDuringTheDay.SleepUtil;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;


public class PcaMod implements CarpetExtension, ModInitializer, ClientModInitializer {
    public static final boolean tisCarpetLoaded = FabricLoader.getInstance().isModLoaded("carpet-tis-addition");
    @Nullable
    public static MinecraftServer server = null;

    static {
        CarpetServer.manageExtension(new PcaMod());
    }

    @Override
    public void onGameStarted() {
        // let's /carpet handle our few simple settings
        // CarpetServer.settingsManager.parseSettingsClass(ExampleSimpleSettings.class);
        // Lets have our own settings class independent from carpet.conf
        CarpetServer.settingsManager.parseSettingsClass(PcaSettings.class);
        // set-up a snooper to observe how rules are changing in carpet
        CarpetServer.settingsManager.addRuleObserver((serverCommandSource, currentRuleState, originalUserTest) ->
        {
            if (currentRuleState.categories.contains("WNS")) {
                if (currentRuleState.name.equals("wnsSyncProtocol")) {
                    if (currentRuleState.getBoolValue()) {
                        PcaSyncProtocol.enablePcaSyncProtocolGlobal();
                    } else {
                        PcaSyncProtocol.disablePcaSyncProtocolGlobal();
                    }
                } else if (currentRuleState.name.equals("wnsDebug")) {
                    if (currentRuleState.getBoolValue()) {
                        Configurator.setLevel(ModInfo.LOGGER.getName(), Level.toLevel("DEBUG"));
                    } else {
                        Configurator.setLevel(ModInfo.LOGGER.getName(), Level.toLevel("INFO"));
                    }
                }
            }
        });
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        // reloading of /carpet settings is handled by carpet
        // reloading of own settings is handled as an extension, since we claim own settings manager
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        PcaSyncProtocol.init();
        FlipCooldown.init();
        PcaMod.server = server;
        if (PcaSettings.pcaDebug) {
            Configurator.setLevel(ModInfo.LOGGER.getName(), Level.toLevel("DEBUG"));
        }
    }

    @Override
    public void onTick(MinecraftServer server) {
        // no need to add this.
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CarpetCommand.register(dispatcher);
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayerEntity player) {
        // 在这写如果是在 BC 端的情况下，ServerPlayNetworking.canSend 在这个时机调用会出现错误
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayerEntity player) {
        PcaSyncProtocol.clearPlayerWatchData(player);
        FlipCooldown.removePlayer(player);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        String dataJSON;
        try {
            dataJSON = IOUtils.toString(
                    Objects.requireNonNull(Translations.class.getClassLoader().getResourceAsStream(
                            String.format("assets/pca/lang/%s.json", lang))),
                    StandardCharsets.UTF_8);
        } catch (NullPointerException | IOException e) {
            return null;
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @Override
    public void onInitialize() {
        GravestoneUtil.init();
        SleepUtil.init();
        IronIngotDispenserBehavior.init();
        GlassBottleDispenserBehavior.init();
        PotionDispenserBehavior.init();
    }

    @Override
    public void onInitializeClient() {
        MultiConnectAPI.instance().addClientboundIdentifierCustomPayloadListener(event -> {
            Identifier channel = event.getChannel();
            if (channel.equals(CarpetClient.CARPET_CHANNEL)) {
                ClientNetworkHandler.handleData(event.getData(), MinecraftClient.getInstance().player);
            }
        });
        MultiConnectAPI.instance().addServerboundIdentifierCustomPayloadListener(event -> {
            Identifier channel = event.getChannel();
            if (channel.equals(CarpetClient.CARPET_CHANNEL)) {
                MultiConnectAPI.instance().forceSendCustomPayload(event.getNetworkHandler(), event.getChannel(), event.getData());
            }
        });
    }
}