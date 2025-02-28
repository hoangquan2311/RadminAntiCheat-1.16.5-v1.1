package net.dreamanticheat.home.mixin;

import com.mojang.authlib.GameProfile;
import net.dreamanticheat.home.DreamAntiCheat;
import net.dreamanticheat.home.TextColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(ServerLoginNetworkHandler.class)
public abstract class AntiLogAcc {

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private GameProfile profile;

    @Shadow
    @Final
    public ClientConnection connection;

    @Inject(method = "acceptPlayer", at = @At("HEAD"), cancellable = true)
    private void acceptPlayer(CallbackInfo info) {
        UUID uUID = PlayerEntity.getUuidFromProfile(this.profile);
        ServerPlayerEntity serverPlayerEntity = this.server.getPlayerManager().getPlayer(uUID);


        String playerName = this.profile.getName();
        if (!playerName.matches("[a-zA-Z0-9_]+")) {
            MutableText msg = TextColor.text("Tên không hợp lệ.\nCác ký tự cho phép: [a-zA-Z0-9_]+", Formatting.RED);
            MutableText suffix = TextColor.text("\n\nMod by Dream_Da_Vang", Formatting.GRAY);
            msg.append(suffix);
            this.connection.send(new LoginDisconnectS2CPacket(msg));
            this.connection.disconnect(msg);
            MutableText hostMsg = TextColor.text("Hệ thống đã kick " + playerName + " \nLý do: Tên không hợp lệ.\nMod by Dream_Da_Vang", Formatting.YELLOW);
            for (ServerPlayerEntity entity : server.getPlayerManager().getPlayerList()) {
                entity.sendMessage(hostMsg, false);
            }
        }
        else if (serverPlayerEntity != null) {
            String address = this.connection.getAddress().toString();
            int colonIndex = address.indexOf(':');
            String ip = address.substring(1,colonIndex);
            MutableText reason = (TextColor.text("Bạn không thể log acc\nNgười chơi ", Formatting.RED));
            MutableText reason1 = (TextColor.text(serverPlayerEntity.getName().getString(), Formatting.GREEN));
            MutableText reason2 = (TextColor.text(" hiện đang online.", Formatting.RED));
            MutableText reason3 = (TextColor.text("\n\nMod by Dream_Da_Vang", Formatting.GRAY));
            reason.append(reason1).append(reason2).append(reason3);
            this.connection.send(new LoginDisconnectS2CPacket(reason));
            this.connection.disconnect(reason);
            MutableText msg = TextColor.text("\nPhát hiện địa chỉ IP: ", Formatting.RED);
            MutableText m2 = TextColor.text(ip,Formatting.YELLOW);
            MutableText m3 = TextColor.text(" đang cố log acc bằng tên của bạn, hệ thống đã tự động kick người này.\n", Formatting.RED);
            msg.append(m2).append(m3);

            String loggerName = null;
            for (int i = 0; i < this.server.getPlayerManager().getPlayerList().size(); ++i) {
                ServerPlayerEntity player = (ServerPlayerEntity) this.server.getPlayerManager().getPlayerList().get(i);
                if (player.getIp().equals(ip) && !player.getName().getString().equals("Dream_Da_Vang")) {
                    loggerName = player.getName().getString();
                    break;
                }
            }
            String victimName = serverPlayerEntity.getName().getString();
            MutableText trungIP = TextColor.text("\nIP này giống với IP của người chơi có tên: ", Formatting.GRAY);
            MutableText trungIP1 = TextColor.text(loggerName, Formatting.RED);
            trungIP.append(trungIP1);
            MutableText msgHost = TextColor.text("\nPhát hiện địa chỉ IP: ", Formatting.RED);
            MutableText msgHost1 = TextColor.text(ip, Formatting.YELLOW);
            MutableText msgHost2 = TextColor.text(" đang cố log acc của ", Formatting.RED);
            MutableText msgHost3 = TextColor.text(victimName+"\n", Formatting.GREEN);
            msgHost.append(msgHost1).append(msgHost2).append(msgHost3);
            if (loggerName != null) {
                serverPlayerEntity.sendMessage(msg.append(trungIP), false);
                DreamAntiCheat.sendMsgHost(msgHost.append(trungIP));
            } else {
                serverPlayerEntity.sendMessage(msg, false);
                DreamAntiCheat.sendMsgHost(msgHost);
            }
            serverPlayerEntity.playSound(SoundEvents.ENTITY_HORSE_DEATH, SoundCategory.AMBIENT,1f,1f);
            info.cancel();
        }
    }
}