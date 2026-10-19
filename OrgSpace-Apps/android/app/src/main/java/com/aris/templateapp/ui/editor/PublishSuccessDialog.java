package com.aris.templateapp.ui.editor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.DirectPublishResponseDto;
import com.aris.templateapp.databinding.DialogPublishSuccessBinding;

/**
 * Layar konfirmasi saat website berhasil diterbitkan langsung (publish langsung).
 * Menampilkan port, URL, dan ruang pribadi tempat website aktif, serta tombol Buka Website.
 */
public class PublishSuccessDialog extends DialogFragment {

    private static final String TAG = "publish-success";
    private static final String ARG_URL = "url";
    private static final String ARG_PORT = "port";
    private static final String ARG_ORG_NAME = "org_name";
    private static final String ARG_SLUG = "slug";
    private static final String ARG_PROJECT_ID = "project_id";

    private DialogPublishSuccessBinding binding;

    public static void show(Fragment editor, DirectPublishResponseDto result) {
        PublishSuccessDialog dialog = new PublishSuccessDialog();
        Bundle args = new Bundle();
        args.putString(ARG_URL, result.url);
        args.putInt(ARG_PORT, result.port);
        args.putString(ARG_ORG_NAME, result.organizationName);
        args.putString(ARG_SLUG, result.slug);
        args.putString(ARG_PROJECT_ID, result.projectId != null ? result.projectId.toString() : "");
        dialog.setArguments(args);
        dialog.show(editor.getChildFragmentManager(), TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_App_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogPublishSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String url = args.getString(ARG_URL, "");
        int port = args.getInt(ARG_PORT, 8080);
        String orgName = args.getString(ARG_ORG_NAME, "Ruang Pribadi");

        binding.textWorkspace.setText(orgName);
        binding.textPort.setText(String.valueOf(port));
        binding.textUrl.setText(url);

        binding.btnOpenSite.setOnClickListener(v -> {
            if (!url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        binding.btnCopyLink.setOnClickListener(v -> {
            if (!url.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Website URL", url);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), R.string.publish_success_copied, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnCustomDomain.setOnClickListener(v -> {
            String projectId = args.getString(ARG_PROJECT_ID, "");
            String slug = args.getString(ARG_SLUG, "");
            dismiss();
            Fragment parent = getParentFragment();
            if (parent != null) {
                com.aris.templateapp.ui.website.CustomDomainDialog.show(parent, projectId, "", slug, url, null);
            }
        });

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        FullScreenDialogs.apply(this, binding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
