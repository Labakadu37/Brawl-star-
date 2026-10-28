// Translation via Google Translate's free endpoint (no key), from the phone.

export interface Translation {
    text: string;
    source: string;
}

const ENDPOINT = "https://translate.googleapis.com/translate_a/single";

export async function translate(text: string, target: string, source = "auto"): Promise<Translation> {
    const url =
        `${ENDPOINT}?client=gtx&sl=${encodeURIComponent(source)}&tl=${encodeURIComponent(target)}` +
        `&dt=t&q=${encodeURIComponent(text)}`;
    const res = await (globalThis as any).fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    // data = [ [ [translatedChunk, originalChunk, …], … ], …, detectedSource ]
    const translated = (data?.[0] ?? []).map((chunk: any[]) => chunk?.[0] ?? "").join("");
    const detected = data?.[2] ?? source;
    if (!translated) throw new Error("empty translation");
    return { text: translated, source: detected };
}
