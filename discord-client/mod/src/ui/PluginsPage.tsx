import { LOGO_URL } from "../lib/branding";
import { getLogHistory } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { getPluginOption, getPlugins, isRunning, setPluginEnabled, setPluginOption } from "../lib/plugins";
import { findComponent, getReactNative, h, isComponent, useEffect, useState } from "../lib/react";
import { promptRestart, reloadDiscord } from "../lib/restart";
import { getUserToken, maskToken } from "../lib/token";

const MUTED = "#949ba4";
const TEXT = "#dbdee1";

// Plain React Native stand-ins, used if Discord's components can't be found.
function FallbackGroup({ title, children }: any) {
    const { View, Text } = getReactNative();
    return (
        <View style={{ marginBottom: 16 }}>
            <Text style={{ color: MUTED, fontWeight: "bold", marginBottom: 8 }}>{title}</Text>
            {children}
        </View>
    );
}

function FallbackSwitchRow({ label, subLabel, value, onValueChange }: any) {
    const { View, Text, Switch } = getReactNative();
    return (
        <View style={{ flexDirection: "row", alignItems: "center", paddingVertical: 10 }}>
            <View style={{ flex: 1 }}>
                <Text style={{ color: TEXT, fontWeight: "bold" }}>{label}</Text>
                <Text style={{ color: MUTED, fontSize: 12 }}>{subLabel}</Text>
            </View>
            <Switch value={value} onValueChange={onValueChange} />
        </View>
    );
}

function FallbackRow({ label, onPress }: any) {
    const { Pressable, Text } = getReactNative();
    return (
        <Pressable onPress={onPress} style={{ paddingVertical: 12 }}>
            <Text style={{ color: TEXT }}>{label}</Text>
        </Pressable>
    );
}

export function PluginsPage() {
    const ReactNative = getReactNative();
    const { Text } = ReactNative;
    const ScrollView = ReactNative.ScrollView ?? ReactNative.View;
    const Group = findComponent("TableRowGroup") ?? FallbackGroup;
    const SwitchRow = findComponent("TableSwitchRow") ?? FallbackSwitchRow;
    const Row = findComponent("TableRow") ?? FallbackRow;

    const [, rerender] = useState(0);
    const [showLogs, setShowLogs] = useState(false);
    const [showToken, setShowToken] = useState(false);

    const navigation = find(filters.byProps("NavigationContainer", "useNavigation"))?.useNavigation?.();
    useEffect(() => void navigation?.setOptions?.({ title: "JZCord" }), []);

    const copyLogs = () => {
        find(filters.byProps("setString", "getString"))?.setString(getLogHistory());
        ReactNative.ToastAndroid?.show("Journal copié", 0);
    };

    const token = getUserToken();
    const copyToken = () => {
        if (!token) return ReactNative.ToastAndroid?.show("Token introuvable", 0);
        find(filters.byProps("setString", "getString"))?.setString(token);
        ReactNative.ToastAndroid?.show("Token copié", 0);
    };

    return (
        <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 80 }}>
            {isComponent(ReactNative.Image) ? (
                <ReactNative.View style={{ alignItems: "center", marginBottom: 16 }}>
                    <ReactNative.Image source={{ uri: LOGO_URL }} style={{ width: 96, height: 96, borderRadius: 22 }} />
                    <Text style={{ color: TEXT, fontSize: 20, fontWeight: "bold", marginTop: 8 }}>JZCord</Text>
                    <Text style={{ color: MUTED, fontSize: 12 }}>v{__VERCORS_VERSION__}</Text>
                </ReactNative.View>
            ) : null}
            <Group title="Plugins">
                {getPlugins().flatMap(plugin => [
                    <SwitchRow
                        key={plugin.name}
                        label={plugin.name}
                        subLabel={plugin.description}
                        value={isRunning(plugin.name)}
                        onValueChange={(enabled: boolean) => {
                            setPluginEnabled(plugin, enabled);
                            rerender(n => n + 1);
                            promptRestart(`${plugin.name} ${enabled ? "activé" : "désactivé"}. Redémarre Discord pour que tout s'applique.`);
                        }}
                    />,
                    // Options of a running plugin: tap to go to the next choice.
                    ...(isRunning(plugin.name) ? plugin.options ?? [] : []).map(option => {
                        const current = getPluginOption(plugin, option.key);
                        const index = option.choices.findIndex(c => c.value === current);
                        const next = option.choices[(index + 1) % option.choices.length];
                        return (
                            <Row
                                key={`${plugin.name}.${option.key}`}
                                label={`   ↳ ${option.label} : ${option.choices[index]?.label ?? current}`}
                                onPress={() => {
                                    setPluginOption(plugin, option.key, next.value);
                                    rerender(n => n + 1);
                                }}
                            />
                        );
                    }),
                ])}
            </Group>
            <Text style={{ height: 16 }} />
            <Group title="Discord">
                <Row label="🔄 Redémarrer Discord" onPress={() => reloadDiscord()} />
            </Group>
            <Text style={{ height: 16 }} />
            <Group title="Compte">
                <Row
                    label={showToken ? "🙈 Masquer le token" : "🔑 Révéler le token"}
                    onPress={() => setShowToken(v => !v)}
                />
                {showToken ? (
                    <ReactNative.View style={{ paddingVertical: 8 }}>
                        <Text style={{ color: "#f23f43", fontSize: 11, marginBottom: 6 }}>
                            ⚠ Ne le montre jamais à personne. Un token = accès total à ton compte sans mot de passe.
                        </Text>
                        <Text selectable style={{ color: TEXT, fontFamily: "monospace", fontSize: 11 }}>
                            {token ?? "Token introuvable"}
                        </Text>
                    </ReactNative.View>
                ) : (
                    <Text style={{ color: MUTED, fontSize: 11, paddingVertical: 4 }}>
                        {token ? maskToken(token) : "Token introuvable"}
                    </Text>
                )}
                <Row label="📋 Copier le token" onPress={copyToken} />
            </Group>
            <Text style={{ height: 16 }} />
            <Group title="Journal">
                <Row label={showLogs ? "Masquer le journal" : "Afficher le journal"} onPress={() => setShowLogs(v => !v)} arrow />
                <Row label="Copier le journal" onPress={copyLogs} />
            </Group>
            {showLogs ? (
                <Text selectable style={{ color: MUTED, fontFamily: "monospace", fontSize: 10, marginTop: 12 }}>
                    {getLogHistory() || "Journal vide"}
                </Text>
            ) : null}
            <Text style={{ color: MUTED, fontSize: 12, textAlign: "center", marginTop: 24 }}>
                JZCord v{__VERCORS_VERSION__} · les changements s'appliquent tout de suite
            </Text>
        </ScrollView>
    );
}
