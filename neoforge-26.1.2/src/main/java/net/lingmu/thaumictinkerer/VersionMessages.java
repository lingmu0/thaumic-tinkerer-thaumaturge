package net.lingmu.thaumictinkerer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
public final class VersionMessages {
    public static void status(Player player, Component message) {send(player,message,true);}
    public static void send(Player player,Component message,boolean overlay) {
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(message, overlay);
    }
}
