package com.neverlaughtingboy.apisignatureverification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * List of current application API authorized caller information
 *
 * @author mengyuan.xiang
 * @date 2022/4/13
 */
@Component
@ConfigurationProperties(prefix = "api.authorized")
public class ApiAuthorization {

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }

    public List<AuthorizedClientInfo> getClientInfos() {
        return clientInfos;
    }

    public void setClientInfos(List<AuthorizedClientInfo> clientInfos) {
        this.clientInfos = clientInfos;
    }

    /**
     * disable check
     */
    private Boolean disable;

    /**
     * current application API authorization caller information
     */
    private List<AuthorizedClientInfo> clientInfos;
}
