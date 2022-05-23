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
     * list of current application API authorized caller information
     */
    @Resource
    private ApiAuthorization apiAuthorization;

    @Pointcut("@annotation(com.neverlaughtingboy.apisignatureverification.annotation.ApiSignCheck)")
    public void annotationPointCut() {

    }

    @Before(value = "annotationPointCut()")
    public void checkApiSign() {

        //global switch check, if disabled, the check will be skipped
        if (apiAuthorization.getDisable())
        {
            return;
        }

        //get requestAttributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new BusinessException("40002001","get ServletRequestAttributes fail");
        }

        //get httpServletRequest from requestAttributes
        HttpServletRequest request = attributes.getRequest();

        //get and check head contains necessary info
        Map<String,String> headers = RequestUtils.getHeaders(request);

        String accessKey = headers.get(RequestHeaderParamConstants.ACCESS_KEY);
        String timeStamp = headers.get(RequestHeaderParamConstants.TIME_STAMP);
        String signMethod = headers.get(RequestHeaderParamConstants.SIGN_METHOD);
        String sign = headers.get(RequestHeaderParamConstants.SIGN);
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(timeStamp) || StringUtils.isBlank(signMethod) || StringUtils.isBlank(sign)) {
            throw new BusinessException("40002002","header access-key,timestamp,sign-method,sign can not be blank");
        }

        //get accessSecret by accessKey
        List<AuthorizedClientInfo> authorizedClientInfos = apiAuthorization.getClientInfos();

        if (CollectionUtils.isEmpty(authorizedClientInfos)) {
            throw new BusinessException("40002003","no permission");
        }

        Optional<AuthorizedClientInfo> authorizedClientInfoOptional = authorizedClientInfos.stream().filter(t->t.getAccessKey().equals(accessKey)).findFirst();

        if (!authorizedClientInfoOptional.isPresent()) {
            throw new BusinessException("40002003","no permission");
        }

        AuthorizedClientInfo authorizedClientInfo = authorizedClientInfoOptional.get();
        String accessSecret = authorizedClientInfo.getAccessSecret();
        //compare and return signature verification results
        if (!checkSign(request,accessKey,accessSecret,request.getMethod().toUpperCase(),signMethod,timeStamp,sign)) {
            throw new BusinessException("40002008","sign error");
        }
    }

    /**
     * check sign
     * @param request request
     * @param accessKey accessKey
     * @param accessSecret accessSecret
     * @param httpMethod httpMethod
     * @param signMethod signMethod
     * @param timeStamp timeStamp
     * @param sign sign
     * @return check sign result
     */
    private boolean checkSign(HttpServletRequest request,String accessKey,String accessSecret,String httpMethod,String signMethod,String timeStamp,String sign) {
        // build sign content
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
                throw new BusinessException("40002004","get body params has an exception,exception message："+e.getMessage());
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
            log.error("create sign has an exception,exception message:"+e.getMessage());
            throw new BusinessException("40002007","create sign has an exception");
        }
        return StringUtils.equals(sign, signResult);
    }
}
