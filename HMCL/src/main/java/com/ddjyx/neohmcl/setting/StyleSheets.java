// SPDX-FileCopyrightText: Copyright (C) 2025 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.setting;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import com.ddjyx.neohmcl.theme.ResolvedTheme;
import com.ddjyx.neohmcl.theme.ThemeColor;
import com.ddjyx.neohmcl.theme.Themes;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

/**
 * @author Glavo
 */
public final class StyleSheets {
    private static final int FONT_STYLE_SHEET_INDEX = 0;
    private static final int THEME_STYLE_SHEET_INDEX = 1;
    private static final int BRIGHTNESS_SHEET_INDEX = 2;

    private static final ObservableList<String> stylesheets;

    private static String toStyleSheetUri(String styleSheet) {
        return "data:text/css;charset=UTF-8;base64," + Base64.getEncoder().encodeToString(styleSheet.getBytes(StandardCharsets.UTF_8));
    }

    private static String getFontStyleSheet() {
        final String defaultCss = "/assets/css/font.css";
        final FontManager.FontReference font = FontManager.getFont();

        if (font == null || "System".equals(font.family()))
            return defaultCss;

        String fontFamily = font.family();
        String style = font.style();
        String weight = null;
        String posture = null;

        if (style != null) {
            style = style.toLowerCase(Locale.ROOT);

            if (style.contains("thin"))
                weight = "100";
            else if (style.contains("extralight") || style.contains("extra light") || style.contains("ultralight") | style.contains("ultra light"))
                weight = "200";
            else if (style.contains("medium"))
                weight = "500";
            else if (style.contains("semibold") || style.contains("semi bold") || style.contains("demibold") || style.contains("demi bold"))
                weight = "600";
            else if (style.contains("extrabold") || style.contains("extra bold") || style.contains("ultrabold") || style.contains("ultra bold"))
                weight = "800";
            else if (style.contains("black") || style.contains("heavy"))
                weight = "900";
            else if (style.contains("light"))
                weight = "lighter";
            else if (style.contains("bold"))
                weight = "bold";

            posture = style.contains("italic") || style.contains("oblique") ? "italic" : null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(".root {");
        builder.append("-fx-font-family:\"").append(fontFamily).append("\";");

        if (weight != null)
            builder.append("-fx-font-weight:").append(weight).append(";");

        if (posture != null)
            builder.append("-fx-font-style:").append(posture).append(";");

        builder.append('}');

        return toStyleSheetUri(builder.toString());
    }

    private static String getBrightnessStyleSheet() {
        return Themes.getColorScheme().getBrightness() == Brightness.LIGHT
                ? "/assets/css/brightness-light.css"
                : "/assets/css/brightness-dark.css";
    }

    private static void addColor(StringBuilder builder, String name, Color color) {
        builder.append("  ").append(name)
                .append(": ").append(ThemeColor.getColorDisplayName(color)).append(";\n");
    }

    private static void addColor(StringBuilder builder, String name, Color color, double opacity) {
        builder.append("  ").append(name)
                .append(": ").append(ThemeColor.getColorDisplayNameWithOpacity(color, opacity)).append(";\n");
    }

    private static void addColor(StringBuilder builder, ColorScheme scheme, ColorRole role, double opacity) {
        builder.append("  ").append(role.getVariableName()).append("-transparent-%02d".formatted((int) (100 * opacity)))
                .append(": ").append(ThemeColor.getColorDisplayNameWithOpacity(resolveRoleColor(scheme, role), opacity))
                .append(";\n");
    }

    /// Neutral grayscale overrides for decorative color roles in light mode.
    /// Roles absent from this map keep their Monet-derived color, so only the primary theme
    /// color family and the semantic error colors stay chromatic.
    private static final @Unmodifiable Map<ColorRole, Color> LIGHT_NEUTRAL_COLORS = Map.ofEntries(
            Map.entry(ColorRole.SECONDARY, Color.web("#575757")),
            Map.entry(ColorRole.ON_SECONDARY, Color.web("#FFFFFF")),
            Map.entry(ColorRole.SECONDARY_CONTAINER, Color.web("#E0E0E0")),
            Map.entry(ColorRole.ON_SECONDARY_CONTAINER, Color.web("#3A3A3A")),
            Map.entry(ColorRole.SECONDARY_FIXED, Color.web("#E0E0E0")),
            Map.entry(ColorRole.SECONDARY_FIXED_DIM, Color.web("#C6C6C6")),
            Map.entry(ColorRole.ON_SECONDARY_FIXED, Color.web("#141414")),
            Map.entry(ColorRole.ON_SECONDARY_FIXED_VARIANT, Color.web("#3F3F3F")),
            Map.entry(ColorRole.TERTIARY, Color.web("#575757")),
            Map.entry(ColorRole.ON_TERTIARY, Color.web("#FFFFFF")),
            Map.entry(ColorRole.TERTIARY_CONTAINER, Color.web("#E0E0E0")),
            Map.entry(ColorRole.ON_TERTIARY_CONTAINER, Color.web("#3A3A3A")),
            Map.entry(ColorRole.TERTIARY_FIXED, Color.web("#E0E0E0")),
            Map.entry(ColorRole.TERTIARY_FIXED_DIM, Color.web("#C6C6C6")),
            Map.entry(ColorRole.ON_TERTIARY_FIXED, Color.web("#141414")),
            Map.entry(ColorRole.ON_TERTIARY_FIXED_VARIANT, Color.web("#3F3F3F")),
            Map.entry(ColorRole.SURFACE, Color.web("#FFFFFF")),
            Map.entry(ColorRole.ON_SURFACE, Color.web("#000000")),
            Map.entry(ColorRole.SURFACE_DIM, Color.web("#DEDEDE")),
            Map.entry(ColorRole.SURFACE_BRIGHT, Color.web("#FFFFFF")),
            Map.entry(ColorRole.SURFACE_CONTAINER_LOWEST, Color.web("#FFFFFF")),
            Map.entry(ColorRole.SURFACE_CONTAINER_LOW, Color.web("#F5F5F5")),
            Map.entry(ColorRole.SURFACE_CONTAINER, Color.web("#F0F0F0")),
            Map.entry(ColorRole.SURFACE_CONTAINER_HIGH, Color.web("#E9E9E9")),
            Map.entry(ColorRole.SURFACE_CONTAINER_HIGHEST, Color.web("#E3E3E3")),
            Map.entry(ColorRole.SURFACE_VARIANT, Color.web("#E2E2E2")),
            Map.entry(ColorRole.ON_SURFACE_VARIANT, Color.web("#454545")),
            Map.entry(ColorRole.BACKGROUND, Color.web("#FFFFFF")),
            Map.entry(ColorRole.ON_BACKGROUND, Color.web("#000000")),
            Map.entry(ColorRole.OUTLINE, Color.web("#767676")),
            Map.entry(ColorRole.OUTLINE_VARIANT, Color.web("#C6C6C6")),
            Map.entry(ColorRole.SHADOW, Color.web("#000000")),
            Map.entry(ColorRole.SCRIM, Color.web("#000000")),
            Map.entry(ColorRole.INVERSE_SURFACE, Color.web("#303030")),
            Map.entry(ColorRole.INVERSE_ON_SURFACE, Color.web("#F2F2F2"))
    );

    /// Neutral grayscale overrides for decorative color roles in dark mode.
    private static final @Unmodifiable Map<ColorRole, Color> DARK_NEUTRAL_COLORS = Map.ofEntries(
            Map.entry(ColorRole.SECONDARY, Color.web("#BBBBBB")),
            Map.entry(ColorRole.ON_SECONDARY, Color.web("#000000")),
            Map.entry(ColorRole.SECONDARY_CONTAINER, Color.web("#333333")),
            Map.entry(ColorRole.ON_SECONDARY_CONTAINER, Color.web("#E0E0E0")),
            Map.entry(ColorRole.SECONDARY_FIXED, Color.web("#E0E0E0")),
            Map.entry(ColorRole.SECONDARY_FIXED_DIM, Color.web("#C6C6C6")),
            Map.entry(ColorRole.ON_SECONDARY_FIXED, Color.web("#141414")),
            Map.entry(ColorRole.ON_SECONDARY_FIXED_VARIANT, Color.web("#3F3F3F")),
            Map.entry(ColorRole.TERTIARY, Color.web("#BBBBBB")),
            Map.entry(ColorRole.ON_TERTIARY, Color.web("#000000")),
            Map.entry(ColorRole.TERTIARY_CONTAINER, Color.web("#333333")),
            Map.entry(ColorRole.ON_TERTIARY_CONTAINER, Color.web("#E0E0E0")),
            Map.entry(ColorRole.TERTIARY_FIXED, Color.web("#E0E0E0")),
            Map.entry(ColorRole.TERTIARY_FIXED_DIM, Color.web("#C6C6C6")),
            Map.entry(ColorRole.ON_TERTIARY_FIXED, Color.web("#141414")),
            Map.entry(ColorRole.ON_TERTIARY_FIXED_VARIANT, Color.web("#3F3F3F")),
            Map.entry(ColorRole.SURFACE, Color.web("#000000")),
            Map.entry(ColorRole.ON_SURFACE, Color.web("#FFFFFF")),
            Map.entry(ColorRole.SURFACE_DIM, Color.web("#000000")),
            Map.entry(ColorRole.SURFACE_BRIGHT, Color.web("#2C2C2C")),
            Map.entry(ColorRole.SURFACE_CONTAINER_LOWEST, Color.web("#000000")),
            Map.entry(ColorRole.SURFACE_CONTAINER_LOW, Color.web("#0D0D0D")),
            Map.entry(ColorRole.SURFACE_CONTAINER, Color.web("#141414")),
            Map.entry(ColorRole.SURFACE_CONTAINER_HIGH, Color.web("#1C1C1C")),
            Map.entry(ColorRole.SURFACE_CONTAINER_HIGHEST, Color.web("#262626")),
            Map.entry(ColorRole.SURFACE_VARIANT, Color.web("#262626")),
            Map.entry(ColorRole.ON_SURFACE_VARIANT, Color.web("#BBBBBB")),
            Map.entry(ColorRole.BACKGROUND, Color.web("#000000")),
            Map.entry(ColorRole.ON_BACKGROUND, Color.web("#FFFFFF")),
            Map.entry(ColorRole.OUTLINE, Color.web("#8A8A8A")),
            Map.entry(ColorRole.OUTLINE_VARIANT, Color.web("#3A3A3A")),
            Map.entry(ColorRole.SHADOW, Color.web("#000000")),
            Map.entry(ColorRole.SCRIM, Color.web("#000000")),
            Map.entry(ColorRole.INVERSE_SURFACE, Color.web("#E0E0E0")),
            Map.entry(ColorRole.INVERSE_ON_SURFACE, Color.web("#1A1A1A"))
    );

    /// Resolves the display color for a role, replacing decorative roles with neutral grayscale
    /// so only the primary theme color family and semantic error colors remain chromatic.
    private static Color resolveRoleColor(ColorScheme scheme, ColorRole role) {
        Map<ColorRole, Color> neutrals = scheme.getBrightness() == Brightness.DARK ? DARK_NEUTRAL_COLORS : LIGHT_NEUTRAL_COLORS;
        Color neutral = neutrals.get(role);
        return neutral != null ? neutral : scheme.getColor(role);
    }

    private static String getThemeStyleSheet() {
        final String blueCss = "/assets/css/blue.css";

        if (ResolvedTheme.DEFAULT.equals(Themes.getTheme()))
            return blueCss;

        ColorScheme scheme = Themes.getColorScheme();

        StringBuilder builder = new StringBuilder();
        builder.append("* {\n");
        for (ColorRole colorRole : ColorRole.ALL) {
            addColor(builder, colorRole.getVariableName(), resolveRoleColor(scheme, colorRole));
        }

        addColor(builder, "-monet-primary-seed", scheme.getPrimaryColorSeed());

        addColor(builder, scheme, ColorRole.PRIMARY, 0.5);
        addColor(builder, scheme, ColorRole.PRIMARY, 0.8);
        addColor(builder, scheme, ColorRole.SECONDARY_CONTAINER, 0.5);
        addColor(builder, scheme, ColorRole.SURFACE, 0.5);
        addColor(builder, scheme, ColorRole.SURFACE, 0.8);
        addColor(builder, scheme, ColorRole.ON_SURFACE_VARIANT, 0.38);
        addColor(builder, scheme, ColorRole.SURFACE_CONTAINER_LOW, 0.8);
        addColor(builder, scheme, ColorRole.SECONDARY_CONTAINER, 0.8);
        addColor(builder, scheme, ColorRole.INVERSE_SURFACE, 0.8);

        builder.append("}\n");
        return toStyleSheetUri(builder.toString());
    }

    public static void init(Scene scene) {
        Bindings.bindContent(scene.getStylesheets(), stylesheets);
    }

    // Runs after the neutral color maps above are initialized, since building the theme
    // stylesheet reads them; declared last so static initialization order is correct.
    static {
        String[] array = new String[]{
                getFontStyleSheet(),
                getThemeStyleSheet(),
                getBrightnessStyleSheet(),
                "/assets/css/root.css"
        };
        stylesheets = FXCollections.observableList(Arrays.asList(array));

        FontManager.fontProperty().addListener(o -> stylesheets.set(FONT_STYLE_SHEET_INDEX, getFontStyleSheet()));
        Themes.colorSchemeProperty().addListener(o -> {
            stylesheets.set(THEME_STYLE_SHEET_INDEX, getThemeStyleSheet());
            stylesheets.set(BRIGHTNESS_SHEET_INDEX, getBrightnessStyleSheet());
        });
    }

    private StyleSheets() {
    }
}
