package com.neverlaughtingboy.apisignatureverification.util;

import com.neverlaughtingboy.apisignatureverification.constants.RequestHeaderParamConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * request utils
 *
 * @author mengyuan.xiang
 * @date 2022/4/13
 */
public class RequestUtils {

    private static final Logger log = LoggerFactory.getLogger(RequestUtils.class);

    /**
     * request header convert to map
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        String accessKey = request.getHeader(RequestHeaderParamConstants.ACCESS_KEY);
        String timeStamp = request.getHeader(RequestHeaderParamConstants.TIME_STAMP);
        String signMethod = request.getHeader(RequestHeaderParamConstants.SIGN_METHOD);
        String sign = request.getHeader(RequestHeaderParamConstants.SIGN);
        Map<String, String> header = new HashMap<>();
        header.put(RequestHeaderParamConstants.ACCESS_KEY, accessKey);
        header.put(RequestHeaderParamConstants.TIME_STAMP, timeStamp);
        header.put(RequestHeaderParamConstants.SIGN_METHOD, signMethod);
        header.put(RequestHeaderParamConstants.SIGN, sign);
        return header;
    }

    /**
     * get method param
     */
    public static String getQueryParams(HttpServletRequest request) {
        return request.getQueryString() != null ? request.getQueryString() : "";
    }

    /**
     * get post body
     */
    public static String getBodyParams(HttpServletRequest request) throws RuntimeException {
        StringBuilder data = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            String line;
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while (null != (line = reader.readLine())) {
                data.append(line);
            }
        } catch (Exception e) {
            log.error("get post body content has exception, error message: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (Exception ex) {
                log.error("close stream has exception, error message: {}", ex.getMessage());
            }
        }
        return data.toString();
    }

}
