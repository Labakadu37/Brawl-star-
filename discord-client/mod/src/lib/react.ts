// React and React Native come from Discord's own bundle; we never ship a copy.

import { filters, find } from "./metro";

let react: any;
export let ReactNative: any;

export function setReactNative(module: any) {
    ReactNative = module;
}

export function getReact() {
    return (react ??= find(filters.byProps("createElement", "useState", "useEffect")));
}

/** JSX factory. */
export function h(type: any, props: any, ...children: any[]) {
    return getReact().createElement(type, props, ...children);
}

export const Fragment = Symbol.for("react.fragment");

export const useState = <T>(initial: T): [T, (v: T | ((old: T) => T)) => void] => getReact().useState(initial);
export const useEffect = (effect: () => void | (() => void), deps?: unknown[]) => getReact().useEffect(effect, deps);

/** A component exported under `name` by one of Discord's modules (TableRow, TableSwitchRow…). */
export function findComponent(name: string): any {
    return find(filters.byProps(name))?.[name];
}

/**
 * Catches render errors so a broken page shows a message instead of taking
 * Discord's settings down. Written without `class` syntax, which Hermes may
 * not support.
 */
export function createErrorBoundary() {
    const React = getReact();
    function ErrorBoundary(this: any, props: any) {
        React.Component.call(this, props);
        this.state = { error: null };
    }
    ErrorBoundary.prototype = Object.create(React.Component.prototype);
    ErrorBoundary.prototype.constructor = ErrorBoundary;
    (ErrorBoundary as any).getDerivedStateFromError = (error: unknown) => ({ error });
    ErrorBoundary.prototype.render = function (this: any) {
        if (!this.state.error) return this.props.children;
        const { Text } = ReactNative;
        return h(Text, { style: { color: "#f23f43", padding: 16 }, selectable: true }, `Vercors : ${String(this.state.error?.stack ?? this.state.error)}`);
    };
    return ErrorBoundary;
}
