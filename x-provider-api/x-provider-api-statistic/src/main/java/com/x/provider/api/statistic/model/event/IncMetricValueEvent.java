package com.x.provider.api.statistic.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncMetricValueEvent {
    private Long longValue;
    private Double doubleValue;
    private int periodEnum;
    private int metricEnum;
    private String itemId;
    private int itemType;
}
