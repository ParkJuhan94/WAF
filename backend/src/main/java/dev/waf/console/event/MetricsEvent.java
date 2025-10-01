package dev.waf.console.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetricsEvent extends WAFEvent {

    private String metricName;
    private Double value;
    private String unit;
    private Map<String, String> tags;
    private Long windowStart;
    private Long windowEnd;

    @Override
    public WAFEventType getEventType() {
        return WAFEventType.METRICS;
    }
}