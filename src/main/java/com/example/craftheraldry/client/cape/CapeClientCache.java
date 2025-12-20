package com.example.craftheraldry.client.cape;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.util.CrestData;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of which players have the cosmetic crest cape equipped.
 *
 * Also builds a vanilla-compatible 64x32 cape texture per-player so we can render using
 * the real PlayerModel cloak part (i.e., the same geometry/UVs as vanilla capes).
 */
public final class CapeClientCache {

    /**
     * Icon sheets used by CraftHeraldry crests.
     * (Assumed 2048x4096 with 64x64 icons.)
     */
    private static final ResourceLocation SHEET0 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    private static final class Entry {
        CrestData crest;
        ResourceLocation texId;
        DynamicTexture dyn;
    }

    private static final Map<UUID, Entry> CAPES = new ConcurrentHashMap<>();

    private CapeClientCache() {}

    /** Returns the crest for a given player, or null. */
    public static CrestData get(UUID playerId) {
        Entry e = CAPES.get(playerId);
        return e == null ? null : e.crest;
    }

    /** Returns the dynamic cape texture id for a given player, or null. */
    public static ResourceLocation getCapeTexture(UUID playerId) {
        Entry e = CAPES.get(playerId);
        return e == null ? null : e.texId;
    }

    /**
     * Apply a server sync message.
     *
     * When enabled/changed, we generate and register a 64x32 cape texture.
     */
    public static void applySync(UUID playerId, boolean hasCape, CrestData crest) {
        Minecraft mc = Minecraft.getInstance();
        TextureManager tm = mc.getTextureManager();

        if (!hasCape || crest == null || crest.icon() < 0) {
            Entry old = CAPES.remove(playerId);
            if (old != null && old.texId != null) {
                tm.release(old.texId);
            }
            return;
        }

        Entry e = CAPES.computeIfAbsent(playerId, id -> new Entry());

        // No change? keep existing texture.
        if (crest.equals(e.crest) && e.texId != null) {
            return;
        }

        // Replace old texture.
        if (e.texId != null) {
            tm.release(e.texId);
            e.texId = null;
            e.dyn = null;
        }

        e.crest = crest;

        NativeImage img = buildCapeImage(crest);
        e.dyn = new DynamicTexture(img);
        e.texId = new ResourceLocation(CraftHeraldry.MODID, "dynamic_capes/" + playerId);
        tm.register(e.texId, e.dyn);
    }

    public static void clearAll() {
        Minecraft mc = Minecraft.getInstance();
        TextureManager tm = mc.getTextureManager();
        for (Entry e : CAPES.values()) {
            if (e != null && e.texId != null) tm.release(e.texId);
        }
        CAPES.clear();
    }

    // ---------------- texture build ----------------

    /**
     * Builds a vanilla-compatible cape image (64x32).
     *
     * The vanilla cape UVs sample multiple parts of the 64x32 layout; to make the cape look
     * consistently "solid", we simply fill the whole texture with the crest image pattern.
     */
    private static NativeImage buildCapeImage(CrestData crest) {
        final int W = 64;
        final int H = 32;
        NativeImage out = new NativeImage(W, H, true);

        // Transparent base.
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                out.setPixelRGBA(x, y, 0x00000000);
            }
        }

        NativeImage sheet0 = loadPng(SHEET0);
        NativeImage sheet1 = loadPng(SHEET1);
        if (sheet0 == null || sheet1 == null) {
            // Fallback: solid white if resources fail.
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) out.setPixelRGBA(x, y, 0xFFFFFFFF);
            }
            if (sheet0 != null) sheet0.close();
            if (sheet1 != null) sheet1.close();
            return out;
        }

        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        int sx = col * 64;
        int sy = row * 64;

        // Colors are RGB in CrestData. NativeImage expects ABGR in setPixelRGBA.
        int c1 = 0xFF000000 | (crest.color1() & 0x00FFFFFF);
        int c2 = 0xFF000000 | (crest.color2() & 0x00FFFFFF);

        // Fill the cape with a base color so the cloth still renders when we only place a
        // small crest patch on the back face.
        for (int y = 0; y < H; y++) {

            for (int x = 0; x < W; x++) out.setPixelRGBA(x, y, c1);
        }

        // Place the crest on the *front* face of the vanilla Mojang cape (the inner panel).
        // Vanilla cloak model is a 10x16x1 box with texOffs(0,0). The two large faces are:
        // - North face ("front" / inner):  U=1..10,  V=1..16  (10x16)
        // - South face ("back"  / outer):  U=12..21, V=1..16  (10x16)
        // To keep the icon crisp and undistorted, we fit a square crest inside 10x16 and
        // downscale with nearest-neighbor.
        final int FACE_U0 = 1;
        final int FACE_V0 = 1;
        final int FACE_W  = 10;
        final int FACE_H  = 16;

        final int ICON_SIZE = 64;
        final int crestSize = Math.min(FACE_W, FACE_H); // 10
        // Nearest-neighbor sample from the 64x64 icon into a crestSize x crestSize square.
        int crestX0 = FACE_U0 + (FACE_W - crestSize) / 2;
        int crestY0 = FACE_V0 + (FACE_H - crestSize) / 2;

        for (int y = 0; y < crestSize; y++) {
            int iy = (y * ICON_SIZE) / crestSize;
            for (int x = 0; x < crestSize; x++) {
                int ix = (x * ICON_SIZE) / crestSize;

                int p0 = sheet0.getPixelRGBA(sx + ix, sy + iy);
                int p1 = sheet1.getPixelRGBA(sx + ix, sy + iy);

                int dstX = crestX0 + x;
                int dstY = crestY0 + y;

                int outPx = out.getPixelRGBA(dstX, dstY);
                outPx = blendTint(outPx, p0, c1);
                outPx = blendTint(outPx, p1, c2);
                out.setPixelRGBA(dstX, dstY, outPx);
            }
        }
        sheet0.close();
        sheet1.close();
        return out;
    }

    /** Loads a PNG from the resource manager into a NativeImage, or null on failure. */
    private static NativeImage loadPng(ResourceLocation loc) {
        try {
            InputStream in = Minecraft.getInstance().getResourceManager().open(loc);
            try (in) {
                return NativeImage.read(in);
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Alpha-blends a tinted source pixel over an existing destination pixel.
     *
     * NativeImage pixel format is ABGR.
     */
    private static int blendTint(int dstABGR, int srcABGR, int tintARGB) {
        int sa = (srcABGR >>> 24) & 0xFF;
        if (sa == 0) return dstABGR;

        // Convert src ABGR to RGBA components.
        int sb = (srcABGR >>> 16) & 0xFF;
        int sg = (srcABGR >>> 8) & 0xFF;
        int sr = (srcABGR) & 0xFF;

        int tr = (tintARGB >>> 16) & 0xFF;
        int tg = (tintARGB >>> 8) & 0xFF;
        int tb = (tintARGB) & 0xFF;

        // Treat the icon sheets as masks; use source alpha for coverage and tint for color.
        int srcR = tr;
        int srcG = tg;
        int srcB = tb;

        // dst ABGR to RGB/A
        int da = (dstABGR >>> 24) & 0xFF;
        int db = (dstABGR >>> 16) & 0xFF;
        int dg = (dstABGR >>> 8) & 0xFF;
        int dr = (dstABGR) & 0xFF;

        float a = sa / 255f;
        int outA = Math.min(255, (int) (da + sa * (1f - da / 255f)));
        int outR = (int) (dr * (1f - a) + srcR * a);
        int outG = (int) (dg * (1f - a) + srcG * a);
        int outB = (int) (db * (1f - a) + srcB * a);

        // Back to ABGR
        return (outA << 24) | (outB << 16) | (outG << 8) | (outR);
    }
}
