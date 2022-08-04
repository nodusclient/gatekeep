package gg.nodus.gatekeep.key;

import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.Signer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GatekeepProfileKeys extends ProfileKeys {
    private final PlayerKeyPair keyPair;
    private final PlayerPublicKey publicKey;
    private final Signer signer;


    public GatekeepProfileKeys(UserApiService userApiService, UUID uuid, Path root, File file) throws IOException {
        super(userApiService, uuid, root);
        var keyString = Files.readString(file.toPath());
        var keys = PlayerKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(keyString)).result();
        if (!keys.isPresent() || keys.get().isExpired()) {
            throw new RuntimeException("key already expired");
        }
        this.keyPair = keys.get();
        this.publicKey = keys.get().publicKey();
        this.signer = Signer.create(keys.get().privateKey(), "SHA256withRSA");
    }

    @Nullable
    @Override
    public Signer getSigner() {
        return signer;
    }

    @Override
    public Optional<PlayerPublicKey> getPublicKey() {
        return Optional.of(publicKey);
    }

    @Override
    public Optional<PlayerPublicKey.PublicKeyData> getPublicKeyData() {
        return super.getPublicKeyData();
    }
}
