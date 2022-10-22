package cn.magicst.carpet;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.magicst.carpet.command.DumpEntityCommand;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import java.util.Optional;

public class ModInfo implements CarpetExtension{
    public static final String MOD_ID = "pca";  // still use pca here so protocol works
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static String MOD_VERSION;
    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
    @Override
    public String version() {
        return MOD_ID;
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(PcaSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        DumpEntityCommand.registerCommand(dispatcher);
    }

    static {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(MOD_ID);
        modContainerOptional.ifPresent(modContainer -> MOD_VERSION = modContainer.getMetadata().getVersion().getFriendlyString());
    }
}
