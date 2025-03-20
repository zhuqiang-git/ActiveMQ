

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface API {
    Status status();

    String since() default "";

    String[] consumers() default {"*"};

    public static enum Status {
        INTERNAL,
        DEPRECATED,
        EXPERIMENTAL,
        MAINTAINED,
        STABLE;

        private Status() {
        }
    }

	public List<MediaType> resolveMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
		for (ContentNegotiationStrategy strategy : this.strategies) {
			List<MediaType> mediaTypes = strategy.resolveMediaTypes(request);
			if (mediaTypes.equals(MEDIA_TYPE_ALL_LIST)) {
				continue;
			}
			return mediaTypes;
		}
		return MEDIA_TYPE_ALL_LIST;
	}


	private List<String> doResolveExtensions(Function<MediaTypeFileExtensionResolver, List<String>> extractor) {
		List<String> result = null;
		for (MediaTypeFileExtensionResolver resolver : this.resolvers) {
			List<String> extensions = extractor.apply(resolver);
			if (CollectionUtils.isEmpty(extensions)) {
				continue;
			}
			result = (result != null ? result : new ArrayList<>(4));
			for (String extension : extensions) {
				if (!result.contains(extension)) {
					result.add(extension);
				}
			}
		}
		return (result != null ? result : Collections.emptyList());
	}



    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy()))
        {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }



    @Autowired
    public void setRedisTemplate1(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setLimitScript(RedisScript<Long> limitScript)
    {
        this.limitScript = limitScript;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        String combineKey = getCombineKey(rateLimiter, point);
        List<Object> keys = Collections.singletonList(combineKey);
        try
        {
            Long number = redisTemplate.execute(limitScript, keys, count, time);
            if (StringUtils.isNull(number) || number.intValue() > count)
            {
                throw new ServiceException("访问过于频繁，请稍候再试");
            }
            log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("服务器限流异常，请稍候再试");
        }
    }

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr()).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }

	public Map<String, MediaType> getMediaTypeMappings() {
		Map<String, MediaType> result = null;
		for (MediaTypeFileExtensionResolver resolver : this.resolvers) {
			if (resolver instanceof MappingMediaTypeFileExtensionResolver) {
				Map<String, MediaType> map = ((MappingMediaTypeFileExtensionResolver) resolver).getMediaTypes();
				if (CollectionUtils.isEmpty(map)) {
					continue;
				}
				result = (result != null ? result : new HashMap<>(4));
				result.putAll(map);
			}
		}
		return (result != null ? result : Collections.emptyMap());
	}
}
