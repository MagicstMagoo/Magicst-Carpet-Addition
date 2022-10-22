package cn.magicst.carpet;
import carpet.settings.Rule;
import java.lang.annotation.Target;

import static carpet.settings.RuleCategory.*;

public class PcaSettings {
    public static boolean pcaSyncProtocol = true;

    static final String MCA = ModInfo.MOD_ID;
    @Rule(
            desc = "Observer will be activied when player uses Flint and Steel on it.",
            extra = {"Sneak to light fire on observers."},
            category = {MCA, CREATIVE}
    )
    public static boolean flintAndSteelActivatesObserver = false;

    @Rule(
            desc = "Enable the usage of /dumpentity",
            category = {MCA, CREATIVE, COMMAND}
    )
    public static String commandDumpEntity = "true";
    public static boolean fakePlayerGamemode = true;
}
