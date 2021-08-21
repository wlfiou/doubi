package com.paascloud.provider.api.customer.service.factory;

import com.paascloud.core.web.api.R;
import com.paascloud.provider.api.customer.model.dto.CustomerDTO;
import com.paascloud.provider.api.customer.service.CustomerRpcService;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerFallbackFactory implements FallbackFactory<CustomerRpcService> {
    @Override
    public CustomerRpcService create(Throwable throwable) {
        return new CustomerRpcService() {
            @Override
            public R<CustomerDTO> getCustomer(long customerId, List<String> customerOptions) {
                return null;
            }

            @Override
            public R<Long> authorize(String token, String path) {
                return null;
            }
        };
    }
}
