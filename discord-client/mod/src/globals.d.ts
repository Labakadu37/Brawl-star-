declare const __VERCORS_VERSION__: string;

// Minimal console typing: Hermes provides it, but we don't pull in DOM/node libs.
declare const console: {
    log(...args: unknown[]): void;
    info(...args: unknown[]): void;
    warn(...args: unknown[]): void;
    error(...args: unknown[]): void;
};

declare function setTimeout(cb: () => void, ms?: number): number;
declare function clearTimeout(id: number): void;

// JSX is compiled to h() (see lib/react.ts); components come from Discord at runtime.
declare namespace JSX {
    type Element = any;
    interface IntrinsicElements {
        [name: string]: any;
    }
}
