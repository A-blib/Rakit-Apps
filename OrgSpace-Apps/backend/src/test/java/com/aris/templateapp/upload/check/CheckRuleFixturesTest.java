package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.template.IssueSeverity;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ZIP uji per aturan (alur-fitur-upload.md bagian 5.10): setiap aturan wajib punya {@code gagal.zip} yang tertangkap
 * dengan kode aturan yang benar dan {@code lolos.zip} yang tidak tertangkap. ZIP dibuat ulang dengan
 * {@code python3 tools/buat-zip-uji.py}.
 */
class CheckRuleFixturesTest {

    private static final Path FIXTURES = Paths.get("src/test/resources/test-fixtures");
    private static final AppProperties.Upload SETTINGS = testSettings();
    private static final TemplateChecker CHECKER = new TemplateChecker(SETTINGS);

    @Test
    void cleanTemplateHasNoIssuesAtAll() {
        TemplateChecker.Result result = check(FIXTURES.resolve("_DASAR/bersih.zip"));
        assertThat(result.findings()).isEmpty();
        assertThat(result.passed()).isTrue();
        assertThat(result.techInfo().pages()).containsExactly("index.html", "tentang.html");
        assertThat(result.techInfo().responsive()).isTrue();
        assertThat(result.techInfo().cssVariables()).extracting(TechInfo.CssVariable::name)
                .containsExactly("--primary", "--radius");
    }

    @Test
    void everyServerRuleHasFailAndPassFixtures() {
        List<String> missing = new ArrayList<>();
        for (CheckRule rule : CheckRule.values()) {
            if (rule.checkedOnDevice()) {
                continue;
            }
            for (String name : List.of("gagal.zip", "lolos.zip")) {
                if (!Files.exists(FIXTURES.resolve(rule.name()).resolve(name))) {
                    missing.add(rule.name() + "/" + name);
                }
            }
        }
        assertThat(missing).as("Aturan baru wajib punya ZIP uji (jalankan tools/buat-zip-uji.py)").isEmpty();
    }

    @TestFactory
    Stream<DynamicTest> failFixtureIsCaughtByItsRule() {
        return serverRules().map(rule -> DynamicTest.dynamicTest(rule.name() + "/gagal.zip", () -> {
            TemplateChecker.Result result = check(FIXTURES.resolve(rule.name()).resolve("gagal.zip"));
            assertThat(result.findings()).extracting(Finding::rule).as(describe(result)).contains(rule);
            if (rule.severity() == IssueSeverity.ERROR) {
                assertThat(result.passed()).isFalse();
            }
        }));
    }

    @TestFactory
    Stream<DynamicTest> passFixturesAreNotCaughtByTheirRule() {
        return serverRules().flatMap(rule -> passFixtures(rule).map(zip -> DynamicTest.dynamicTest(
                rule.name() + "/" + zip.getFileName(), () -> {
                    TemplateChecker.Result result = check(zip);
                    assertThat(result.findings()).extracting(Finding::rule).as(describe(result)).doesNotContain(rule);
                })));
    }

    @Test
    void nameIsTakenFromZipFileName() {
        assertThat(TemplateChecker.nameFromFile("toko-kue.zip")).isEqualTo("Toko Kue");
        assertThat(TemplateChecker.nameFromFile("portofolio_minimal.ZIP")).isEqualTo("Portofolio Minimal");
        assertThat(TemplateChecker.nameFromFile(".zip")).isEqualTo("Template baru");
    }

    private static Stream<CheckRule> serverRules() {
        return Arrays.stream(CheckRule.values()).filter(rule -> !rule.checkedOnDevice());
    }

    /** lolos.zip + lolos-*.zip (kasus salah tuduh dari laporan "Ini keliru?"). */
    private static Stream<Path> passFixtures(CheckRule rule) {
        try (Stream<Path> files = Files.list(FIXTURES.resolve(rule.name()))) {
            return files.filter(p -> p.getFileName().toString().matches("lolos(-.+)?\\.zip")).sorted().toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static TemplateChecker.Result check(Path zip) {
        try {
            return CHECKER.check(Files.readAllBytes(zip), zip.getFileName().toString(), stage -> { });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String describe(TemplateChecker.Result result) {
        StringBuilder sb = new StringBuilder("Temuan:\n");
        result.findings().forEach(f -> sb.append("  ").append(f.rule()).append(": ").append(f.message()).append('\n'));
        return sb.toString();
    }

    /**
     * Pengaturan sama dengan application.yml, kecuali batas ukuran yang diperkecil (±1/100) agar ZIP uji untuk
     * aturan ukuran tetap kecil di repo.
     */
    static AppProperties.Upload testSettings() {
        try {
            StandardEnvironment env = new StandardEnvironment();
            for (PropertySource<?> source : new YamlPropertySourceLoader()
                    .load("application", new ClassPathResource("application.yml"))) {
                env.getPropertySources().addLast(source);
            }
            AppProperties.Upload upload = Binder.get(env).bind("app.upload", AppProperties.Upload.class).get();
            AppProperties.Limits l = upload.limits();
            AppProperties.Limits small = new AppProperties.Limits(200 * 1024, 400 * 1024, 30, 100 * 1024, 20 * 1024,
                    60 * 1024, l.maxPages(), 20 * 1024, 2 * 1024, l.jsBodyMinTextChars(), l.jsBodyMinImages(),
                    l.minEditableElements());
            return new AppProperties.Upload(upload.storageDir(), small, upload.chunkSizeBytes(), upload.draftLimit(),
                    upload.draftExpireDays(), upload.draftWarnDays(), upload.sessionExpireHours(), upload.trustedCdnHosts(),
                    upload.allowedLibraries(), upload.knownLibraries(), upload.iframeAllowed(), upload.trackerPatterns(),
                    upload.remoteDataPatterns());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
