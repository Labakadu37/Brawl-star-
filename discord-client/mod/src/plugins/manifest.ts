// Build-time only: describes the built-in plugins for dist/plugins.json.
import { builtinPlugins } from ".";

export default builtinPlugins.map(({ name, description, enabledByDefault }) => ({
    name,
    description,
    enabledByDefault: enabledByDefault ?? false,
}));
