package com.enviouse.progressivestagesported.common.util;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

/**
 * Constants used throughout the mod
 */
public final class Constants {
    public static final String MOD_ID = "progressivestagesported";
    public static final String MOD_NAME = "progressivestagesported";

    /**
     * Runtime version read from the mod container (injected from gradle.properties mod_version
     * into neoforge.mods.toml at build time). Never needs manual updating.
     */
    public static String MOD_VERSION = ModList.get()
        .getModContainerById(MOD_ID)
        .map(mc -> mc.getModInfo().getVersion().toString())
        .orElse("unknown");

    // Network packet IDs
    public static final Identifier STAGE_SYNC_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "stage_sync");
    public static final Identifier STAGE_UPDATE_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "stage_update");
    public static final Identifier LOCK_REGISTRY_SYNC_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "lock_registry_sync");
    public static final Identifier LOCK_SYNC_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "lock_sync");
    public static final Identifier STAGE_DEFINITIONS_SYNC_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "stage_definitions_sync");
    public static final Identifier CREATIVE_BYPASS_PACKET = Identifier.fromNamespaceAndPath(MOD_ID, "creative_bypass");

    // Stage file directory name (inside config folder)
    public static final String STAGE_FILES_DIRECTORY = "progressivestagesported";

    // Attachment type names
    public static final Identifier TEAM_STAGE_ATTACHMENT = Identifier.fromNamespaceAndPath(MOD_ID, "team_stages");

    private Constants() {
        // Prevent instantiation
    }
}
