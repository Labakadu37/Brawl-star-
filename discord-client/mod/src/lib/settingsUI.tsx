// Adds a "JZCord" section to Discord's own settings screen, the way Revenge
// does: rows are declared in SETTING_RENDERER_CONFIG, and the section is
// inserted into the list built by createList().

import { findAssetId } from "./assets";
import { LOGO_URL } from "./branding";
import { logger } from "./logger";
import { filters, find, waitFor } from "./metro";
import { after } from "./patcher";
import { createErrorBoundary, findComponent, getReactNative, h, isComponent } from "./react";
import { PluginsPage } from "../ui/PluginsPage";

const SECTION_TITLE = "JZCord";
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
    navigation.navigate(PAGE_ROUTE, { title: "JZCord" });
}

// Our logo, drawn as a plain image: TableRow.Icon tints its icon to one color.
function LogoIcon() {
    const Image = getReactNative()?.Image;
    return isComponent(Image) ? <Image source={{ uri: LOGO_URL }} style={{ width: 24, height: 24, borderRadius: 6 }} /> : null;
}

function rows() {
    const fallbackIcon = findAssetId("PuzzlePieceIcon") ?? findAssetId("SettingsIcon");
    const TableRow = findComponent("TableRow");
    const hasImage = isComponent(getReactNative()?.Image);
    return {
        [PAGE_ROUTE]: {
            type: "route",
            title: () => "JZCord",
            useTitle: () => "JZCord",
            screen: { route: PAGE_ROUTE, getComponent: () => PageRoute },
        },
        [ROW_KEY]: {
            type: "pressable",
            title: () => "Plugins JZCord",
            useTitle: () => "Plugins JZCord",
            icon: hasImage ? { uri: LOGO_URL } : fallbackIcon,
            IconComponent: hasImage
                ? LogoIcon
                : () => (fallbackIcon != null && TableRow?.Icon ? <TableRow.Icon source={fallbackIcon} /> : null),
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
