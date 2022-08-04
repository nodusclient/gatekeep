package gg.nodus.gatekeep.key;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.network.encryption.PlayerPublicKey;

import java.nio.file.Path;
import java.util.*;

public class KeyRegistry {

    private static KeyRegistry keyRegistry = null;

    private final List<ProfileKeys> profileKeys = new ArrayList<>();

    public KeyRegistry(Path root, UserApiService userApiService, UUID uuid) {
        if (keyRegistry != null) {
            throw new IllegalStateException();
        }
        var dir = root.resolve("gatekeep").resolve(uuid.toString()).toFile();
        dir.mkdirs();
        var keys = dir.listFiles();
        for (var key : keys) {
            try {
                var profileKey = new GatekeepProfileKeys(userApiService, uuid, root, key);
                this.profileKeys.add(profileKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        keyRegistry = this;
    }

    public List<ProfileKeys> getProfileKeys() {
        return profileKeys;
    }

    public static KeyRegistry getInstance() {
        return keyRegistry;
    }

    public Optional<ProfileKeys> getOldestValidKey() {
        return profileKeys.stream()
                .filter(x -> x.getPublicKeyData().map(PlayerPublicKey.PublicKeyData::isExpired).orElse(Boolean.FALSE))
                .min(Comparator.comparingLong(x -> x.getPublicKeyData().get().expiresAt().toEpochMilli()));
    }

}
