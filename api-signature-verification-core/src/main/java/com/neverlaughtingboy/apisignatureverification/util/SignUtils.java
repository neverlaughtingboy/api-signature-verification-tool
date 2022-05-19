package com.neverlaughtingboy.apisignatureverification.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.neverlaughtingboy.apisignatureverification.exception.BusinessException;

/**
 * Generate signature
 *
 * @author mengyuan.xiang
 * @date 2022/4/13
 */
public class SignUtils {

    public static String createSign(String type, String content) {
        switch (type) {
            case "sha256":
                return Hashing.sha256().hashString(content, Charsets.UTF_8).toString();
            default:
                throw new BusinessException("40002006", "not found the sign method signMethod=" + type);
        }
    }

}
