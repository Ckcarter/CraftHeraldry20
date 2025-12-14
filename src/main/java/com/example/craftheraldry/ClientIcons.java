package com.example.craftheraldry;

import com.example.craftheraldry.client.HeraldryRender;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads icon names from the original CraftHeraldry asset file:
 *   assets/craftheraldry/textures/icons/iconNames.txt
 */
public final class ClientIcons {
    public static final List<String> ICON_NAMES = new ArrayList<>();
    public static boolean LOADED = false;

    private ClientIcons() {}

    @OnlyIn(Dist.CLIENT)
    public static void loadIfNeeded() {
        if (LOADED) return;
        LOADED = true;

        try {
            ResourceLocation loc = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/iconNames.txt");
            Resource res = Minecraft.getInstance().getResourceManager().getResource(loc).orElse(null);
            if (res == null) return;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(res.open(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    ICON_NAMES.add(line);
                }
            }
        } catch (Exception ignored) {
            // If loading fails, the UI will still work (names just missing)
        }
    }

    public static int iconCount() {
        return Math.max(1, ICON_NAMES.size());
    }

    public static String nameOrFallback(int idx) {
        if (idx >= 0 && idx < ICON_NAMES.size()) return ICON_NAMES.get(idx);
        return "Icon " + (idx + 1);
    }
}
