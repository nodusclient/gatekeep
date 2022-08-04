package gg.nodus.gatekeep.mixin;

import com.google.common.hash.Hashing;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.serialization.JsonOps;
import gg.nodus.gatekeep.key.GatekeepProfileKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.network.encryption.PlayerKeyPair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ProfileKeys.class)
public class MixinProfileKeys {

    @Inject(method = "getKeyPair", at = @At("HEAD"), cancellable = true)
    public void getKeyPair(UserApiService userApiService, CallbackInfoReturnable<CompletableFuture<Optional<PlayerKeyPair>>> cir) {
        if ((Class<?>) this.getClass() == GatekeepProfileKeys.class) {
            cir.setReturnValue(CompletableFuture.supplyAsync(Optional::empty));
        }
    }

    @Inject(method = "saveKeyPairToFile", at = @At("HEAD"))
    public void saveKeyPairToFile(PlayerKeyPair keyPair, CallbackInfo ci) {
        PlayerKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, keyPair).result().ifPresent(json -> {
            var string = json.toString();
            var hash = Hashing.sha256().hashBytes(string.getBytes(StandardCharsets.UTF_8)).toString();
            var folder = MinecraftClient.getInstance().runDirectory.toPath().resolve("gatekeep").resolve(MinecraftClient.getInstance().getSession().getUuid());
            folder.toFile().mkdirs();
            var path = folder.resolve(hash);
            try {
                Files.writeString(path, string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
