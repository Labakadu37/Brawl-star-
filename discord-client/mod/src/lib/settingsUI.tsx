// Adds a "JZSCord" section to Discord's own settings screen, the way Revenge
// does: rows are declared in SETTING_RENDERER_CONFIG, and the section is
// inserted into the list built by createList().

import { findAssetId } from "./assets";
import { logger } from "./logger";
import { filters, find, waitFor } from "./metro";
import { after } from "./patcher";
import { createErrorBoundary, findComponent, h } from "./react";
import { PluginsPage } from "../ui/PluginsPage";

const SECTION_TITLE = "JZSCord";
const ROW_KEY = "VERCORS";
const PAGE_ROUTE = "VERCORS_PAGE";

let SafePage: any;

function PageRoute() {
    SafePage ??= createErrorBoundary();
    return (
        <SafePage>
            <PluginsPage />
        </SafePage>
    );
}

function openPage() {
    const navigation = find(filters.byProps("getRootNavigationRef"))?.getRootNavigationRef?.();
    if (!navigation) return logger.error("Settings: navigation not found");
    navigation.navigate(PAGE_ROUTE, { title: "JZSCord" });
}

function rows() {
    const icon = findAssetId("PuzzlePieceIcon") ?? findAssetId("SettingsIcon");
    const TableRow = findComponent("TableRow");
    return {
        [PAGE_ROUTE]: {
            type: "route",
            title: () => "JZSCord",
            useTitle: () => "JZSCord",
            screen: { route: PAGE_ROUTE, getComponent: () => PageRoute },
        },
        [ROW_KEY]: {
            type: "pressable",
            title: () => "Plugins JZSCord",
            useTitle: () => "Plugins JZSCord",
            icon,
            IconComponent: () => (icon != null && TableRow?.Icon ? <TableRow.Icon source={icon} /> : null),
            useTrailing: () => `v${__VERCORS_VERSION__}`,
            onPress: openPage,
            withArrow: true,
        },
    };
}

export function initSettingsUI() {
    waitFor(filters.byProps("SETTING_RENDERER_CONFIG"), constants => {
        let config = constants.SETTING_RENDERER_CONFIG;
        Object.defineProperty(constants, "SETTING_RENDERER_CONFIG", {
            enumerable: true,
            configurable: true,
            get: () => {
                try {
                    return { ...config, ...rows() };
                } catch (e) {
                    logger.error("Settings rows failed:", e);
                    return config;
                }
            },
            set: value => {
                config = value;
            },
        });
        logger.log("Settings: rows registered");
    });

    waitFor(filters.byProps("createList"), module => {
        after(module, "createList", ([config]) => {
            const sections = config?.sections;
            if (!Array.isArray(sections) || sections.some((s: any) => s?.label === SECTION_TITLE)) return;
            const account = sections.findIndex((s: any) => s?.settings?.includes?.("ACCOUNT"));
            sections.splice(account === -1 ? 0 : account + 1, 0, {
                label: SECTION_TITLE,
                title: SECTION_TITLE,
                settings: [ROW_KEY],
            });
        });
        logger.log("Settings: section added");
    });
}
