package com.aris.templateapp.provider;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.provider.dto.ActionItemResponse;
import com.aris.templateapp.provider.dto.DailyCountResponse;
import com.aris.templateapp.provider.dto.ProviderDashboardResponse;
import com.aris.templateapp.provider.dto.SummaryResponse;
import com.aris.templateapp.template.ProviderTemplateQueries;
import com.aris.templateapp.template.TemplateEventType;
import com.aris.templateapp.template.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Beranda provider (alur-provider.md bagian 3). */
@Service
public class ProviderDashboardService {

    private static final int POPULAR_LIMIT = 3;

    private final ProviderAccess providerAccess;
    private final ProviderTemplateQueries queries;
    private final TemplateRepository templateRepository;
    private final Clock clock;
    private final ZoneId zone;

    public ProviderDashboardService(ProviderAccess providerAccess, ProviderTemplateQueries queries,
                                    TemplateRepository templateRepository, Clock clock, AppProperties properties) {
        this.providerAccess = providerAccess;
        this.queries = queries;
        this.templateRepository = templateRepository;
        this.clock = clock;
        this.zone = ZoneId.of(properties.timezone());
    }

    @Transactional(readOnly = true)
    public ProviderDashboardResponse dashboard(UUID userId, String periodValue) {
        ProviderProfile profile = providerAccess.requireActive(userId);
        StatsPeriod period = StatsPeriod.parse(periodValue, clock, zone);
        boolean complete = isProfileComplete(profile);

        List<ActionItemResponse> actionItems = new ArrayList<>(queries.templateActionItems(userId));
        if (!complete) {
            actionItems.add(new ActionItemResponse(ActionItemResponse.PROFILE_INCOMPLETE, null, null, 0, 0));
        }
        SummaryResponse summary = new SummaryResponse(
                queries.countPublished(userId),
                queries.countEvents(userId, TemplateEventType.VIEW, period.from()),
                queries.countEvents(userId, TemplateEventType.DOWNLOAD, period.from()),
                periodValue == null ? "7d" : periodValue);

        return new ProviderDashboardResponse(profile.getCreatorName(), templateRepository.existsByProviderId(userId),
                complete, actionItems, summary, queries.downloadTrend(userId, period, zone),
                queries.popular(userId, period.from(), POPULAR_LIMIT));
    }

    /** GET /providers/me/stats/downloads (bagian 3.7): data grafik saja. */
    @Transactional(readOnly = true)
    public List<DailyCountResponse> downloadTrend(UUID userId, String periodValue) {
        providerAccess.requireActive(userId);
        return queries.downloadTrend(userId, StatsPeriod.parse(periodValue, clock, zone), zone);
    }

    /** Profil dianggap lengkap jika bio dan keahlian sudah diisi (langkah pertama checklist provider baru). */
    static boolean isProfileComplete(ProviderProfile profile) {
        return profile.getBio() != null && !profile.getBio().isBlank()
                && profile.getSpecialties() != null && !profile.getSpecialties().isEmpty();
    }
}
