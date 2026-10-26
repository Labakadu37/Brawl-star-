// React and React Native come from Discord's own bundle; we never ship a copy.

import { logger } from "./logger";
import { filters, find } from "./metro";

let react: any;
let reactNative: any;

/** Something React can render: a function/class, or a memo/forwardRef object. */
export const isComponent = (value: unknown): boolean =>
    typeof value === "function" || (value != null && typeof value === "object" && "$$typeof" in value);

// Host components may also be plain strings (as in tests).
const isElementType = (value: unknown) => typeof value === "string" || isComponent(value);

/**
 * react-native's index module. Matched by its components: some other Discord
 * modules also export AppRegistry and ToastAndroid.
 */
export function getReactNative(): any {
    if (reactNative) return reactNative;
    const index = find(m => isElementType(m.View) && isElementType(m.Text) && isElementType(m.ScrollView));
    if (index) return (reactNative = index);
    // Discord may import react-native's modules directly, never loading the
    // index: gather the pieces we use by name instead (not cached, may load later).
    const named = (name: string) =>
        find(m => (typeof m === "function" || isComponent(m)) && (m.displayName === name || m.name === name));
    return {
        View: named("View"),
        Text: named("Text"),
        ScrollView: named("ScrollView"),
        Switch: named("Switch"),
        Pressable: named("Pressable"),
        processColor: named("processColor"),
        ToastAndroid: find(filters.byProps("show", "showWithGravity")),
    };
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
    const component = find(m => isComponent(m[name]))?.[name];
    return isComponent(component) ? component : undefined;
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
    ErrorBoundary.prototype.componentDidCatch = (error: unknown) => logger.error("Page crashed:", error);
    ErrorBoundary.prototype.render = function (this: any) {
        if (!this.state.error) return this.props.children;
        const Text = getReactNative()?.Text;
        if (!isElementType(Text)) return null;
        const message = String(this.state.error?.stack ?? this.state.error);
        return h(Text, { style: { color: "#f23f43", padding: 16 }, selectable: true }, `JZSCord : ${message}`);
    };
    return ErrorBoundary;
}
