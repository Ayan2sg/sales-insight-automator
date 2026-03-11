package com.rabbitt.salesinsight.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesInsights {

    private final int totalUnits;
    private final BigDecimal totalRevenue;
    private final Map<String, Integer> unitsByRegion;
    private final Map<String, BigDecimal> revenueByCategory;
    private final List<String> sampleRows;

    public SalesInsights(int totalUnits,
                         BigDecimal totalRevenue,
                         Map<String, Integer> unitsByRegion,
                         Map<String, BigDecimal> revenueByCategory,
                         List<String> sampleRows) {
        this.totalUnits = totalUnits;
        this.totalRevenue = totalRevenue;
        this.unitsByRegion = unitsByRegion;
        this.revenueByCategory = revenueByCategory;
        this.sampleRows = sampleRows;
    }

    public String toSummaryString() {
        String regions = unitsByRegion.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue() + " units")
                .collect(Collectors.joining("; "));
        String categories = revenueByCategory.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue() + " revenue")
                .collect(Collectors.joining("; "));

        return """
                Total units sold: %d
                Total revenue: %s
                Units by region: %s
                Revenue by category: %s
                """.formatted(totalUnits, totalRevenue, regions, categories);
    }

    public String sampleRowsAsString(int limit) {
        return sampleRows.stream().limit(limit).collect(Collectors.joining("\n"));
    }
}

