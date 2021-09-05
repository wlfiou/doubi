package com.x.provider.api.vod.model.ao;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetContentReviewResultAO {
    private String notifyUrl;
    private List<String> fileIds;
}
