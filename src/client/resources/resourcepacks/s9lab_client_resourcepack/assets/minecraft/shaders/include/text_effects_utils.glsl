#moj_import <minecraft:common.glsl>
#moj_import <minecraft:offset.glsl>
#moj_import <minecraft:defaults.glsl>
#moj_import <minecraft:rainbow.glsl>
#moj_import <minecraft:wavy.glsl>
#moj_import <minecraft:bouncy.glsl>
#moj_import <minecraft:blinking.glsl>
#moj_import <minecraft:pulse.glsl>
#moj_import <minecraft:spin.glsl>
#moj_import <minecraft:shake.glsl>
#moj_import <minecraft:fade.glsl>
#moj_import <minecraft:iterating.glsl>
#moj_import <minecraft:glitch.glsl>
#moj_import <minecraft:gradient.glsl>
#moj_import <minecraft:scale.glsl>
#moj_import <minecraft:text_effects_api.glsl>
#moj_import <minecraft:apply_effect.glsl>

// ============================================================
// TEXT EFFECTS - Config-based System
// ============================================================
// Edit _config.glsl to customize color-effect mappings
// ============================================================

// Helper function to check shadow match and update state
bool checkAndSetShadow(ivec3 c, int R, int G, int B) {
    // Check main color (exact match)
    if (c.r == R && c.g == G && c.b == B) {
        return true;
    }
    // Check shadow color (exact match, approx 25% of main)
    if (c.r == int(R/4) && c.g == int(G/4) && c.b == int(B/4)) {
        currentIsShadow = true;
        return currentIsShadow;
    }
    return false;
}

// TEXT_EFFECT macro: matches RGB color only (exact match)
#define TEXT_EFFECT(R, G, B) \
    if (c.r == R && c.g == G && c.b == B)

// TEXT_EFFECT_WITH_SHADOW macro: matches RGB color AND its shadow (exact match)
#define TEXT_EFFECT_WITH_SHADOW(R, G, B) \
    if (checkAndSetShadow(c, R, G, B))


// ============================================================
// S9LAB COMPACT MULTI-EFFECT MARKER
// Red channel 253 marks an encoded S9C+ name. Green/blue contain
// three base-18 effect IDs, allowing any 1-3 effects without
// generating hundreds of shader blocks.
// ============================================================
void s9ApplyEffectId(int id) {
    if (id == 1) apply_shake();
    else if (id == 2) apply_wavy();
    else if (id == 3) apply_rainbow();
    else if (id == 4) apply_bouncy();
    else if (id == 5) apply_blinking();
    else if (id == 6) apply_pulse();
    else if (id == 7) apply_spin();
    else if (id == 8) apply_sequential_spin();
    else if (id == 9) apply_fade();
    else if (id == 10) apply_iterating();
    else if (id == 11) apply_glitch();
    else if (id == 12) apply_scale(1.5);
    else if (id == 13) apply_offset(0.0, 40.0);
    else if (id == 14) apply_gradient(rgb(0.0, 200.0, 0.0), rgb(255.0, 255.0, 0.0), 4.0);
    else if (id == 15) apply_dynamic_gradient(rgb(255.0, 0.0, 0.0), rgb(0.0, 0.0, 255.0), 2.0, 500.0);
    else if (id == 16) apply_dynamic_gradient(rgb(0.0, 200.0, 0.0), rgb(255.0, 255.0, 0.0), 4.0, 500.0);
    else if (id == 17) apply_lava();
}

bool s9ApplyEncodedEffects(ivec3 c) {
    if (c.r != 253) return false;

    int code = c.g * 256 + c.b;
    int first = code % 18;
    code = code / 18;
    int second = code % 18;
    code = code / 18;
    int third = code % 18;

    s9ApplyEffectId(first);
    s9ApplyEffectId(second);
    s9ApplyEffectId(third);
    return hasAnyEffect();
}

void applyTextEffects() {
    vec4 vertex = vec4(Position, 1.0);
    ivec3 c = ivec3(Color.rgb * 255.0 + 0.5);

    // Initialize global state
    currentVertex = vertex;
    currentBaseColor = Color;
    currentIsShadow = false;
    currentApplyToShadow = false;

    // Compact S9C+ marker path. This is intentionally evaluated before
    // the pack's normal fixed color mappings.
    if (s9ApplyEncodedEffects(c)) {
        applyEffect(currentVertex, currentBaseColor, false);
        return;
    }

    // ============================================
    // Config-based color-effect mappings
    // ============================================
    #moj_import <minecraft:_config.glsl>

    // If any effect was applied, execute it
    if (hasAnyEffect()) {
        applyEffect(currentVertex, currentBaseColor, currentIsShadow);
        return;
    }

    // === No effect matched, render normally ===
    applyProjection(vertex);
    applyColorTexture();
    finalize();
}
