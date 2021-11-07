package com.x.provider.general.controller.rpc;

import com.x.core.web.api.IErrorCode;
import com.x.core.web.api.R;
import com.x.provider.api.general.model.ao.SendVerificationCodeAO;
import com.x.provider.api.general.model.ao.ValidateVerificationCodeAO;
import com.x.provider.api.general.service.SmsRpcService;
import com.x.provider.general.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rpc/general/sms")
public class SmsRpcController implements SmsRpcService {

    private final SmsService smsService;
    public SmsRpcController(SmsService smsService){
        this.smsService = smsService;
    }

    @PostMapping("/verification/code/send")
    @Override
    public R<Void> sendVerificationCode( @RequestBody SendVerificationCodeAO sendVerificationCodeAO) {
        smsService.sendVerificationCode(sendVerificationCodeAO.getPhoneNumber());
        return R.ok();
    }

    @PostMapping("/verification/code/validate")
    @Override
    public R<Void> validateVerificationCode(@RequestBody ValidateVerificationCodeAO validateVerificationCodeAO) {
        smsService.validateVerificationCode(validateVerificationCodeAO.getPhoneNumber(), validateVerificationCodeAO.getSms());
        return R.ok();
    }
}
