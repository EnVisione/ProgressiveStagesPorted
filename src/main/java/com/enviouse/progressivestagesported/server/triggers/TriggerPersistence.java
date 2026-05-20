package com.enviouse.progressivestagesported.server.triggers;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

import java.util.*;

/**
 * Persists "already triggered" state for one-time triggers (dimensions, bosses, etc.).
 *
 * <p>This prevents re-granting stages after server restart or datapack reload
 * for triggers that should only fire once per player/team.
 *
 * <p><b>Storage:</b> Anchored to the overworld's data storage, making it <b>global</b>
 * across all dimensions. Data persists in: {@code world/data/progressivestages_triggers.dat}
 *
 * <p><b>Important:</b> Always call {@link #setDirty()} after mutations to ensure persistence.
 */
public class TriggerPersistence extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Map: trigger type + trigger key -> Set of player/team UUIDs that have triggered it
    // Example: "dimension:minecraft:the_nether" -> {player1UUID, player2UUID}
    private final Map<String, Set<UUID>> triggeredMap = new HashMap<>();

    /**
     * Codec for serialization/deserialization of the triggered map.
     * Format: map of string key -> list of UUID strings
     */
    public static final Codec<TriggerPersistence> CODEC = Codec.unboundedMap(
        Codec.STRING,
        Codec.STRING.listOf()
    ).xmap(
        // Decode: Map<String, List<String>> -> TriggerPersistence
        map -> {
            TriggerPersistence persistence = new TriggerPersistence();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                Set<UUID> uuids = new HashSet<>();
                for (String uuidStr : entry.getValue()) {
                    try {
                        uuids.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("[ProgressiveStages] Invalid UUID in trigger data: {}", uuidStr);
                    }
                }
                if (!uuids.isEmpty()) {
                    persistence.triggeredMap.put(entry.getKey(), uuids);
                }
            }
            LOGGER.debug("[ProgressiveStages] Loaded {} trigger entries", persistence.triggeredMap.size());
            return persistence;
        },
        // Encode: TriggerPersistence -> Map<String, List<String>>
        persistence -> {
            Map<String, List<String>> map = new HashMap<>();
            for (Map.Entry<String, Set<UUID>> entry : persistence.triggeredMap.entrySet()) {
                List<String> uuidStrings = new ArrayList<>();
                for (UUID uuid : entry.getValue()) {
                    uuidStrings.add(uuid.toString());
                }
                map.put(entry.getKey(), uuidStrings);
            }
            return map;
        }
    );

    /**
     * SavedDataType for the new MC 26.1.2 SavedData system.
     */
    public static final SavedDataType<TriggerPersistence> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("progressivestagesported", "triggers"),
        TriggerPersistence::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    public TriggerPersistence() {
    }

    /**
     * Get or create the trigger persistence for a server.
     */
    public static TriggerPersistence get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Check if a trigger has already been activated for a player/team.
     */
    public boolean hasTriggered(String triggerType, String triggerKey, UUID targetId) {
        String key = triggerType + ":" + triggerKey;
        Set<UUID> triggered = triggeredMap.get(key);
        return triggered != null && triggered.contains(targetId);
    }

    /**
     * Mark a trigger as activated for a player/team.
     */
    public void markTriggered(String triggerType, String triggerKey, UUID targetId) {
        String key = triggerType + ":" + triggerKey;
        triggeredMap.computeIfAbsent(key, k -> new HashSet<>()).add(targetId);
        setDirty();
        LOGGER.debug("[ProgressiveStages] Marked trigger as activated: {} for {}", key, targetId);
    }

    /**
     * Clear a specific trigger for a player/team (useful for testing/admin).
     */
    public void clearTrigger(String triggerType, String triggerKey, UUID targetId) {
        String key = triggerType + ":" + triggerKey;
        Set<UUID> triggered = triggeredMap.get(key);
        if (triggered != null) {
            triggered.remove(targetId);
            if (triggered.isEmpty()) {
                triggeredMap.remove(key);
            }
            setDirty();
        }
    }

    /**
     * Clear all triggers for a player/team.
     */
    public void clearAllTriggersFor(UUID targetId) {
        boolean changed = false;
        Iterator<Map.Entry<String, Set<UUID>>> it = triggeredMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Set<UUID>> entry = it.next();
            if (entry.getValue().remove(targetId)) {
                changed = true;
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            }
        }
        if (changed) {
            setDirty();
        }
    }
}
