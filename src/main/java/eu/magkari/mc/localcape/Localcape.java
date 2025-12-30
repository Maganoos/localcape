package eu.magkari.mc.localcape;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Localcape implements ModInitializer {
	public static final String MOD_ID = "localcape";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            dispatcher.register(ClientCommandManager.literal("flush-capes")
                    .executes(commandContext -> {
                        CACHE.clear();
                        commandContext.getSource().sendFeedback(Component.literal("Flushed cache.").withStyle(ChatFormatting.AQUA));
                        return Command.SINGLE_SUCCESS;
                    }));
        });
    }

    private static final Path BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve(Localcape.MOD_ID);

    private static final Map<UUID, CachedCape> CACHE = new HashMap<>();

    public static PlayerSkin.Patch loadCape(Player player) {
        UUID uuid = player.getUUID();
        Path path = BASE_DIR.resolve(player.getGameProfile().name() + ".png");

        if (!Files.exists(path)) {
            CACHE.remove(uuid);
            return null;
        }

        try {
            long lastModified = Files.getLastModifiedTime(path).toMillis();
            CachedCape cached = CACHE.get(uuid);
            if (cached != null && cached.lastModified == lastModified) return cached.patch;

            byte[] data = Files.readAllBytes(path);

            Identifier id = Identifier.fromNamespaceAndPath(Localcape.MOD_ID, player.getGameProfile().name().toLowerCase(Locale.ROOT) + "_cape");
            NativeImage image = NativeImage.read(data);
            if (image.getHeight() != 32 || image.getWidth() != 64) {
                LOGGER.warn("Cape texture is invalid, defaulting.");
                return null;
            }
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            ClientAsset.ResourceTexture cape = new ClientAsset.ResourceTexture(id, id);
            PlayerSkin.Patch patch = PlayerSkin.Patch.create(
                    Optional.empty(),
                    Optional.of(cape),
                    Optional.empty(),
                    Optional.empty()
            );

            CACHE.put(uuid, new CachedCape(patch, lastModified));
            return patch;
        } catch (IOException e) {
            LOGGER.error("Failed to read cape, defaulting.", e);
            return null;
        }
    }

    private record CachedCape(PlayerSkin.Patch patch, long lastModified) {
    }

}