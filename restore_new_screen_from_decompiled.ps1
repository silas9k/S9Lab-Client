param(
    [string]$Decompiled = ".\S9LabClientScreen_decompiled.java",
    [string]$Target = ".\src\client\java\site\s9lab\s9labclient\client\ui\S9LabClientScreen.java"
)

$ErrorActionPreference = "Stop"

Write-Host "S9LabClientScreen restore from decompiled .class source" -ForegroundColor Cyan
Write-Host ""

if (!(Test-Path $Decompiled)) {
    Write-Host "ERROR: Decompiled file not found: $Decompiled" -ForegroundColor Red
    Write-Host ""
    Write-Host "Do this:"
    Write-Host "1) In IntelliJ open your built jar/class."
    Write-Host "2) Open S9LabClientScreen.class."
    Write-Host "3) Copy the decompiled source into this file:"
    Write-Host "   $Decompiled"
    Write-Host "4) Run this script again."
    exit 1
}

if (!(Test-Path (Split-Path $Target -Parent))) {
    Write-Host "ERROR: Target folder not found: $(Split-Path $Target -Parent)" -ForegroundColor Red
    Write-Host "Run this script from your project root."
    exit 1
}

if (Test-Path $Target) {
    $backup = "$Target.before-restore.bak"
    Copy-Item $Target $backup -Force
    Write-Host "Backup created: $backup" -ForegroundColor Green
}

$content = Get-Content $Decompiled -Raw

# Remove IntelliJ/FernFlower header.
$content = $content.Replace("// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).", "")

# Replace obfuscated/intermediary class names with Yarn named classes.
$replacements = [ordered]@{
    "class_10799" = "RenderPipelines"
    "class_11905" = "CharInput"
    "class_11908" = "KeyInput"
    "class_11909" = "Click"
    "class_2561"  = "Text"
    "class_2960"  = "Identifier"
    "class_310"   = "MinecraftClient"
    "class_327"   = "TextRenderer"
    "class_332"   = "DrawContext"
    "class_3532"  = "MathHelper"
    "class_437"   = "Screen"
    "class_5250"  = "MutableText"
}

foreach ($k in $replacements.Keys) {
    $content = $content.Replace($k, $replacements[$k])
}

# Yarn method/name replacements from Minecraft 1.21.11 GUI classes.
$methodReplacements = [ordered]@{
    "Identifier.method_60655" = "Identifier.of"
    "Text.method_43470" = "Text.literal"
    "RenderPipelines.field_56883" = "RenderPipelines.GUI_TEXTURED"

    ".method_25302(" = ".drawTexture("
    ".method_25290(" = ".drawTexture("
    ".method_27535(" = ".drawTextWithShadow("
    ".method_27534(" = ".drawCenteredTextWithShadow("
    ".method_25294(" = ".fill("
    ".method_44379(" = ".enableScissor("
    ".method_44380()" = ".disableScissor()"

    "MinecraftClient.method_1551()" = "MinecraftClient.getInstance()"
    ".method_1507(" = ".setScreen("
    ".method_1548()" = ".getSession()"
    ".method_1676()" = ".getUsername()"
    ".method_1478()" = ".getResourceManager()"
    ".method_14486(" = ".getResource("

    ".method_1727(" = ".getWidth("

    ".comp_4798()" = ".x()"
    ".comp_4799()" = ".y()"

    ".method_74227()" = ".isValidChar()"
    ".method_74226()" = ".asString()"
    ".method_74231()" = ".isEscape()"
    ".method_74228()" = ".getKeycode()"
}

foreach ($k in $methodReplacements.Keys) {
    $content = $content.Replace($k, $methodReplacements[$k])
}

# Screen override method names.
$content = $content -replace "protected void method_25426\(\)", "protected void init()"
$content = $content -replace "public boolean method_25421\(\)", "public boolean shouldPause()"
$content = $content -replace "public void method_25394\(DrawContext context, int mouseX, int mouseY, float deltaTicks\)", "public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks)"
$content = $content -replace "public boolean method_25402\(Click click, boolean doubled\)", "public boolean mouseClicked(Click click, boolean doubled)"
$content = $content -replace "public boolean method_25403\(Click click, double offsetX, double offsetY\)", "public boolean mouseDragged(Click click, double offsetX, double offsetY)"
$content = $content -replace "public boolean method_25406\(Click click\)", "public boolean mouseReleased(Click click)"
$content = $content -replace "public boolean method_25401\(double mouseX, double mouseY, double horizontalAmount, double verticalAmount\)", "public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)"
$content = $content -replace "public boolean method_25400\(CharInput input\)", "public boolean charTyped(CharInput input)"
$content = $content -replace "public boolean method_25404\(KeyInput input\)", "public boolean keyPressed(KeyInput input)"
$content = $content -replace "public void method_25419\(\)", "public void close()"

# Super method names.
$content = $content.Replace("super.method_25394(context, mouseX, mouseY, deltaTicks)", "super.render(context, mouseX, mouseY, deltaTicks)")
$content = $content.Replace("super.method_25402(click, doubled)", "super.mouseClicked(click, doubled)")
$content = $content.Replace("super.method_25403(click, offsetX, offsetY)", "super.mouseDragged(click, offsetX, offsetY)")
$content = $content.Replace("super.method_25406(click)", "super.mouseReleased(click)")
$content = $content.Replace("super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount)", "super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)")
$content = $content.Replace("super.method_25400(input)", "super.charTyped(input)")
$content = $content.Replace("super.method_25404(input)", "super.keyPressed(input)")

# Screen fields.
$content = $content.Replace("this.field_22793", "this.textRenderer")
$content = $content.Replace("field_22793", "textRenderer")
$content = $content.Replace("this.field_22789", "this.width")
$content = $content.Replace("this.field_22790", "this.height")
$content = $content.Replace("field_22789", "width")
$content = $content.Replace("field_22790", "height")
$content = $content.Replace(".field_1724", ".player")

# DrawContext size getters from intermediary decompile -> Screen width/height in this instance method.
$content = $content.Replace("context.method_51421()", "this.width")
$content = $content.Replace("context.method_51443()", "this.height")

# MathHelper mappings/fixes.
$content = $content -replace "MathHelper\.method_15362\(\(double\)([^)]+)\)", "MathHelper.cos(`$1)"
$content = $content -replace "MathHelper\.method_15374\(\(double\)([^)]+)\)", "MathHelper.sin(`$1)"
$content = $content.Replace("MathHelper.method_15362", "MathHelper.cos")
$content = $content.Replace("MathHelper.method_15374", "MathHelper.sin")
$content = $content.Replace("MathHelper.method_15363", "MathHelper.clamp")

# FernFlower artifacts and invalid Java fixes observed in your decompiled file.
$content = $content.Replace(
    "private static final Map<Identifier, Boolean> MODULE_ICON_CACHE = new HashMap();",
    "private static final Map<Identifier, Boolean> MODULE_ICON_CACHE = new HashMap<>();"
)

$content = $content.Replace(
    "this.selectedCosmetic = (Cosmetic)S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse((Object)null);",
    "this.selectedCosmetic = S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse(null);"
)

$content = $content.Replace(
    "cosmeticId = type == CosmeticType.EMOTE ? `"s9lab_emote_`" + (String)modeSetting.getValue() : (String)modeSetting.getValue();",
    "String cosmeticId = type == CosmeticType.EMOTE ? `"s9lab_emote_`" + (String)modeSetting.getValue() : (String)modeSetting.getValue();"
)

$content = $content.Replace(
    "int var10002 = hsb[0]++;",
    "hsb[0] += 1.0F;"
)

# Convert common numeric keycodes to GLFW constants. Optional but cleaner.
$content = $content.Replace("input.getKeycode() == 259", "input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE")
$content = $content.Replace("input.getKeycode() == 257", "input.getKeycode() == GLFW.GLFW_KEY_ENTER")
$content = $content.Replace("input.getKeycode() == 263", "input.getKeycode() == GLFW.GLFW_KEY_LEFT")
$content = $content.Replace("input.getKeycode() == 262", "input.getKeycode() == GLFW.GLFW_KEY_RIGHT")
$content = $content.Replace("input.getKeycode() == 265", "input.getKeycode() == GLFW.GLFW_KEY_UP")
$content = $content.Replace("input.getKeycode() == 264", "input.getKeycode() == GLFW.GLFW_KEY_DOWN")

# Rebuild imports cleanly. This avoids leftover import net.minecraft.class_... lines.
$packageMatch = [regex]::Match($content, "^\s*package\s+site\.s9lab\.s9labclient\.client\.ui;\s*", [System.Text.RegularExpressions.RegexOptions]::Singleline)
if (!$packageMatch.Success) {
    Write-Host "ERROR: Could not find expected package line." -ForegroundColor Red
    exit 1
}

# Remove all import blocks after package.
$body = $content.Substring($packageMatch.Length)
$body = [regex]::Replace($body, "^\s*(import\s+[^;]+;\s*)+", "", [System.Text.RegularExpressions.RegexOptions]::Singleline)

$imports = @"
import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticIdAliases;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext.BaseVisibility;
import site.s9lab.s9labclient.client.cosmetics.preview.CosmeticPreviewRenderer;
import site.s9lab.s9labclient.client.cosmetics.preview.PreviewPose;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.impl.utility.TablistBadgeModule;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ColorSetting;
import site.s9lab.s9labclient.client.module.setting.KeybindSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.notification.S9ToastManager;
import site.s9lab.s9labclient.client.performance.ModDetectionManager;
import site.s9lab.s9labclient.client.performance.PerformanceManager;
import site.s9lab.s9labclient.client.performance.PerformanceManager.PerformancePreset;
import site.s9lab.s9labclient.client.ui.coin.CoinPack;
import site.s9lab.s9labclient.client.ui.coin.CoinPackCard;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;
import site.s9lab.s9labclient.client.util.S9TextEffects;

"@

$result = "package site.s9lab.s9labclient.client.ui;" + "`r`n`r`n" + $imports + $body.TrimStart()

# Last safety warnings.
$leftover = @()
$patterns = @("class_\d+", "method_\d+", "field_\d+", "comp_\d+")
foreach ($pattern in $patterns) {
    $matches = [regex]::Matches($result, $pattern)
    if ($matches.Count -gt 0) {
        $leftover += "$pattern = $($matches.Count)"
    }
}

Set-Content $Target $result -Encoding UTF8 -NoNewline
Write-Host "Wrote restored source: $Target" -ForegroundColor Green

if ($leftover.Count -gt 0) {
    Write-Host ""
    Write-Host "WARNING: Some obfuscated names are still left:" -ForegroundColor Yellow
    foreach ($l in $leftover) {
        Write-Host "  $l" -ForegroundColor Yellow
    }
    Write-Host "Open the file and search for class_/method_/field_/comp_ if compile fails."
}

Write-Host ""
Write-Host "Now run:" -ForegroundColor Cyan
Write-Host "  .\gradlew.bat compileClientJava"
Write-Host ""
Write-Host "If there are errors, send the complete compile output."
