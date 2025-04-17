package depend;


import com.jd.platform.async.callback.ICallback;
import com.jd.platform.async.callback.IWorker;
import com.jd.platform.async.worker.WorkResult;
import com.jd.platform.async.wrapper.WorkerWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wuweifeng wrote on 2019-11-20.
 */
public class DeWorker2 implements IWorker<WorkResult<User>, String>, ICallback<WorkResult<User>, String> {



    @Autowired
    private SwitchAndOptions switches;

    private CmdbService cmdbService;

    private final Collection<CmdbService> services = NacosServiceLoader.load(CmdbService.class);

    private Map<String, Map<String, Entity>> entityMap = new ConcurrentHashMap<>();

    private Map<String, Label> labelMap = new ConcurrentHashMap<>();

    private Set<String> entityTypeSet = new HashSet<>();

    private long eventTimestamp = System.currentTimeMillis();

    public CmdbProvider() throws NacosException {
    }

    private void initCmdbService() throws NacosException {
        Iterator<CmdbService> iterator = services.iterator();
        if (iterator.hasNext()) {
            cmdbService = iterator.next();
        }

        if (cmdbService == null && switches.isLoadDataAtStart()) {
            throw new NacosException(NacosException.SERVER_ERROR, "Cannot initialize CmdbService!");
        }
    }

    /**
     * load data.
     */
    public void load() {

        if (!switches.isLoadDataAtStart()) {
            return;
        }

        // init label map:
        Set<String> labelNames = cmdbService.getLabelNames();
        if (labelNames == null || labelNames.isEmpty()) {
            Loggers.MAIN.warn("[LOAD] init label names failed!");
        } else {
            for (String labelName : labelNames) {
                // If get null label, it's still ok. We will try it later when we meet this label:
                labelMap.put(labelName, cmdbService.getLabel(labelName));
            }
        }

        // init entity type set:
        entityTypeSet = cmdbService.getEntityTypes();

        // init entity map:
        entityMap = cmdbService.getAllEntities();
    }



    private static final int SHOW_CONTENT_SIZE = 100;

    /**
     * Verify increment pub content.
     *
     * @param content content
     * @throws IllegalArgumentException if content is not valid
     */
    public static void verifyIncrementPubContent(String content) {

        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("publish/delete content can not be null");
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\r' || c == '\n') {
                throw new IllegalArgumentException("publish/delete content can not contain return and linefeed");
            }
            if (c == Constants.WORD_SEPARATOR.charAt(0)) {
                throw new IllegalArgumentException("publish/delete content can not contain(char)2");
            }
        }
    }

    public static String getContentIdentity(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("content does not contain separator");
        }
        return content.substring(0, index);
    }

    public static String getContent(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("content does not contain separator");
        }
        return content.substring(index + 1);
    }

    /**
     * Truncate content.
     *
     * @param content content
     * @return truncated content
     */
    public static String truncateContent(String content) {
        if (content == null) {
            return "";
        } else if (content.length() <= SHOW_CONTENT_SIZE) {
            return content;
        } else {
            return content.substring(0, SHOW_CONTENT_SIZE) + "...";
        }
    }


    /**
     * Init, called by spring.
     *
     * @throws NacosException nacos exception
     */
    @PostConstruct
    public void init() throws NacosException {

        initCmdbService();
        load();

        CmdbExecutor.scheduleCmdbTask(new CmdbDumpTask(), switches.getDumpTaskInterval(), TimeUnit.SECONDS);
        CmdbExecutor.scheduleCmdbTask(new CmdbLabelTask(), switches.getLabelTaskInterval(), TimeUnit.SECONDS);
        CmdbExecutor.scheduleCmdbTask(new CmdbEventTask(), switches.getEventTaskInterval(), TimeUnit.SECONDS);
    }

    @Override
    public Entity queryEntity(String entityName, String entityType) {
        if (!entityMap.containsKey(entityType)) {
            return null;
        }
        return entityMap.get(entityType).get(entityName);
    }


    @Override
    public String action(WorkResult<User> result, Map<String, WorkerWrapper> allWrappers) {
        System.out.println("par2的入参来自于par1： " + result.getResult());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.getResult().getName();
    }


    @Override
    public String defaultValue() {
        return "default";
    }

    @Override
    public void begin() {
        //System.out.println(Thread.currentThread().getName() + "- start --" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, WorkResult<User> param, WorkResult<String> workResult) {
        System.out.println("worker2 的结果是：" + workResult.getResult());
    }

      protected final Object[][] getContents() {
        Object[][] var1 = new Object[][]{{"CalendarData", "aa-DJ aa-ER aa-ET af-NA af-ZA agq-CM ak-GH am-ET ar-AE ar-BH ar-DZ ar-EG ar-IQ ar-JO ar-KW ar-LB ar-LY ar-MA ar-OM ar-QA ar-SA ar-SD ar-SY ar-TN ar-YE as-IN asa-TZ az-Cyrl-AZ az-Latn-AZ bas-CM be-BY bem-ZM bez-TZ bg-BG bm-ML bn-BD bn-IN bo-CN bo-IN br-FR brx-IN bs-BA byn-ER ca-ES cgg-UG chr-US cs-CZ cy-GB da-DK dav-KE de-AT de-BE de-CH de-DE de-LI de-LU dje-NE dua-CM dyo-SN dz-BT ebu-KE ee-GH ee-TG el-CY el-GR en-AS en-AU en-BB en-BE en-BM en-BW en-BZ en-CA en-Dsrt-US en-GB en-GU en-GY en-HK en-IE en-IN en-JM en-MH en-MP en-MT en-MU en-NA en-NZ en-PH en-PK en-SG en-TT en-UM en-US en-US-POSIX en-VI en-ZA en-ZW es-AR es-BO es-CL es-CO es-CR es-DO es-EC es-ES es-GQ es-GT es-HN es-MX es-NI es-PA es-PE es-PR es-PY es-SV es-US es-UY es-VE et-EE eu-ES ewo-CM fa-AF fa-IR ff-SN fi-FI fil-PH fo-FO fr-BE fr-BF fr-BI fr-BJ fr-BL fr-CA fr-CD fr-CF fr-CG fr-CH fr-CI fr-CM fr-DJ fr-FR fr-GA fr-GF fr-GN fr-GP fr-GQ fr-KM fr-LU fr-MC fr-MF fr-MG fr-ML fr-MQ fr-NE fr-RE fr-RW fr-SN fr-TD fr-TG fr-YT fur-IT ga-IE gd-GB gl-ES gsw-CH gu-IN guz-KE gv-GB ha-Latn-GH ha-Latn-NE ha-Latn-NG haw-US he-IL hi-IN hr-HR hu-HU hy-AM id-ID ig-NG ii-CN is-IS it-CH it-IT ja-JP jmc-TZ ka-GE kab-DZ kam-KE kde-TZ kea-CV khq-ML ki-KE kk-Cyrl-KZ kl-GL kln-KE km-KH kn-IN ko-KR kok-IN ksb-TZ ksf-CM ksh-DE kw-GB lag-TZ lg-UG ln-CD ln-CG lo-LA lt-LT lu-CD luo-KE luy-KE lv-LV mas-KE mas-TZ mer-KE mfe-MU mg-MG mgh-MZ mk-MK ml-IN mr-IN ms-BN ms-MY mt-MT mua-CM my-MM naq-NA nb-NO nd-ZW ne-IN ne-NP nl-AW nl-BE nl-CW nl-NL nl-SX nmg-CM nn-NO nr-ZA nso-ZA nus-SD nyn-UG om-ET om-KE or-IN pa-Arab-PK pa-Guru-IN pl-PL ps-AF pt-AO pt-BR pt-GW pt-MZ pt-PT pt-ST rm-CH rn-BI ro-MD ro-RO rof-TZ ru-MD ru-RU ru-UA rw-RW rwk-TZ sah-RU saq-KE sbp-TZ se-FI se-NO seh-MZ ses-ML sg-CF shi-Latn-MA shi-Tfng-MA si-LK sk-SK sl-SI sn-ZW so-DJ so-ET so-KE so-SO sq-AL sr-Cyrl-BA sr-Cyrl-ME sr-Cyrl-RS sr-Latn-BA sr-Latn-ME sr-Latn-RS ss-SZ ss-ZA ssy-ER st-LS st-ZA sv-FI sv-SE sw-KE sw-TZ swc-CD ta-IN ta-LK te-IN teo-KE teo-UG tg-Cyrl-TJ th-TH ti-ER ti-ET tig-ER tn-ZA to-TO tr-TR ts-ZA twq-NE tzm-Latn-MA uk-UA ur-IN ur-PK uz-Arab-AF uz-Cyrl-UZ uz-Latn-UZ vai-Latn-LR vai-Vaii-LR ve-ZA vi-VN vun-TZ wae-CH wal-ET xh-ZA xog-UG yav-CM yo-NG zh-Hans-CN zh-Hans-HK zh-Hans-MO zh-Hans-SG zh-Hant-HK zh-Hant-MO zh-Hant-TW zu-ZA"}, {"TimeZoneNames", "af am ar as az be bg bn brx bs ca chr cs da de ee el en en-AU en-CA en-Dsrt en-GB en-GU en-HK en-IE en-IN en-NZ en-PK en-SG en-ZA en-ZW es es-419 es-AR et eu fa fi fil fr fr-CA ga gl gsw gu he hi hr hu id is it ja kea kk kn ko kok ksh lt lv mk ml mr ms mt my nb ne nl nn or pa pl pt pt-PT ro ru sk sl sq sr sr-Latn sv sw ta te th to tr uk ur vi zh zh-Hant zu"}, {"FormatData", "aa af af-NA agq ak am ar ar-DZ ar-JO ar-LB ar-MA ar-QA ar-SA ar-SY ar-TN ar-YE as asa az az-Cyrl bas be bem bez bg bm bn bn-IN bo br brx bs byn ca cgg chr cs cy da dav de de-AT de-CH de-LI dje dua dyo dz ebu ee el el-CY en en-AU en-BE en-BW en-BZ en-CA en-Dsrt en-GB en-HK en-IE en-IN en-JM en-MT en-NA en-NZ en-PK en-SG en-TT en-US-POSIX en-ZA en-ZW eo es es-419 es-AR es-BO es-CL es-CO es-CR es-EC es-GQ es-GT es-HN es-PA es-PE es-PR es-PY es-US es-UY es-VE et eu ewo fa fa-AF ff fi fil fo fr fr-BE fr-CA fr-CH fr-LU fur ga gd gl gsw gu guz gv ha haw he hi hr hu hy ia id ig ii is it it-CH ja jmc ka kab kam kde kea khq ki kk kl kln km kn ko kok ksb ksf ksh kw lag lg ln lo lt lu luo luy lv mas mer mfe mg mgh mk ml mr ms ms-BN mt mua my naq nb nd ne ne-IN nl nl-BE nmg nn nr nso nus nyn om or pa pa-Arab pl ps pt pt-PT rm rn ro rof ru ru-UA rw rwk saq sbp se seh ses sg shi shi-Tfng si sk sl sn so sq sr sr-Cyrl-BA sr-Latn sr-Latn-ME ss ssy st sv sv-FI sw sw-KE swc ta te teo th ti ti-ER tig tn to tr ts twq tzm uk ur ur-IN uz uz-Arab uz-Latn vai vai-Latn ve vi vun wae wal xh xog yav yo zh zh-Hans-HK zh-Hans-MO zh-Hans-SG zh-Hant zh-Hant-HK zh-Hant-MO zu"}, {"LocaleNames", "af agq ak am ar as asa az az-Cyrl bas be bem bez bg bm bn bn-IN bo br brx bs ca cgg chr cs cy da dav de de-CH dje dua dyo ebu ee el en en-Dsrt eo es es-CL et eu ewo fa fa-AF ff fi fil fo fr ga gl gsw gu guz gv ha haw he hi hr hu hy id ig ii is it ja jmc ka kab kam kde kea khq ki kk kl kln km kn ko kok ksb ksf kw lag lg ln lt lu luo luy lv mas mer mfe mg mgh mk ml mr ms mt mua my naq nb nd ne nl nl-BE nmg nn nus nyn om or pa pa-Arab pl ps pt pt-PT rm rn ro rof ru ru-UA rw rwk sah saq sbp se seh ses sg shi shi-Tfng si sk sl sn so sq sr sr-Latn st sv sw swc ta te teo tg th ti to tr twq tzm uk ur uz uz-Arab uz-Latn vai vai-Latn vi vun wae xog yav yo zh zh-Hans-HK zh-Hans-MO zh-Hans-SG zh-Hant zh-Hant-HK zu"}, {"All", "aa aa-DJ aa-ER aa-ET af af-NA af-ZA agq agq-CM ak ak-GH am am-ET ar ar-AE ar-BH ar-DZ ar-EG ar-IQ ar-JO ar-KW ar-LB ar-LY ar-MA ar-OM ar-QA ar-SA ar-SD ar-SY ar-TN ar-YE as as-IN asa asa-TZ az az-Cyrl az-Cyrl-AZ az-Latn-AZ bas bas-CM be be-BY bem bem-ZM bez bez-TZ bg bg-BG bm bm-ML bn bn-BD bn-IN bo bo-CN bo-IN br br-FR brx brx-IN bs bs-BA byn byn-ER ca ca-ES cgg cgg-UG chr chr-US cs cs-CZ cy cy-GB da da-DK dav dav-KE de de-AT de-BE de-CH de-DE de-LI de-LU dje dje-NE dua dua-CM dyo dyo-SN dz dz-BT ebu ebu-KE ee ee-GH ee-TG el el-CY el-GR en en-AS en-AU en-BB en-BE en-BM en-BW en-BZ en-CA en-Dsrt en-Dsrt-US en-GB en-GU en-GY en-HK en-IE en-IN en-JM en-MH en-MP en-MT en-MU en-NA en-NZ en-PH en-PK en-SG en-TT en-UM en-US en-US-POSIX en-VI en-ZA en-ZW eo es es-419 es-AR es-BO es-CL es-CO es-CR es-DO es-EC es-ES es-GQ es-GT es-HN es-MX es-NI es-PA es-PE es-PR es-PY es-SV es-US es-UY es-VE et et-EE eu eu-ES ewo ewo-CM fa fa-AF fa-IR ff ff-SN fi fi-FI fil fil-PH fo fo-FO fr fr-BE fr-BF fr-BI fr-BJ fr-BL fr-CA fr-CD fr-CF fr-CG fr-CH fr-CI fr-CM fr-DJ fr-FR fr-GA fr-GF fr-GN fr-GP fr-GQ fr-KM fr-LU fr-MC fr-MF fr-MG fr-ML fr-MQ fr-NE fr-RE fr-RW fr-SN fr-TD fr-TG fr-YT fur fur-IT ga ga-IE gd gd-GB gl gl-ES gsw gsw-CH gu gu-IN guz guz-KE gv gv-GB ha ha-Latn-GH ha-Latn-NE ha-Latn-NG haw haw-US he he-IL hi hi-IN hr hr-HR hu hu-HU hy hy-AM ia id id-ID ig ig-NG ii ii-CN is is-IS it it-CH it-IT ja ja-JP jmc jmc-TZ ka ka-GE kab kab-DZ kam kam-KE kde kde-TZ kea kea-CV khq khq-ML ki ki-KE kk kk-Cyrl-KZ kl kl-GL kln kln-KE km km-KH kn kn-IN ko ko-KR kok kok-IN ksb ksb-TZ ksf ksf-CM ksh ksh-DE kw kw-GB lag lag-TZ lg lg-UG ln ln-CD ln-CG lo lo-LA lt lt-LT lu lu-CD luo luo-KE luy luy-KE lv lv-LV mas mas-KE mas-TZ mer mer-KE mfe mfe-MU mg mg-MG mgh mgh-MZ mk mk-MK ml ml-IN mr mr-IN ms ms-BN ms-MY mt mt-MT mua mua-CM my my-MM naq naq-NA nb nb-NO nd nd-ZW ne ne-IN ne-NP nl nl-AW nl-BE nl-CW nl-NL nl-SX nmg nmg-CM nn nn-NO nr nr-ZA nso nso-ZA nus nus-SD nyn nyn-UG om om-ET om-KE or or-IN pa pa-Arab pa-Arab-PK pa-Guru-IN pl pl-PL ps ps-AF pt pt-AO pt-BR pt-GW pt-MZ pt-PT pt-ST rm rm-CH rn rn-BI ro ro-MD ro-RO rof rof-TZ ru ru-MD ru-RU ru-UA rw rw-RW rwk rwk-TZ sah sah-RU saq saq-KE sbp sbp-TZ se se-FI se-NO seh seh-MZ ses ses-ML sg sg-CF shi shi-Latn-MA shi-Tfng shi-Tfng-MA si si-LK sk sk-SK sl sl-SI sn sn-ZW so so-DJ so-ET so-KE so-SO sq sq-AL sr sr-Cyrl-BA sr-Cyrl-ME sr-Cyrl-RS sr-Latn sr-Latn-BA sr-Latn-ME sr-Latn-RS ss ss-SZ ss-ZA ssy ssy-ER st st-LS st-ZA sv sv-FI sv-SE sw sw-KE sw-TZ swc swc-CD ta ta-IN ta-LK te te-IN teo teo-KE teo-UG tg tg-Cyrl-TJ th th-TH ti ti-ER ti-ET tig tig-ER tn tn-ZA to to-TO tr tr-TR ts ts-ZA twq twq-NE tzm tzm-Latn-MA uk uk-UA ur ur-IN ur-PK uz uz-Arab uz-Arab-AF uz-Cyrl-UZ uz-Latn uz-Latn-UZ vai vai-Latn vai-Latn-LR vai-Vaii-LR ve ve-ZA vi vi-VN vun vun-TZ wae wae-CH wal wal-ET xh xh-ZA xog xog-UG yav yav-CM yo yo-NG zh zh-Hans-CN zh-Hans-HK zh-Hans-MO zh-Hans-SG zh-Hant zh-Hant-HK zh-Hant-MO zh-Hant-TW zu zu-ZA"}, {"CurrencyNames", "aa aa-DJ aa-ER af af-NA agq ak am ar asa az az-Cyrl bas be bem bez bg bm bn bo br brx bs byn ca cgg chr cs cy da dav de de-LU dje dyo dz ebu ee el en en-AU en-BB en-BM en-BW en-BZ en-CA en-HK en-JM en-MT en-NA en-NZ en-PH en-PK en-SG en-TT en-ZA es es-AR es-BO es-CL es-CO es-CR es-DO es-EC es-GT es-HN es-MX es-NI es-PA es-PE es-PR es-PY es-US es-UY es-VE et eu ewo fa fa-AF ff fi fil fo fr fr-BI fr-CA fr-DJ fr-GN fr-KM fr-LU ga gl gsw gu guz ha he hi hr hu hy id ig ii is it ja jmc ka kab kam kde kea khq ki kk kl kln km kn ko ksb ksf lag lg ln lo lt lu luo luy lv mas mas-TZ mer mfe mg mgh mk ml mr ms ms-BN mt mua my naq nb nd ne ne-IN nl nl-AW nl-CW nl-SX nmg nn nr nso nyn om om-KE or pa pa-Arab pl ps pt pt-AO pt-MZ pt-PT pt-ST rm rn ro rof ru rw rwk saq sbp se seh ses sg shi shi-Tfng si sk sl sn so so-DJ so-ET so-KE sq sr sr-Cyrl-BA sr-Latn ss ssy st st-LS sv sw swc ta ta-LK te teo teo-KE th ti ti-ER tig tn to tr ts twq tzm uk ur uz-Arab vai vai-Latn ve vi vun wal xh xog yav yo zh zh-Hans-HK zh-Hans-MO zh-Hans-SG zh-Hant zh-Hant-HK zu"}};
        return var1;
    }


    
    /**
     * 批量替换前缀
     * 
     * @param replacementm 替换值
     * @param searchList 替换列表
     * @return
     */
    public static String replaceFirst(String replacementm, String[] searchList)
    {
        String text = replacementm;
        for (String searchString : searchList)
        {
            if (replacementm.startsWith(searchString))
            {
                text = replacementm.replaceFirst(searchString, "");
                break;
            }
        }
        return text;
    }

    /**
     * 关键字替换
     * 
     * @param text 需要被替换的名字
     * @return 替换后的名字
     */
    public static String replaceText(String text)
    {
        return RegExUtils.replaceAll(text, "(?:表|若依)", "");
    }

    /**
     * 获取数据库类型字段
     * 
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static String getDbType(String columnType)
    {
        if (StringUtils.indexOf(columnType, "(") > 0)
        {
            return StringUtils.substringBefore(columnType, "(");
        }
        else
        {
            return columnType;
        }
    }

    /**
     * 获取字段长度
     * 
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static Integer getColumnLength(String columnType)
    {
        if (StringUtils.indexOf(columnType, "(") > 0)
        {
            String length = StringUtils.substringBetween(columnType, "(", ")");
            return Integer.valueOf(length);
        }
        else
        {
            return 0;
        }
    }

}
