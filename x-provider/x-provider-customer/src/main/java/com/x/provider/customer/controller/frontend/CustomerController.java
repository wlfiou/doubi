package com.x.provider.customer.controller.frontend;

import com.x.core.constant.Constants;
import com.x.core.utils.ApiAssetUtil;
import com.x.core.utils.BeanUtil;
import com.x.core.web.api.R;
import com.x.core.web.api.ResultCode;
import com.x.core.web.controller.BaseFrontendController;
import com.x.core.web.page.PageList;
import com.x.provider.api.customer.enums.CustomerOptions;
import com.x.provider.api.customer.enums.CustomerRelationEnum;
import com.x.provider.api.customer.model.ao.ListSimpleCustomerAO;
import com.x.provider.api.customer.model.dto.SimpleCustomerDTO;
import com.x.provider.api.general.model.ao.SendVerificationCodeAO;
import com.x.provider.api.general.service.SmsRpcService;
import com.x.provider.api.oss.service.OssRpcService;
import com.x.provider.customer.enums.SystemCustomerAttributeName;
import com.x.provider.customer.model.ao.*;
import com.x.provider.customer.model.domain.Customer;
import com.x.provider.customer.model.domain.CustomerRelation;
import com.x.provider.customer.model.domain.CustomerStat;
import com.x.provider.customer.model.vo.CustomerHomePageVO;
import com.x.provider.customer.model.vo.CustomerStatVO;
import com.x.provider.customer.model.vo.SimpleCustomerVO;
import com.x.provider.customer.service.AuthenticationService;
import com.x.provider.customer.service.CustomerRelationService;
import com.x.provider.customer.service.CustomerService;
import com.x.provider.customer.service.CustomerStatService;
import com.x.provider.customer.service.impl.authext.ExternalAuthEngine;
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
    private final CustomerStatService customerStatService;
    private final AuthenticationService authenticationService;
    private final ExternalAuthEngine externalAuthEngine;

    public CustomerController(CustomerService customerService,
                              CustomerRelationService customerRelationService,
                              OssRpcService ossRpcService,
                              SmsRpcService smsRpcService,
                              CustomerStatService customerStatService,
                              AuthenticationService authenticationService,
                              ExternalAuthEngine externalAuthEngine){
        this.customerService = customerService;
        this.customerRelationService = customerRelationService;
        this.ossRpcService = ossRpcService;
        this.smsRpcService = smsRpcService;
        this.customerStatService = customerStatService;
        this.authenticationService = authenticationService;
        this.externalAuthEngine = externalAuthEngine;
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

    @ApiOperation(value = "第三方登陆，目前支持微信小程序, 返回token,下次方法其它接口是在此Token至于http header Authorization 中，值为 Bear token")
    @PostMapping("/login/external")
    public R<String> loginExternal(@RequestBody @Validated ExternalAuthenticationAO externalAuthenticationAO){
        return R.ok(externalAuthEngine.authenticate(externalAuthenticationAO));
    }

    @ApiOperation(value = "注销")
    @PostMapping("/logout")
    public R<Void> logout(){
        authenticationService.signOut();
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
        if (customerId == null || customerId <= 0){
            customerId = getCurrentCustomerIdAndNotCheckLogin();
        }
        Customer customer = customerService.getCustomer(customerId);
        CustomerHomePageVO customerHomePage = new CustomerHomePageVO();
        BeanUtils.copyProperties(customer, customerHomePage);
        Map<String, String> customerAttribute = customerService.listCustomerAttribute(customerId);
        prepareCustomerAttribute(customerAttribute);
        customerHomePage.setAttributes(customerAttribute);
        Map<Long, CustomerStat> customerStatMap = customerStatService.list(Arrays.asList(customerId));
        customerHomePage.setStatistic(BeanUtil.prepare(customerStatMap.getOrDefault(customerId, CustomerStat.builder().id(customerId).build()), CustomerStatVO.class));
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
    public R<PageList<SimpleCustomerVO>> listFollow(@RequestParam Long cursor,
                                                    @RequestParam Integer pageSize,
                                                    @RequestParam @Validated @Min(0)  @ApiParam(value = "用户id") long customerId){
        PageList<CustomerRelation> follows = customerRelationService.listFollow(customerId, getPageDomain());
        if (follows.getList().isEmpty()){
            return R.ok(new PageList<>());
        }
        List<SimpleCustomerVO> followsCustomer = prepareRelation(customerId, CustomerRelationEnum.FOLLOW, follows.getList());
        return R.ok(PageList.map(follows, followsCustomer));
    }

    @ApiOperation(value = "查询粉丝列表")
    @GetMapping("/relation/fans")
    public R<PageList<SimpleCustomerVO>> listFans(@RequestParam Long cursor, @RequestParam Integer pageSize, @RequestParam @Validated @Min(1)  @ApiParam(value = "用户id") long customerId){
        PageList<CustomerRelation> fans = customerRelationService.listFans(customerId, getPageDomain());
        if (fans.getList().isEmpty()){
            return R.ok(new PageList<>());
        }
        List<SimpleCustomerVO> followsCustomer = prepareRelation(customerId, CustomerRelationEnum.FOLLOW, fans.getList());
        return R.ok(PageList.map(fans, followsCustomer));
    }

    private List<SimpleCustomerVO> prepareRelation(long customerId, CustomerRelationEnum relation, List<CustomerRelation> customerRelations) {
        Map<Long, CustomerRelation> customerRelationMap = CustomerRelationEnum.FOLLOW.getValue() == relation.getValue() ? customerRelations.stream().collect(Collectors.toMap(CustomerRelation::getToCustomerId, item -> item)) :
                customerRelations.stream().collect(Collectors.toMap(CustomerRelation::getFromCustomerId, item -> item));
        List<Long> customerIdList = CustomerRelationEnum.FOLLOW.getValue() == relation.getValue() ? customerRelations.stream().map(CustomerRelation::getToCustomerId).collect(Collectors.toList()) :
                customerRelations.stream().map(CustomerRelation::getFromCustomerId).collect(Collectors.toList());
        Map<Long, SimpleCustomerDTO> customers = customerService.listCustomer(ListSimpleCustomerAO.builder()
                .customerIds(new ArrayList<>(customerRelationMap.keySet()))
                .customerRelation(relation.getValue()).loginCustomerId(customerId).customerOptions(Arrays.asList(CustomerOptions.CUSTOMER_RELATION.name()))
                .build());
        customers.entrySet().forEach(item ->{
            if (customerRelationMap.containsKey(item.getKey())) {
                item.getValue().setRelation(customerRelationMap.get(item.getKey()).getRelation());
            }
        });
        List<SimpleCustomerVO> result = new ArrayList<>(customerIdList.size());
        customerIdList.forEach(item ->{
            result.add(BeanUtil.prepare(customers.get(item), SimpleCustomerVO.class));
        });
        return result;
    }

    private void prepareCustomerAttribute(Map<String, String> attributes){
        List<String> allMediaCustomerAttributeNames = new ArrayList<>();
        allMediaCustomerAttributeNames.addAll(CustomerService.MEDIA_CUSTOMER_ATTRIBUTE_NAME);
        CustomerService.MEDIA_CUSTOMER_ATTRIBUTE_NAME.forEach(item -> {
            allMediaCustomerAttributeNames.add(Constants.getDraftAttributeName(item));
        });
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
