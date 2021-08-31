package com.x.provider.api.finance.service.factory;

import com.x.core.web.api.R;
import com.x.provider.api.finance.model.ao.ListIndustryAO;
import com.x.provider.api.finance.model.ao.ListSecurityAO;
import com.x.provider.api.finance.model.dto.IndustryDTO;
import com.x.provider.api.finance.model.dto.SecurityDTO;
import com.x.provider.api.finance.service.FinanceRpcService;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinianceFallbackFactory implements FallbackFactory<FinanceRpcService> {

    @Override
    public FinanceRpcService create(Throwable throwable) {
        return new FinanceRpcService() {
            @Override
            public List<SecurityDTO> listSecurity(ListSecurityAO listSecurityAO) {
                return null;
            }

            @Override
            public List<IndustryDTO> listIndustry(ListIndustryAO listIndustryAO) {
                return null;
            }
        };
    }
}
