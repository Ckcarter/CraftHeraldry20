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

public final class CapeClientCache {

    private static final float CREST_STRETCH_X = 1.0f;
    private static final float CREST_STRETCH_Y = 1.0f;

    private static final ResourceLocation SHEET0 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    private static final class Entry {
        CrestData crest;
        ResourceLocation texId;
        DynamicTexture dyn;
    }

    private static final Map<UUID, Entry> CAPES = new ConcurrentHashMap<>();

    private CapeClientCache() {}

    public static CrestData get(UUID playerId) {
        Entry e = CAPES.get(playerId);
        return e == null ? null : e.crest;
    }

    public static ResourceLocation getCapeTexture(UUID playerId) {
        Entry e = CAPES.get(playerId);
        return e == null ? null : e.texId;
    }

    public static void applySync(UUID playerId, boolean hasCape, CrestData crest) {
        Minecraft mc = Minecraft.getInstance();
        TextureManager tm = mc.getTextureManager();

        if (!hasCape || crest == null || crest.icon() < 0) {
            Entry old = CAPES.remove(playerId);
            if (old != null && old.texId != null) tm.release(old.texId);
            return;
        }

        Entry e = CAPES.computeIfAbsent(playerId, id -> new Entry());

        if (crest.equals(e.crest) && e.texId != null) return;

        if (e.texId != null) {
            tm.release(e.texId);
            e.texId = null;
            e.dyn = null;
        }

        e.crest = crest;

        NativeImage img = buildCapeImage(crest);
        e.dyn = new DynamicTexture(img);
        e.dyn.setFilter(false, false);
        e.texId = new ResourceLocation(CraftHeraldry.MODID, "dynamic_capes/" + playerId);
        tm.register(e.texId, e.dyn);
    }

    private static NativeImage buildCapeImage(CrestData crest) {
        final int S = 8;
        final int W = 64 * S;
        final int H = 32 * S;

        NativeImage out = new NativeImage(W, H, true);

        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                out.setPixelRGBA(x, y, 0x00000000);

        NativeImage sheet0 = loadPng(SHEET0);
        NativeImage sheet1 = loadPng(SHEET1);
        // If resources failed to load, return a transparent cape to avoid client crashes.
        if (sheet0 == null || sheet1 == null) {
            if (sheet0 != null) sheet0.close();
            if (sheet1 != null) sheet1.close();
            return out;
        }

        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        int sx = col * 64;
        int sy = row * 64;

        int c1 = rgbToAbgr(crest.color1());
        int c2 = rgbToAbgr(crest.color2());

        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                out.setPixelRGBA(x, y, c1);

        final int FACE_W = 10 * S;
        final int FACE_H = 16 * S;
        final int FACE_V0 = 1 * S;

        final int FRONT_U0 = 1 * S;
        final int BACK_U0  = 12 * S;

        blitCrest(sheet0, sheet1, out, sx, sy, c1, c2,
                FRONT_U0, FACE_V0, FACE_W, FACE_H);

        blitCrest(sheet0, sheet1, out, sx, sy, c1, c2,
                BACK_U0, FACE_V0, FACE_W, FACE_H);

        sheet0.close();
        sheet1.close();
        return out;
    }

    private static void blitCrest(NativeImage sheet0, NativeImage sheet1, NativeImage dst,
                                  int srcX0, int srcY0,
                                  int tint1ABGR, int tint2ABGR,
                                  int dstX0, int dstY0, int dstW, int dstH) {

        final int ICON = 64;
        // IMPORTANT:
        // We intentionally draw into the full destination rectangle (dstW x dstH) instead of
        // forcing a square. This lets the crest "canvas" stretch up/down independently from
        // left/right while staying pixel-crisp (nearest-neighbor sampling).
        int drawW = dstW;
        int drawH = dstH;

        float c = (ICON - 1) / 2f;

        for (int y = 0; y < drawH; y++) {
            int by = (y * ICON) / drawH;
            int iy = Math.round(c + ((by - c) / CREST_STRETCH_Y));
            iy = Math.max(0, Math.min(ICON - 1, iy));

            for (int x = 0; x < drawW; x++) {
                int bx = (x * ICON) / drawW;
                int ix = Math.round(c + ((bx - c) / CREST_STRETCH_X));
                ix = Math.max(0, Math.min(ICON - 1, ix));

                int m0 = sheet0.getPixelRGBA(srcX0 + ix, srcY0 + iy);
                int m1 = sheet1.getPixelRGBA(srcX0 + ix, srcY0 + iy);

                int dx = dstX0 + x;
                int dy = dstY0 + y;

                int base = dst.getPixelRGBA(dx, dy);
                int px = blendMultiplyTint(base, m0, tint1ABGR);
                px = blendMultiplyTint(px, m1, tint2ABGR);
                dst.setPixelRGBA(dx, dy, px);
            }
        }
    }

    private static int rgbToAbgr(int rgb) {
        int r = (rgb >>> 16) & 0xFF;
        int g = (rgb >>> 8) & 0xFF;
        int b = rgb & 0xFF;
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    private static NativeImage loadPng(ResourceLocation loc) {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(loc)) {
            return NativeImage.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    private static int blendMultiplyTint(int dstABGR, int maskABGR, int tintABGR) {
        int sa = (maskABGR >>> 24) & 0xFF;
        if (sa == 0) return dstABGR;

        int mb = (maskABGR >>> 16) & 0xFF;
        int mg = (maskABGR >>> 8) & 0xFF;
        int mr = maskABGR & 0xFF;

        int tb = (tintABGR >>> 16) & 0xFF;
        int tg = (tintABGR >>> 8) & 0xFF;
        int tr = tintABGR & 0xFF;

        int srcB = (mb * tb) / 255;
        int srcG = (mg * tg) / 255;
        int srcR = (mr * tr) / 255;

        int da = (dstABGR >>> 24) & 0xFF;
        int db = (dstABGR >>> 16) & 0xFF;
        int dg = (dstABGR >>> 8) & 0xFF;
        int dr = dstABGR & 0xFF;

        float a = sa / 255f;
        float ia = 1f - a;

        int outA = (int)(sa + da * ia);
        int outR = (int)(srcR * a + dr * ia);
        int outG = (int)(srcG * a + dg * ia);
        int outB = (int)(srcB * a + db * ia);

        return (outA << 24) | (outB << 16) | (outG << 8) | outR;
    }
}
