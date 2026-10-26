// Monkey-patching of object methods, with several patches per method and
// clean removal. Same model as Vencord / Vendetta: before, after, instead.

import { logger } from "./logger";

export type BeforeCallback = (this: any, args: any[]) => any[] | void;
export type AfterCallback = (this: any, args: any[], result: any) => any;
export type InsteadCallback = (this: any, args: any[], original: (...args: any[]) => any) => any;
export type Unpatch = () => void;

interface PatchState {
    descriptor: PropertyDescriptor | undefined;
    original: (...args: any[]) => any;
    befores: BeforeCallback[];
    afters: AfterCallback[];
    insteads: InsteadCallback[];
}

const states = new WeakMap<object, Map<string, PatchState>>();

function setProperty(target: any, name: string, value: unknown) {
    try {
        target[name] = value;
    } catch {}
    // Babel's ES module interop often exposes exports as getters: redefine them.
    if (target[name] !== value) {
        Object.defineProperty(target, name, { value, writable: true, configurable: true, enumerable: true });
    }
}

function createPatchedFunction(state: PatchState) {
    return function (this: any, ...callArgs: any[]) {
        let args = callArgs;
        for (const before of state.befores) {
            try {
                const newArgs = before.call(this, args);
                if (Array.isArray(newArgs)) args = newArgs;
            } catch (e) {
                logger.error("before patch threw:", e);
            }
        }

        let implementation = (...a: any[]) => state.original.apply(this, a);
        for (const instead of state.insteads) {
            const previous = implementation;
            implementation = (...a: any[]) => {
                try {
                    return instead.call(this, a, previous);
                } catch (e) {
                    logger.error("instead patch threw, falling back:", e);
                    return previous(...a);
                }
            };
        }
        let result = implementation(...args);

        for (const after of state.afters) {
            try {
                const newResult = after.call(this, args, result);
                if (newResult !== undefined) result = newResult;
            } catch (e) {
                logger.error("after patch threw:", e);
            }
        }
        return result;
    };
}

function getState(target: any, name: string): PatchState {
    let byName = states.get(target);
    if (!byName) states.set(target, (byName = new Map()));

    let state = byName.get(name);
    if (state) return state;

    const original = target[name];
    if (typeof original !== "function") throw new Error(`Cannot patch ${name}: not a function`);

    state = {
        descriptor: Object.getOwnPropertyDescriptor(target, name),
        original,
        befores: [],
        afters: [],
        insteads: [],
    };
    const patched = createPatchedFunction(state);
    Object.assign(patched, original);
    patched.toString = () => original.toString();
    setProperty(target, name, patched);
    byName.set(name, state);
    return state;
}

function addPatch<T>(target: any, name: string, list: keyof Pick<PatchState, "befores" | "afters" | "insteads">, callback: T): Unpatch {
    const state = getState(target, name);
    (state[list] as T[]).push(callback);

    return () => {
        const callbacks = state[list] as T[];
        const index = callbacks.indexOf(callback);
        if (index !== -1) callbacks.splice(index, 1);

        if (state.befores.length || state.afters.length || state.insteads.length) return;
        if (state.descriptor) Object.defineProperty(target, name, state.descriptor);
        else delete target[name];
        states.get(target)?.delete(name);
    };
}

export const before = (target: any, name: string, cb: BeforeCallback) => addPatch(target, name, "befores", cb);
export const after = (target: any, name: string, cb: AfterCallback) => addPatch(target, name, "afters", cb);
export const instead = (target: any, name: string, cb: InsteadCallback) => addPatch(target, name, "insteads", cb);
