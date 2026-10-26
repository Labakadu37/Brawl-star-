// Vencord-style platform icons (monitor, phone, globe, controller), drawn at
// runtime in the status color and encoded as PNG data URIs.

import { encodePalettePng, pngBase64, pngDataUri } from "./png";

type Shape = (x: number, y: number) => boolean;

const rect =
    (x0: number, y0: number, x1: number, y1: number, r = 0): Shape =>
    (x, y) => {
        if (x < x0 || x > x1 || y < y0 || y > y1) return false;
        // Rounded corners: distance to the nearest corner circle's center.
        const cx = Math.min(Math.max(x, x0 + r), x1 - r);
        const cy = Math.min(Math.max(y, y0 + r), y1 - r);
        return (x - cx) ** 2 + (y - cy) ** 2 <= r * r;
    };
const ellipse =
    (cx: number, cy: number, rx: number, ry: number): Shape =>
    (x, y) =>
        ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1;
const circle = (cx: number, cy: number, r: number) => ellipse(cx, cy, r, r);

const any =
    (...shapes: Shape[]): Shape =>
    (x, y) =>
        shapes.some(shape => shape(x, y));
const minus =
    (shape: Shape, ...holes: Shape[]): Shape =>
    (x, y) =>
        shape(x, y) && !holes.some(hole => hole(x, y));

// Icons on a 24×24 grid.
export const ICONS: Record<string, Shape> = {
    desktop: any(minus(rect(1.5, 3, 22.5, 17, 2), rect(4, 5.5, 20, 14.5, 0.5)), rect(10.5, 17, 13.5, 20), rect(6.5, 19.5, 17.5, 22, 1)),
    mobile: minus(rect(5.5, 1.5, 18.5, 22.5, 2.5), rect(8, 4.5, 16, 17.5, 0.5), circle(12, 20, 1)),
    web: any(
        minus(circle(12, 12, 10.5), circle(12, 12, 8.5)), // outline
        minus(ellipse(12, 12, 4.5, 10.5), ellipse(12, 12, 2.5, 8.5)), // meridian
        rect(2, 11, 22, 13), // equator
    ),
    embedded: minus(rect(1.5, 6.5, 22.5, 18.5, 6), rect(5.5, 11.5, 10.5, 13.5), rect(7, 10, 9, 15), circle(15.5, 11, 1.4), circle(18, 14, 1.4)),
};

export const STATUS_COLORS: Record<string, string> = {
    online: "#23a55a",
    idle: "#f0b232",
    dnd: "#f23f43",
    offline: "#80848e",
};

const hex = (color: string) => [1, 3, 5].map(i => parseInt(color.slice(i, i + 2), 16));

const SIZE = 48; // pixels; the image is square, icons share its width
const SAMPLES = 4; // per axis, for anti-aliasing
const LEVELS = 16; // transparency steps per color

/** The PNG bytes for one icon per [platform, status] pair, left to right. */
function renderPlatformIconsPng(entries: [string, string][]): number[] {
    const colors = Array.from(new Set(entries.map(([, status]) => STATUS_COLORS[status] ?? STATUS_COLORS.online)));
    // Palette: index 0 transparent, then LEVELS opacity steps for each color.
    const palette: number[][] = [[0, 0, 0, 0]];
    for (const color of colors) {
        for (let level = 1; level <= LEVELS; level++) palette.push([...hex(color), Math.round((255 * level) / LEVELS)]);
    }

    const pixels = new Array(SIZE * SIZE).fill(0);
    const cell = SIZE / entries.length;
    const iconSize = Math.min(cell, SIZE) * (entries.length > 1 ? 0.95 : 1);
    entries.forEach(([platform, status], i) => {
        const icon = ICONS[platform];
        if (!icon) return;
        const colorIndex = colors.indexOf(STATUS_COLORS[status] ?? STATUS_COLORS.online);
        const left = i * cell + (cell - iconSize) / 2;
        const top = (SIZE - iconSize) / 2;
        for (let py = Math.floor(top); py < Math.ceil(top + iconSize); py++) {
            for (let px = Math.floor(left); px < Math.ceil(left + iconSize); px++) {
                let hits = 0;
                for (let sy = 0; sy < SAMPLES; sy++) {
                    for (let sx = 0; sx < SAMPLES; sx++) {
                        const gx = ((px + (sx + 0.5) / SAMPLES - left) / iconSize) * 24;
                        const gy = ((py + (sy + 0.5) / SAMPLES - top) / iconSize) * 24;
                        if (icon(gx, gy)) hits++;
                    }
                }
                const level = Math.round((hits / (SAMPLES * SAMPLES)) * LEVELS);
                if (level > 0) pixels[py * SIZE + px] = 1 + colorIndex * LEVELS + (level - 1);
            }
        }
    });
    return encodePalettePng(SIZE, SIZE, pixels, palette);
}

/** Data URI (works in RN <Image>, but not always in the tag/badge loader). */
export function renderPlatformIcons(entries: [string, string][]): string {
    return pngDataUri(renderPlatformIconsPng(entries));
}

/** Base64 PNG, for writing to a file served as file://. */
export function renderPlatformIconsBase64(entries: [string, string][]): string {
    return pngBase64(renderPlatformIconsPng(entries));
}
