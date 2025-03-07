
package com.baomidou.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.core.toolkit.AES;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.util.HashMap;

/**
 * 安全加密处理器
 *
 * @author hubin
 * @since 2020-05-23
 */
public class SafetyEncryptProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        /**
         * 命令行中获取密钥
         */
        String mpwKey = null;
        for (PropertySource<?> ps : environment.getPropertySources()) {
            if (ps instanceof SimpleCommandLinePropertySource) {
                SimpleCommandLinePropertySource source = (SimpleCommandLinePropertySource) ps;
                mpwKey = source.getProperty("mpw.key");
                break;
            }
        }
        /**
         * 处理加密内容
         */
        if (StringUtils.isNotBlank(mpwKey)) {
            HashMap<String, Object> map = new HashMap<>();
            for (PropertySource<?> ps : environment.getPropertySources()) {
                if (ps instanceof OriginTrackedMapPropertySource) {
                    OriginTrackedMapPropertySource source = (OriginTrackedMapPropertySource) ps;
                    for (String name : source.getPropertyNames()) {
                        Object value = source.getProperty(name);
                        if (value instanceof String) {
                            String str = (String) value;
                            if (str.startsWith("mpw:")) {
                                map.put(name, AES.decrypt(str.substring(4), mpwKey));
                            }
                        }
                    }
                }
            }
            // 将解密的数据放入环境变量，并处于第一优先级上
            if (CollectionUtils.isNotEmpty(map)) {
                environment.getPropertySources().addFirst(new MapPropertySource("custom-encrypt", map));
            }
        }
    }


    
    public static void setMenuVelocityContext(VelocityContext context, GenTable genTable)
    {
        String options = genTable.getOptions();
        JSONObject paramsObj = JSON.parseObject(options);
        String parentMenuId = getParentMenuId(paramsObj);
        context.put("parentMenuId", parentMenuId);
    }

    public static void setTreeVelocityContext(VelocityContext context, GenTable genTable)
    {
        String options = genTable.getOptions();
        JSONObject paramsObj = JSON.parseObject(options);
        String treeCode = getTreecode(paramsObj);
        String treeParentCode = getTreeParentCode(paramsObj);
        String treeName = getTreeName(paramsObj);

        context.put("treeCode", treeCode);
        context.put("treeParentCode", treeParentCode);
        context.put("treeName", treeName);
        context.put("expandColumn", getExpandColumn(genTable));
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE))
        {
            context.put("tree_parent_code", paramsObj.getString(GenConstants.TREE_PARENT_CODE));
        }
        if (paramsObj.containsKey(GenConstants.TREE_NAME))
        {
            context.put("tree_name", paramsObj.getString(GenConstants.TREE_NAME));
        }
    }

    public static void setSubVelocityContext(VelocityContext context, GenTable genTable)
    {
        GenTable subTable = genTable.getSubTable();
        String subTableName = genTable.getSubTableName();
        String subTableFkName = genTable.getSubTableFkName();
        String subClassName = genTable.getSubTable().getClassName();
        String subTableFkClassName = StringUtils.convertToCamelCase(subTableFkName);

        context.put("subTable", subTable);
        context.put("subTableName", subTableName);
        context.put("subTableFkName", subTableFkName);
        context.put("subTableFkClassName", subTableFkClassName);
        context.put("subTableFkclassName", StringUtils.uncapitalize(subTableFkClassName));
        context.put("subClassName", subClassName);
        context.put("subclassName", StringUtils.uncapitalize(subClassName));
        context.put("subImportList", getImportList(genTable.getSubTable()));
    }

    /**
     * 获取模板信息
     * @param tplCategory 生成的模板
     * @param tplWebType 前端类型
     * @return 模板列表
     */
    public static List<String> getTemplateList(String tplCategory, String tplWebType)
    {
        String useWebType = "vm/vue";
        if ("element-plus".equals(tplWebType))
        {
            useWebType = "vm/vue/v3";
        }
        List<String> templates = new ArrayList<String>();
        templates.add("vm/java/domain.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/controller.java.vm");
        templates.add("vm/xml/mapper.xml.vm");
        templates.add("vm/sql/sql.vm");
        templates.add("vm/js/api.js.vm");
        if (GenConstants.TPL_CRUD.equals(tplCategory))
        {
            templates.add(useWebType + "/index.vue.vm");
        }
        else if (GenConstants.TPL_TREE.equals(tplCategory))
        {
            templates.add(useWebType + "/index-tree.vue.vm");
        }
        else if (GenConstants.TPL_SUB.equals(tplCategory))
        {
            templates.add(useWebType + "/index.vue.vm");
            templates.add("vm/java/sub-domain.java.vm");
        }
        return templates;
    }


    
}
