package academy.cashbackmerge.report;

import academy.cashbackmerge.simulation.SimulationReport;
import java.nio.file.Path;
import java.util.Locale;

public enum ReportFormat {
    MARKDOWN {
        @Override
        public String render(SimulationReport report) {
            return MarkdownReport.render(report);
        }
    },
    JSON {
        @Override
        public String render(SimulationReport report) {
            return JsonReport.render(report);
        }
    };

    public abstract String render(SimulationReport report);

    public static ReportFormat forFile(Path file) {
        var name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".json") ? JSON : MARKDOWN;
    }
}
