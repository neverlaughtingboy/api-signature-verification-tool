package com.neverlaughtingboy.apisignatureverification.annotation;

import java.lang.annotation.*;

/**
 * api sign check annotation
 *
 * @author mengyuan.xiang
 * @date 2022/4/12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSignCheck {
}
