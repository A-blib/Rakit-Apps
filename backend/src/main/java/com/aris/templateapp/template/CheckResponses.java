package com.aris.templateapp.template;

import com.aris.templateapp.template.dto.CheckResponse;
import com.aris.templateapp.template.dto.IssueResponse;
import com.aris.templateapp.upload.CheckReport;
import com.aris.templateapp.upload.CheckReportRepository;
import com.aris.templateapp.upload.check.CheckRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Mengubah hasil pengecekan di database menjadi {@link CheckResponse}; dipakai Template Anda dan Upload. */
@Component
@RequiredArgsConstructor
public class CheckResponses {

    private static final Map<String, CheckRule> RULES = Arrays.stream(CheckRule.values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final CheckReportRepository reportRepository;

    /** Pengecekan terakhir template, atau null jika belum pernah dicek. */
    public CheckResponse latest(UUID templateId) {
        return checkRepository.findFirstByTemplateIdOrderByVersionDesc(templateId).map(this::of).orElse(null);
    }

    public CheckResponse of(TemplateCheck check) {
        List<TemplateCheckIssue> issues = issueRepository.findByCheckIdOrderBySeverityAscCodeAscFileAscLineAsc(check.getId());
        Set<UUID> reported = reportRepository.findByIssueIdIn(issues.stream().map(TemplateCheckIssue::getId).toList())
                .stream().map(CheckReport::getIssueId).collect(Collectors.toSet());
        return new CheckResponse(check.getVersion(), check.getStatus(), check.getStage(), check.getFinishedAt(),
                issuesOf(issues, IssueSeverity.ERROR, reported), issuesOf(issues, IssueSeverity.WARNING, reported));
    }

    private static List<IssueResponse> issuesOf(List<TemplateCheckIssue> issues, IssueSeverity severity, Set<UUID> reported) {
        return issues.stream()
                .filter(i -> i.getSeverity() == severity)
                .map(i -> {
                    CheckRule rule = RULES.get(i.getCode());
                    return new IssueResponse(i.getId(), i.getCode(), rule == null ? null : rule.title(), i.getRuleVersion(),
                            i.getMessage(), i.getFile(), i.getLine(), i.getSuggestion(), reported.contains(i.getId()));
                })
                .toList();
    }
}
