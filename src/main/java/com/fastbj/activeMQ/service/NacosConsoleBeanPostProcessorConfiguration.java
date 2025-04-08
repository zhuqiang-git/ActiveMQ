
package com.fastbj.activeMQ.service;

import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.NacosDuplicateConfigurationBeanPostProcessor;
import com.alibaba.nacos.sys.env.NacosDuplicateSpringBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean Post Processor Configuration for nacos console.
 *
 * @author xiweng.yy
 */
@Configuration
@ConditionalOnProperty(value = Constants.NACOS_DUPLICATE_BEAN_ENHANCEMENT_ENABLED, havingValue = "true", matchIfMissing = true)
@EnabledInnerHandler
public class NacosConsoleBeanPostProcessorConfiguration {
    
    @Bean
    public InstantiationAwareBeanPostProcessor nacosDuplicateSpringBeanPostProcessor(
            ConfigurableApplicationContext context) {
        return new NacosDuplicateSpringBeanPostProcessor(context);
    }
    
    @Bean
    public InstantiationAwareBeanPostProcessor nacosDuplicateConfigurationBeanPostProcessor(
            ConfigurableApplicationContext context) {
        return new NacosDuplicateConfigurationBeanPostProcessor(context);
    }
}
