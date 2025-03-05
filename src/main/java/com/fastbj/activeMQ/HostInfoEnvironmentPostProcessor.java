
	// Before ConfigFileApplicationListener
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER - 1;

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		InetUtils.HostInfo hostInfo = getFirstNonLoopbackHostInfo(environment);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("spring.cloud.client.hostname", hostInfo.getHostname());
		map.put("spring.cloud.client.ip-address", hostInfo.getIpAddress());
		MapPropertySource propertySource = new MapPropertySource("springCloudClientHostInfo", map);
		environment.getPropertySources().addLast(propertySource);
	}

	private HostInfo getFirstNonLoopbackHostInfo(ConfigurableEnvironment environment) {
		InetUtilsProperties target = new InetUtilsProperties();
		ConfigurationPropertySources.attach(environment);
		Binder.get(environment).bind(InetUtilsProperties.PREFIX, Bindable.ofInstance(target));
		try (InetUtils utils = new InetUtils(target)) {
			return utils.findFirstNonLoopbackHostInfo();
		}
	}
