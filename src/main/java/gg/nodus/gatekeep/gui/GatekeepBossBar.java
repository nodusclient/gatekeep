package gg.nodus.gatekeep.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class GatekeepBossBar extends ClientBossBar {
    public GatekeepBossBar(UUID uuid) {
        super(uuid, Text.literal("§aKey Expiry: 00:00"), 1.0f, Color.GREEN, Style.PROGRESS, false, false, false);
    }

    @Override
    public Text getName() {
        var keyData = MinecraftClient.getInstance().getProfileKeys().getPublicKeyData();
        if (keyData.isPresent()) {
            var keyExpiry = keyData.get().expiresAt();
            var timeRemaining = keyExpiry.toEpochMilli() - Instant.now().toEpochMilli();
            if (timeRemaining < 0) {
                return Text.literal("§aKey Expired!");
            }
            var hoursRemaining = timeRemaining / 1000 / 60 / 60 % 60;
            var minutesRemaining = timeRemaining / 1000 / 60 % 60;
            var secondsRemaining = timeRemaining / 1000 % 60;
            var timeString = String.format("§aKey Expiry: %02d:%02d:%02d", hoursRemaining, minutesRemaining, secondsRemaining);
            return Text.literal(timeString);
        }
        return super.getName();
    }

    @Override
    public float getPercent() {
        var keyData = MinecraftClient.getInstance().getProfileKeys().getPublicKeyData();
        if (keyData.isPresent()) {
            var keyExpiry = keyData.get().expiresAt();
            if (keyExpiry.isBefore(Instant.now())) {
                return 0;
            } else if (keyExpiry.isAfter(Instant.now().plus(Duration.ofMillis(10)))) {
                return 1;
            } else {
                var timeRemaining = keyExpiry.toEpochMilli() - Instant.now().toEpochMilli();
                return ((float) timeRemaining) / 60f / 10f / 1000f;
            }
        }
        return 0;
    }

    @Override
    public Color getColor() {
        var keyData = MinecraftClient.getInstance().getProfileKeys().getPublicKeyData();
        if (keyData.isPresent()) {
            if (keyData.get().isExpired()) {
                return Color.RED;
            }
        }
        return super.getColor();
    }
}
