package gg.nodus.gatekeep.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import gg.nodus.gatekeep.key.KeyRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Mutable
    @Shadow @Final private ProfileKeys profileKeys;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final private UserApiService userApiService;
    @Shadow @Final public File runDirectory;
    @Shadow @Final private Session session;
    private boolean hasSentExpiryNotification = false;
    private KeyRegistry keyRegistry;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(RunArgs args, CallbackInfo ci) {
        keyRegistry = new KeyRegistry(this.runDirectory.toPath(), userApiService, session.getUuidOrNull());
        var keys = keyRegistry.getOldestValidKey();
        if (keys.isPresent()) {
            profileKeys = keys.get();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    public void disconnect(CallbackInfo ci) {
        hasSentExpiryNotification = false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        profileKeys.getPublicKeyData().ifPresent(key -> {
            if (key.isExpired()) {
                if (!hasSentExpiryNotification) {
                    if (player != null) {
                        player.sendMessage(Text.literal("§a[Gatekeep] §7Your public key has now expired! Anyone who joins after this message will be disconnected when you chat."));
                    }
                    hasSentExpiryNotification = true;
                }
            }
        });
    }

}
