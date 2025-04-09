
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

import java.util.List;

/**
 * Bean Post Processor Configuration for nacos console.
 *
 * @author xiweng.yy
 */
@Configuration
@ConditionalOnProperty(value = Constants.NACOS_DUPLICATE_BEAN_ENHANCEMENT_ENABLED, havingValue = "true", matchIfMissing = true)
@EnabledInnerHandler
public class NacosConsoleBeanPostProcessorConfiguration {


    private final NamespaceHandler namespaceHandler;

    public NamespaceProxy(NamespaceHandler namespaceHandler) {
        this.namespaceHandler = namespaceHandler;
    }

    /**
     * Get namespace list.
     */
    public List<Namespace> getNamespaceList() throws NacosException {
        return namespaceHandler.getNamespaceList();
    }

    /**
     * Get the specific namespace information.
     */
    public Namespace getNamespaceDetail(String namespaceId) throws NacosException {
        return namespaceHandler.getNamespaceDetail(namespaceId);
    }

    /**
     * Create or update namespace.
     */
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
            throws NacosException {
        return namespaceHandler.createNamespace(namespaceId, namespaceName, namespaceDesc);
    }

    /**
     * Edit namespace.
     */
    public Boolean updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        return namespaceHandler.updateNamespace(namespaceForm);
    }

    /**
     * Delete namespace.
     */
    public Boolean deleteNamespace(String namespaceId) throws NacosException {
        return namespaceHandler.deleteNamespace(namespaceId);
    }

    /**
     * Check if namespace exists.
     */
    public Boolean checkNamespaceIdExist(String namespaceId) throws NacosException {
        return namespaceHandler.checkNamespaceIdExist(namespaceId);
    }


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
