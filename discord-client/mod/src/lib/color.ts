// Small color helpers for the theme engine.

export interface Rgb {
    r: number;
    g: number;
    b: number;
}

/** Parses "#rrggbb", "rgb(…)", or an Android ARGB/RGB int into {r,g,b}. */
export function toRgb(value: unknown): Rgb | undefined {
    if (typeof value === "number") {
        return { r: (value >> 16) & 0xff, g: (value >> 8) & 0xff, b: value & 0xff };
    }
    if (typeof value !== "string") return undefined;
    const hex = /^#([0-9a-f]{6})/i.exec(value);
    if (hex) {
        const n = parseInt(hex[1], 16);
        return { r: (n >> 16) & 0xff, g: (n >> 8) & 0xff, b: n & 0xff };
    }
    const rgb = /rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)/i.exec(value);
    if (rgb) return { r: +rgb[1], g: +rgb[2], b: +rgb[3] };
    return undefined;
}

export const distance = (a: Rgb, b: Rgb) =>
    Math.sqrt((a.r - b.r) ** 2 + (a.g - b.g) ** 2 + (a.b - b.b) ** 2);

/** Replaces the rgb of a color while keeping its original format and alpha. */
export function withRgb(original: unknown, rgb: Rgb): unknown {
    if (typeof original === "number") {
        const a = (original >>> 24) & 0xff;
        return ((a << 24) | (rgb.r << 16) | (rgb.g << 8) | rgb.b) >>> 0;
    }
    if (typeof original === "string") {
        const rgba = /rgba?\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*(?:,\s*([\d.]+))?\s*\)/i.exec(original);
        if (rgba) return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${rgba[1] ?? "1"})`;
        const hexA = /^#[0-9a-f]{6}([0-9a-f]{2})$/i.exec(original);
        const hex = (n: number) => n.toString(16).padStart(2, "0");
        return `#${hex(rgb.r)}${hex(rgb.g)}${hex(rgb.b)}${hexA ? hexA[1] : ""}`;
    }
    return original;
}

// Shift a whole palette by the hue of a target accent: map Discord's blurple
// to the accent, keeping lightness, so hovers/pressed shades stay coherent.
export function remapFrom(original: Rgb, from: Rgb, to: Rgb): Rgb {
    const ratio = (o: number, f: number, t: number) => {
        // Keep the original's offset from its base, applied to the new base.
        const v = t + (o - f);
        return Math.max(0, Math.min(255, Math.round(v)));
    };
    return { r: ratio(original.r, from.r, to.r), g: ratio(original.g, from.g, to.g), b: ratio(original.b, from.b, to.b) };
}
