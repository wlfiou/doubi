package com.x.provider.customer.controller.frontend;

import com.x.core.utils.ApiAssetUtil;
import com.x.core.web.api.R;
import com.x.core.web.api.ResultCode;
import com.x.core.web.controller.BaseFrontendController;
import com.x.core.web.page.TableDataInfo;
import com.x.provider.api.general.model.ao.SendVerificationCodeAO;
import com.x.provider.api.general.service.SmsRpcService;
import com.x.provider.api.oss.service.OssRpcService;
import com.x.provider.customer.enums.SystemCustomerAttributeName;
import com.x.provider.customer.model.ao.*;
import com.x.provider.customer.model.domain.Customer;
import com.x.provider.customer.model.domain.CustomerRelation;
import com.x.provider.customer.model.vo.CustomerHomePageVO;
import com.x.provider.customer.model.vo.FlowFansListItemVO;
import com.x.provider.customer.service.CustomerRelationService;
import com.x.provider.customer.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "用户服务")
@RestController
@RequestMapping("/frontend/customer")
public class CustomerController extends BaseFrontendController {

    private final CustomerService customerService;
    private final CustomerRelationService customerRelationService;
    private final OssRpcService ossRpcService;
    private final SmsRpcService smsRpcService;

    public CustomerController(CustomerService customerService,
                              CustomerRelationService customerRelationService,
                              OssRpcService ossRpcService,
                              SmsRpcService smsRpcService){
        this.customerService = customerService;
        this.customerRelationService = customerRelationService;
        this.ossRpcService = ossRpcService;
        this.smsRpcService = smsRpcService;
    }

    @ApiOperation(value = "用户名密码注册")
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Validated UserNamePasswordRegisterAO userNamePasswordRegisterAO){
        customerService.register(userNamePasswordRegisterAO);
        return R.ok();
    }

    @ApiOperation(value = "根据密码登陆,返回token,下次方法其它接口是在此Token至于http header Authorization 中，值为 Bear token")
    @PostMapping("/login/by/password")
    public R<String> loginByPassword(@RequestBody @Validated LoginByPasswordAO userNamePasswordLoginAO){
        return R.ok(customerService.loginByPassword(userNamePasswordLoginAO));
    }

    @ApiOperation(value = "根据短信验证码登陆或者注册, 返回token,下次方法其它接口是在此Token至于http header Authorization 中，值为 Bear token")
    @PostMapping("/login/register/by/sms")
    public R<String> loginOrRegisterBySms(@RequestBody @Validated LoginOrRegBySmsAO loginOrRegBySmsAO){
        return R.ok(customerService.loginOrRegisterBySms(loginOrRegBySmsAO));
    }

    @ApiOperation(value = "发送短信验证码")
    @PostMapping("/sms/verification/code/send")
    public R<Void> loginOrRegisterBySms(@RequestBody @Validated SendSmsVerificationCodeAO sendSmsVerificationCodeAO){
        return smsRpcService.sendVerificationCode(SendVerificationCodeAO.builder().phoneNumber(sendSmsVerificationCodeAO.getPhoneNumber()).build());
    }

    @ApiOperation(value = "注销")
    @PostMapping("/logout")
    public R<Void> logout(){
        customerService.logout(getBearAuthorizationToken());
        return R.ok();
    }

    @ApiOperation("验证手机是否被绑定且发验证码")
    @PostMapping("/phone/bind/validate")
    public R<Void> bindPhone(@RequestBody @ApiParam(value = "手机号码", required = true) ValidatePhoneAO validatePhoneAO) {
        customerService.checkPhoneBound(getCurrentCustomerId(), validatePhoneAO);
        return R.ok();
    }

    @ApiOperation("绑定手机")
    @PostMapping("/phone/bind")
    public R<Void> bindPhone(@RequestBody @ApiParam(value = "用户id", required = true) BindPhoneAO bindPhoneAO) {
        customerService.bindPhone(getCurrentCustomerId(), bindPhoneAO);
        return R.ok();
    }

    @ApiOperation(value = "修改密码")
    @PostMapping("/password/change")
    public R<Void> changePassword(@RequestBody @Validated ChangePasswordByOldPasswordAO changePasswordByOldPasswordAO){
        customerService.changePassword(getCurrentCustomerId(), changePasswordByOldPasswordAO);
        return R.ok();
    }

    @ApiOperation(value = "更改手机号码")
    @PostMapping("/phone/change")
    public R<Void> changePhone(@RequestBody @Validated ChangePhoneAO changePhoneAO){
        customerService.changePhone(getCurrentCustomerId(), changePhoneAO);
        return R.ok();
    }

    @ApiOperation(value = "修改用户名")
    @PostMapping("/user/name/change")
    public R<Void> changeUserName(@RequestBody @Validated ChangeUserNameAO changeUserNameAO){
        customerService.changeUserName(getCurrentCustomerId(), changeUserNameAO);
        return R.ok();
    }

    @ApiOperation(value = "设置用户属性")
    @PostMapping("/attribute/set")
    public R<Void> setCustomerAttribute(@RequestBody @Validated SetCustomerAttributeAO setCustomerAttribute){
        customerService.setCustomerDraftAttribute(getCurrentCustomerId(), SystemCustomerAttributeName.valueOf(setCustomerAttribute.getAttributeName()), setCustomerAttribute.getValue());
        return R.ok();
    }

    @ApiOperation(value = "个人主页信息")
    @GetMapping("/homepage")
    public R<CustomerHomePageVO> getCustomerHomePage(@RequestParam(required = false) @ApiParam(value = "用户id") Long customerId){
        if (customerId <= 0){
            customerId = getCurrentCustomerIdAndNotCheckLogin();
        }
        Customer customer = customerService.getCustomer(customerId);
        ApiAssetUtil.isTrue(!customer.isSystemAccount(), ResultCode.FORBIDDEN);
        CustomerHomePageVO customerHomePage = new CustomerHomePageVO();
        BeanUtils.copyProperties(customer, customerHomePage);
        Map<String, String> customerAttribute = customerService.listCustomerAttribute(customerId);
        prepareCustomerAttribute(customerAttribute);
        customerHomePage.setAttributes(customerAttribute);
        customerHomePage.setFansCount(customerRelationService.getFansCount(customerId));
        customerHomePage.setFansCount(customerRelationService.getFollowCount(customerId));
        return R.ok(customerHomePage);
    }

    @ApiOperation(value = "获取我与toCustomerId的关系 0 没有关系 1 关注关系 2 朋友关系")
    @GetMapping("/relation")
    public R<Integer> getRelation(@RequestParam @Validated @Min(1) @ApiParam(value = "用户id") long toCustomerId){
        return R.ok(customerRelationService.getRelation(getCurrentCustomerId(), toCustomerId).getValue());
    }

    @ApiOperation(value = "关注")
    @PostMapping("/relation/following")
    public R<Void> following(@RequestParam @Validated @Min(1) @ApiParam(value = "用户id") long toCustomerId){
        customerRelationService.following(getCurrentCustomerId(), toCustomerId);
        return R.ok();
    }

    @ApiOperation(value = "取消关注")
    @PostMapping("/relation/unfollowing")
    public R<Void> unFollowing(@RequestParam @Validated @Min(1) @ApiParam(value = "用户id") long toCustomerId){
        customerRelationService.unFollowing(getCurrentCustomerId(), toCustomerId);
        return R.ok();
    }

    @ApiOperation(value = "粉丝个数")
    @GetMapping("/relation/fans/count")
    public R<Long> getFansCount(@RequestParam @Validated @Min(1)  @ApiParam(value = "用户id") long customerId){
        return R.ok(customerRelationService.getFansCount(customerId));
    }

    @ApiOperation(value = "关注的人的个数")
    @GetMapping("/relation/follow/count")
    public R<Long> getFollowCount(@RequestParam @Validated @Min(1)  @ApiParam(value = "用户id") long customerId){
        return R.ok(customerRelationService.getFollowCount(customerId));
    }

    @ApiOperation(value = "查询关注列表")
    @GetMapping("/relation/follows")
    public R<TableDataInfo<FlowFansListItemVO>> listFollow(@RequestParam int page, @RequestParam @Validated @Min(1)  @ApiParam(value = "用户id") long customerId){
        List<CustomerRelation> follows = customerRelationService.listFollow(customerId, getPage(), getDefaultFrontendPageSize());
        List<FlowFansListItemVO> result = prepareFollowFansListIem(true, follows);
        return R.ok(new TableDataInfo<>(result, 0, getDefaultFrontendPageSize()));
    }

    @ApiOperation(value = "查询粉丝列表")
    @GetMapping("/relation/fans")
    public R<TableDataInfo<FlowFansListItemVO>> listFans(@RequestParam int page, @RequestParam @Validated @Min(1)  @ApiParam(value = "用户id") long customerId){
        List<CustomerRelation> fans = customerRelationService.listFans(customerId, getPage(), getDefaultFrontendPageSize());
        List<FlowFansListItemVO> result = prepareFollowFansListIem(false, fans);
        return R.ok(new TableDataInfo<>(result, 0, getDefaultFrontendPageSize()));
    }

    private List<FlowFansListItemVO> prepareFollowFansListIem(boolean follow,  List<CustomerRelation> customers) {
        List<FlowFansListItemVO> result = new ArrayList<>(customers.size());
        customers.forEach(item -> {
            long customerId = follow ? item.getToCustomerId() : item.getFromCustomerId();
            FlowFansListItemVO flowFansListItem = new FlowFansListItemVO();
            flowFansListItem.setCustomerId(customerId);
            flowFansListItem.setCustomerAttributes(customerService.listCustomerAttribute(customerId, Arrays.asList(SystemCustomerAttributeName.AVATAR_ID, SystemCustomerAttributeName.NICK_NAME)));
            result.add(flowFansListItem);
        });
        List<String> objectKeys = new ArrayList<>();
        result.stream().forEach(item -> {
            item.getCustomerAttributes().entrySet().stream().forEach(attribute -> {
                if(CustomerService.MEDIA_CUSTOMER_ATTRIBUTE_NAME.contains(attribute.getKey())){
                    objectKeys.add(attribute.getValue());
                }
            });
        });
        if (objectKeys.size() > 0) {
            final R<Map<String, String>> attributeUrls = ossRpcService.listObjectBrowseUrl(objectKeys);
            result.stream().forEach(item -> {
                item.getCustomerAttributes().entrySet().stream().forEach(attribute -> {
                    if (CustomerService.MEDIA_CUSTOMER_ATTRIBUTE_NAME.contains(attribute.getKey())) {
                        attribute.setValue(attributeUrls.getData().getOrDefault(attribute.getValue(), Strings.EMPTY));
                    }
                });
            });
        }
        return result;

    }

    private void prepareCustomerAttribute(Map<String, String> attributes){
        List<String> allMediaCustomerAttributeNames = CustomerService.MEDIA_CUSTOMER_ATTRIBUTE_NAME;
        Map<String, String> mediaAttribute = attributes.entrySet().stream().filter(item -> allMediaCustomerAttributeNames.contains(item.getKey()))
                .collect(Collectors.toMap(item -> item.getKey(), item -> item.getValue()));
        if (CollectionUtils.isEmpty(mediaAttribute)){
            return;
        }
        final R<Map<String, String>> attributeUrls = ossRpcService.listObjectBrowseUrl(mediaAttribute.values().stream().collect(Collectors.toList()));
        attributes.entrySet().stream().forEach(item -> {
            if (allMediaCustomerAttributeNames.contains(item.getKey())){
                item.setValue(attributeUrls.getData().getOrDefault(item.getValue(), Strings.EMPTY));
            }
        });
    }
}
