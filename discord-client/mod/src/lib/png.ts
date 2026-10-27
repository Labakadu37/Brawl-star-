// Minimal PNG encoder (8-bit palette with transparency, uncompressed deflate),
// enough to build small icons at runtime as data: URIs.

const crcTable = Array.from({ length: 256 }, (_, n) => {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    return c >>> 0;
});

function crc32(bytes: number[]): number {
    let crc = 0xffffffff;
    for (const b of bytes) crc = crcTable[(crc ^ b) & 0xff] ^ (crc >>> 8);
    return (crc ^ 0xffffffff) >>> 0;
}

const u32 = (n: number) => [(n >>> 24) & 0xff, (n >>> 16) & 0xff, (n >>> 8) & 0xff, n & 0xff];

function chunk(type: string, data: number[]): number[] {
    const typed = [...Array.from(type, c => c.charCodeAt(0)), ...data];
    return [...u32(data.length), ...typed, ...u32(crc32(typed))];
}

/** zlib stream made of "stored" (uncompressed) deflate blocks. */
function zlibStore(data: number[]): number[] {
    const out = [0x78, 0x01];
    for (let i = 0; i < data.length || i === 0; i += 65535) {
        const block = data.slice(i, i + 65535);
        const last = i + 65535 >= data.length ? 1 : 0;
        out.push(last, block.length & 0xff, block.length >> 8, ~block.length & 0xff, (~block.length >> 8) & 0xff);
        for (const byte of block) out.push(byte);
    }
    let a = 1;
    let b = 0;
    for (const byte of data) {
        a = (a + byte) % 65521;
        b = (b + a) % 65521;
    }
    return [...out, ...u32(((b << 16) | a) >>> 0)];
}

const BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

function base64(bytes: number[]): string {
    let out = "";
    for (let i = 0; i < bytes.length; i += 3) {
        const [a, b = 0, c = 0] = [bytes[i], bytes[i + 1], bytes[i + 2]];
        const n = (a << 16) | (b << 8) | c;
        out += BASE64[(n >> 18) & 63] + BASE64[(n >> 12) & 63];
        out += i + 1 < bytes.length ? BASE64[(n >> 6) & 63] : "=";
        out += i + 2 < bytes.length ? BASE64[n & 63] : "=";
    }
    return out;
}

/**
 * Encodes a palette image. `pixels` holds one palette index per pixel, row by
 * row; `palette` holds [r, g, b, a] entries (at most 256).
 */
export function encodePalettePng(width: number, height: number, pixels: number[], palette: number[][]): number[] {
    const raw: number[] = [];
    for (let y = 0; y < height; y++) raw.push(0, ...pixels.slice(y * width, (y + 1) * width));
    return [
        0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        ...chunk("IHDR", [...u32(width), ...u32(height), 8, 3, 0, 0, 0]),
        ...chunk("PLTE", palette.flatMap(([r, g, b]) => [r, g, b])),
        ...chunk("tRNS", palette.map(p => p[3])),
        ...chunk("IDAT", zlibStore(raw)),
        ...chunk("IEND", []),
    ];
}

/** Encodes a truecolor image; `rgba` holds 4 bytes per pixel, row by row. */
export function encodeRgbaPng(width: number, height: number, rgba: number[]): number[] {
    const raw: number[] = [];
    for (let y = 0; y < height; y++) {
        raw.push(0);
        for (let i = y * width * 4; i < (y + 1) * width * 4; i++) raw.push(rgba[i]);
    }
    return [
        0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        ...chunk("IHDR", [...u32(width), ...u32(height), 8, 6, 0, 0, 0]),
        ...chunk("IDAT", zlibStore(raw)),
        ...chunk("IEND", []),
    ];
}

export const pngBase64 = (png: number[]) => base64(png);
export const pngDataUri = (png: number[]) => `data:image/png;base64,${pngBase64(png)}`;
