package com.rabbitt.salesinsight.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Component
public class SalesParser {

    public SalesInsights parse(MultipartFile file) {
        try {
            String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
            if (filename.toLowerCase().endsWith(".csv")) {
                return parseCsv(file);
            } else {
                // For brevity, treat non-CSV as CSV with header.
                return parseCsv(file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse sales file", e);
        }
    }

    private SalesInsights parseCsv(MultipartFile file) throws Exception {
        Reader in = new InputStreamReader(file.getInputStream());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(in);

        int totalUnits = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Integer> unitsByRegion = new HashMap<>();
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        List<String> sampleRows = new ArrayList<>();

        for (CSVRecord r : records) {
            String dateStr = r.get("Date");
            String category = r.get("Product_Category");
            String region = r.get("Region");
            int units = Integer.parseInt(r.get("Units_Sold"));
            BigDecimal revenue = new BigDecimal(r.get("Revenue"));
            String status = r.get("Status");

            totalUnits += units;
            totalRevenue = totalRevenue.add(revenue);

            unitsByRegion.merge(region, units, Integer::sum);
            revenueByCategory.merge(category, revenue, BigDecimal::add);

            sampleRows.add(String.join(", ",
                    dateStr, category, region,
                    "Units=" + units,
                    "Revenue=" + revenue,
                    "Status=" + status
            ));
        }

        return new SalesInsights(totalUnits, totalRevenue, unitsByRegion, revenueByCategory, sampleRows);
    }
}

