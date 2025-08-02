package com.example.saas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures resource handling for the Rqueue dashboard.  Renamed to avoid
 * conflicting with the internal Rqueue configuration bean.  The dashboard
 * serves static assets from the classpath under {@code /public/}.  When
 * a path prefix is configured via {@code rqueue.web.url.prefix}, the
 * resource handler is adjusted accordingly【997698871576688†L50-L83】.
 */
@Configuration
public class RqueueResourceConfigurer implements WebMvcConfigurer {

    @Value("${rqueue.web.url.prefix:}")
    private String rqueueWebUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (StringUtils.hasText(rqueueWebUrlPrefix)) {
            registry
                    .addResourceHandler(rqueueWebUrlPrefix + "/**")
                    .addResourceLocations("classpath:/public/");
        } else if (!registry.hasMappingForPattern("/**")) {
            registry
                    .addResourceHandler("/**")
                    .addResourceLocations("classpath:/public/");
        }
    }
}