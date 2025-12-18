package com.example.craftheraldry.client.cape;

import com.example.craftheraldry.common.util.CrestData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeClientCache {
    private static final Map<UUID, CrestData> CAPES = new ConcurrentHashMap<>();

    private CapeClientCache() {}

    public static CrestData get(UUID playerId) {
        return CAPES.get(playerId);
    }

    public static void applySync(UUID playerId, boolean hasCape, CrestData crest) {
        if (!hasCape || crest == null || crest.icon() < 0) {
            CAPES.remove(playerId);
        } else {
            CAPES.put(playerId, crest);
        }
    }

    public static void clearAll() {
        CAPES.clear();
    }
}
