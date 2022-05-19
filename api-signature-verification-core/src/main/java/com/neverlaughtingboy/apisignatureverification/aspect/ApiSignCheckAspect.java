package com.neverlaughtingboy.apisignatureverification.aspect;

import com.neverlaughtingboy.apisignatureverification.config.AuthorizedClientInfo;
import com.neverlaughtingboy.apisignatureverification.config.ApiAuthorization;
import com.neverlaughtingboy.apisignatureverification.constants.CommonConstants;
import com.neverlaughtingboy.apisignatureverification.constants.RequestHeaderParamConstants;
import com.neverlaughtingboy.apisignatureverification.exception.BusinessException;
import com.neverlaughtingboy.apisignatureverification.util.RequestUtils;
import com.neverlaughtingboy.apisignatureverification.util.SignUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * api aspect core logic
 *
 * @author mengyuan.xiang
 * @date 2022/4/12
 */
@Component
@Aspect
public class ApiSignCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiSignCheckAspect.class);

    /**
     * List of current application API authorized caller information
     */
    @Resource
    private ApiAuthorization apiAuthorization;

    @Pointcut("@annotation(com.neverlaughtingboy.apisignatureverification.annotation.ApiSignCheck)")
    public void annotationPointCut() {

    }

    @Before(value = "annotationPointCut()")
    public void checkApiSign() {

        //全局开关检查，禁用开启就跳过校验
        if (apiAuthorization.getDisable())
        {
            return;
        }

        //获取RequestAttributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new BusinessException("40002001","get ServletRequestAttributes fail");
        }

        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = attributes.getRequest();

        //获取并校验head是否包含必要信息
        Map<String,String> headers = RequestUtils.getHeaders(request);

        String accessKey = headers.get(RequestHeaderParamConstants.ACCESS_KEY);
        String timeStamp = headers.get(RequestHeaderParamConstants.TIME_STAMP);
        String signMethod = headers.get(RequestHeaderParamConstants.SIGN_METHOD);
        String sign = headers.get(RequestHeaderParamConstants.SIGN);
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(timeStamp) || StringUtils.isBlank(signMethod) || StringUtils.isBlank(sign)) {
            throw new BusinessException("40002002","header db-access-key,db-timestamp,db-sign-method,db-sign不能为空");
        }

        //获取accessKey对应的accessSecret
        List<AuthorizedClientInfo> authorizedClientInfos = apiAuthorization.getClientInfos();

        if (CollectionUtils.isEmpty(authorizedClientInfos)) {
            throw new BusinessException("40002003","调用权限不足");
        }

        Optional<AuthorizedClientInfo> authorizedClientInfoOptional = authorizedClientInfos.stream().filter(t->t.getAccessKey().equals(accessKey)).findFirst();

        if (!authorizedClientInfoOptional.isPresent()) {
            throw new BusinessException("40002003","调用权限不足");
        }

        AuthorizedClientInfo authorizedClientInfo = authorizedClientInfoOptional.get();
        String accessSecret = authorizedClientInfo.getAccessSecret();
        //比较并返回验签结果
        if (!checkSign(request,accessKey,accessSecret,request.getMethod().toUpperCase(),signMethod,timeStamp,sign)) {
            throw new BusinessException("40002008","签名错误");
        }
    }

    /**
     * 校验签名
     * @param request 请求体
     * @param accessKey accessKey
     * @param accessSecret accessSecret
     * @param httpMethod 请求方式
     * @param signMethod 签名方式
     * @param timeStamp 时间戳
     * @param sign 签名
     * @return 验签结果
     */
    private boolean checkSign(HttpServletRequest request,String accessKey,String accessSecret,String httpMethod,String signMethod,String timeStamp,String sign) {
        // 验证签名
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(signMethod);
        stringBuilder.append(httpMethod);
        stringBuilder.append(accessKey);
        stringBuilder.append(timeStamp);
        stringBuilder.append(request.getRequestURI().replace("/", "."));
        if (CommonConstants.HTTP_METHOD_GET.equals(httpMethod)) {
            stringBuilder.append(RequestUtils.getQueryParams(request));
        }
        else if (CommonConstants.HTTP_METHOD_POST.equals(httpMethod)) {
            try {
                stringBuilder.append(RequestUtils.getBodyParams(request));
            }
            catch (RuntimeException e) {
                throw new BusinessException("40002004","获取body参数出现异常，异常信息："+e.getMessage());
            }
        }
        else {
            throw new BusinessException("40002005",httpMethod+" not support");
        }

        stringBuilder.append(accessSecret);
        String signResult;
        try {
            signResult = SignUtils.createSign(signMethod,stringBuilder.toString());
        }catch (Exception e){
            log.error("签名异常信息："+e.getMessage());
            throw new BusinessException("40002007","签名出现异常");
        }
        return StringUtils.equals(sign, signResult);
    }
}
