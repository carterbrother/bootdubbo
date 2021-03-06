package com.springx.bootdubbo.starter.rest.config;

import com.springx.bootdubbo.starter.rest.core.ResponseBodyAndExceptionHandleBean;
import com.springx.bootdubbo.starter.rest.core.ResponseConfigBean;
import com.springx.bootdubbo.starter.rest.core.RestContextInterceptorBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author <a href="mailto:505847426@qq.com">carterbrother</a>
 * @description 自动配置Rest相关，配置拦截器，正常返回预定格式的json数据；
 * 异常也返回预定格式的json数据
 * @date 2019年05月22日 1:56 PM
 * @Copyright (c) carterbrother
 */
@Configuration
@EnableConfigurationProperties(RestPropertiesConfig.class)
@Slf4j
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@ConditionalOnProperty(prefix = "rest.config", value = "enabled", havingValue = "true")
public class RestAutoConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Resource
    private RestPropertiesConfig restPropertiesConfig;

    @Bean
    public ResponseBodyAndExceptionHandleBean exceptionHandlerBean(){
        log.info("===>install exception handler AND response wrapper ");
        applicationContext.getBean(DispatcherServlet.class).setThrowExceptionIfNoHandlerFound(true);
        return new ResponseBodyAndExceptionHandleBean();
    }

    @Bean
    public ResponseConfigBean responseConfigBean(){
        log.info("===>install httpMessage converter ");
        return new ResponseConfigBean(applicationContext,restPropertiesConfig);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        log.info("===>install interceptor");
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                String[] excludePathPatterns = new String[]{"/**/fonts/*", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.gif", "/**/*.jpg", "/**/*.jpeg", "/error"};

                registry.addInterceptor(new RestContextInterceptorBean(restPropertiesConfig))
                        .addPathPatterns("/**")
                        .excludePathPatterns(excludePathPatterns)
                        .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
                ;
            }
            @Override
            public void  addCorsMappings(CorsRegistry registry)
            {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                        .maxAge(3600)
                        .allowCredentials(true);
            }
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
            }
        };
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
