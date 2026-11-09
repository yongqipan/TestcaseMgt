package com.pyq.tcm.ai.dto;

import java.util.List;

public class AcceptRequest {

    private List<Long> itemIds;

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
