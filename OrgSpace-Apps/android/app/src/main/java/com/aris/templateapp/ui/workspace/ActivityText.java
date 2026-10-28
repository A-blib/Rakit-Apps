package com.aris.templateapp.ui.workspace;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Activity;
import com.aris.templateapp.data.model.WorkspaceRole;

/**
 * Menyusun kalimat Riwayat aktivitas, mis. "Rina mengubah peran Budi menjadi Admin". Hasilnya id string + argumen
 * (bukan teks jadi) supaya bisa diuji tanpa Android dan teksnya tetap di strings.xml.
 */
public final class ActivityText {

    private ActivityText() {
    }

    public static final class Sentence {
        @StringRes public final int text;
        public final Object[] args;

        Sentence(@StringRes int text, Object... args) {
            this.text = text;
            this.args = args;
        }
    }

    /**
     * Argumen pertama selalu nama pelaku. Nama peran dikirim sebagai id string ({@link Integer}) dan diterjemahkan
     * pemanggil; null pelaku = aksi sistem atau akun yang sudah dihapus.
     */
    public static Sentence of(Activity activity, String unknownActor) {
        String actor = activity.actorName != null ? activity.actorName : unknownActor;
        String member = activity.meta("memberName");
        switch (activity.action) {
            case "org.created":
                return new Sentence(R.string.activity_org_created, actor);
            case "org.updated":
                return new Sentence(R.string.activity_org_updated, actor);
            case "org.logo_changed":
                return new Sentence(R.string.activity_logo_changed, actor);
            case "member.joined":
                return new Sentence(R.string.activity_member_joined, actor,
                        roleLabel(activity.meta("role")));
            case "member.left":
                return new Sentence(R.string.activity_member_left, actor);
            case "member.removed":
                return new Sentence(R.string.activity_member_removed, actor, nameOr(member, unknownActor));
            case "member.role_changed":
                return new Sentence(R.string.activity_role_changed, actor, nameOr(member, unknownActor),
                        roleLabel(activity.meta("to")));
            case "org.ownership_transferred":
                return new Sentence(R.string.activity_ownership_transferred, actor, nameOr(member, unknownActor));
            case "invitation.created":
                return new Sentence(R.string.activity_invitation_created, actor, roleLabel(activity.meta("role")));
            case "portal_credential.saved":
                return new Sentence(R.string.activity_portal_credential_saved, actor, roleLabel(activity.meta("role")));
            case "invitation.revoked":
                return new Sentence(R.string.activity_invitation_revoked, actor);
            case "website.renamed":
                return new Sentence(R.string.activity_website_renamed, actor, nameOr(activity.meta("from"), ""),
                        nameOr(activity.meta("to"), ""));
            case "website.duplicated":
                return new Sentence(R.string.activity_website_duplicated, actor, nameOr(activity.meta("from"), ""));
            case "website.deleted":
                return new Sentence(R.string.activity_website_deleted, actor, nameOr(activity.meta("name"), ""));
            case "website.publish_requested":
                return new Sentence(R.string.activity_publish_requested, actor, nameOr(activity.meta("name"), ""));
            default:
                return new Sentence(R.string.activity_other, actor);
        }
    }

    private static Integer roleLabel(String value) {
        return WorkspaceUi.roleLabel(WorkspaceRole.fromValue(value));
    }

    private static String nameOr(String name, String fallback) {
        return name != null ? name : fallback;
    }
}
