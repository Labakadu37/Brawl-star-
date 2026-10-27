import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "NoReplyMention",
    description: "Les réponses ne mentionnent plus la personne par défaut (pas de @ping)",
    authors: ["JZCord"],
    start({ waitFor, before }) {
        waitFor(filters.byProps("createPendingReply"), replies => {
            before(replies, "createPendingReply", ([reply]) => {
                if (reply && typeof reply === "object") reply.shouldMention = false;
            });
        });
    },
});
