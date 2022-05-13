package com.x.provider.api.general.model.ao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListStarAO {
    private  long starCustomerId;
    private int itemType;
    private List<Long> itemIdList;
}
