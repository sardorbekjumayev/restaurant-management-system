package com.example.restaurantmanagementsystem.Model.Tables;

import java.util.ArrayList;
import java.util.List;

public class TableChart {
    private int branchId;
    private final List<Table> tables = new ArrayList<>();

    public TableChart() {
    }

    public TableChart(int branchId) {
        this.branchId = branchId;
    }

    public int getBranchId() {
        return branchId;
    }

    public List<Table> getTables() {
        return tables;
    }

    public boolean addTable(Table table) {
        return tables.add(table);
    }
}
