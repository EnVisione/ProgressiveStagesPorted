package com.enviouse.progressivestagesported.common.api;

import com.enviouse.progressivestagesported.common.util.Constants;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Wrapper for stage identifiers using Identifier.
 *
 * <p>Format: namespace:path (e.g., progressivestages:tech/iron_age)
 *
 * <h3>Normalization Rules (Aligned with Minecraft Identifier)</h3>
 * <ul>
 *   <li>Whitespace is trimmed from both ends</li>
 *   <li>All characters are converted to lowercase</li>
 *   <li>Namespace allows: a-z, 0-9, underscore, hyphen, period</li>
 *   <li>Path allows: a-z, 0-9, underscore, hyphen, period, forward slash (/)</li>
 *   <li>Namespace defaults to "progressivestagesported" if not specified</li>
 * </ul>
 *
 * <h3>Examples</h3>
 * <ul>
 *   <li>"Diamond_Age" → "progressivestages:diamond_age"</li>
 *   <li>"  iron_age  " → "progressivestages:iron_age"</li>
 *   <li>"mymod:my_stage" → "mymod:my_stage"</li>
 *   <li>"tech/iron_age" → "progressivestages:tech/iron_age" (hierarchical)</li>
 *   <li>"progressivestages:stone_age" → "progressivestages:stone_age"</li>
 * </ul>
 */
public final class StageId implements Comparable<StageId> {

    /**
     * Allowed characters in namespace: a-z, 0-9, underscore, hyphen, period
     * (matches Minecraft Identifier namespace rules)
     */
    private static final Pattern VALID_NAMESPACE = Pattern.compile("^[a-z0-9_.-]+$");

    /**
     * Allowed characters in path: a-z, 0-9, underscore, hyphen, period, forward slash
     * (matches Minecraft Identifier path rules)
     */
    private static final Pattern VALID_PATH = Pattern.compile("^[a-z0-9_./-]+$");

    private final Identifier id;

    public StageId(Identifier id) {
        this.id = Objects.requireNonNull(id, "Stage ID cannot be null");
    }

    public StageId(String namespace, String path) {
        this(Identifier.fromNamespaceAndPath(
            normalizeComponent(namespace),
            normalizeComponent(path)
        ));
    }

    public StageId(String id) {
        this(parseId(id));
    }

    /**
     * Parse and normalize a stage ID string.
     *
     * @param id The raw ID string (may include namespace)
     * @return Normalized Identifier
     * @throws IllegalArgumentException if ID contains invalid characters
     */
    private static Identifier parseId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Stage ID cannot be null or empty");
        }

        // Trim whitespace and convert to lowercase
        id = id.trim().toLowerCase();

        if (id.contains(":")) {
            String[] parts = id.split(":", 2);
            String namespace = normalizeComponent(parts[0]);
            String path = normalizeComponent(parts[1]);
            validateNamespace(namespace);
            validatePath(path);
            return Identifier.fromNamespaceAndPath(namespace, path);
        } else {
            String path = normalizeComponent(id);
            validatePath(path);
            // Default to our mod namespace if none specified
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
        }
    }

    /**
     * Normalize a component (namespace or path) of the stage ID.
     */
    private static String normalizeComponent(String component) {
        if (component == null) return "";
        return component.trim().toLowerCase();
    }

    /**
     * Validate a namespace component.
     */
    private static void validateNamespace(String namespace) {
        if (namespace.isEmpty()) {
            throw new IllegalArgumentException("Stage ID namespace cannot be empty");
        }
        if (!VALID_NAMESPACE.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                "Stage ID namespace contains invalid characters: '" + namespace + "'. " +
                "Only lowercase letters, numbers, underscores, hyphens, and periods are allowed."
            );
        }
    }

    /**
     * Validate a path component.
     */
    private static void validatePath(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Stage ID path cannot be empty");
        }
        if (!VALID_PATH.matcher(path).matches()) {
            throw new IllegalArgumentException(
                "Stage ID path contains invalid characters: '" + path + "'. " +
                "Only lowercase letters, numbers, underscores, hyphens, periods, and forward slashes are allowed."
            );
        }
        // Additional validation: no double slashes, no leading/trailing slashes
        if (path.contains("//")) {
            throw new IllegalArgumentException(
                "Stage ID path cannot contain double slashes: '" + path + "'"
            );
        }
        if (path.startsWith("/") || path.endsWith("/")) {
            throw new IllegalArgumentException(
                "Stage ID path cannot start or end with a slash: '" + path + "'"
            );
        }
    }

    /**
     * Create a StageId with the progressivestages namespace.
     * The name will be normalized (trimmed, lowercased).
     */
    public static StageId of(String name) {
        return new StageId(Constants.MOD_ID, normalizeComponent(name));
    }

    /**
     * Create a StageId from a full Identifier string.
     * The ID will be normalized (trimmed, lowercased).
     */
    public static StageId parse(String id) {
        return new StageId(id);
    }

    /**
     * Try to parse a stage ID, returning null if invalid.
     * Use this for user input that may be malformed.
     */
    public static StageId tryParse(String id) {
        try {
            return parse(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Create a StageId from a Identifier.
     * Note: The Identifier is used as-is without normalization.
     */
    public static StageId fromIdentifier(Identifier location) {
        return new StageId(location);
    }

    public Identifier getIdentifier() {
        return id;
    }

    public String getNamespace() {
        return id.getNamespace();
    }

    public String getPath() {
        return id.getPath();
    }

    /**
     * Check if this stage uses the default progressivestages namespace.
     */
    public boolean isDefaultNamespace() {
        return Constants.MOD_ID.equals(id.getNamespace());
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StageId stageId = (StageId) o;
        return Objects.equals(id, stageId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(StageId other) {
        return this.id.compareTo(other.id);
    }
}
