
	@Nullable
	private RowMapper<T> rowMapper;

	@SuppressWarnings("rawtypes")
	@Nullable
	private Class<? extends RowMapper> rowMapperClass;


	public void setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}

	@SuppressWarnings("rawtypes")
	public void setRowMapperClass(Class<? extends RowMapper> rowMapperClass) {
		this.rowMapperClass = rowMapperClass;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.isTrue(this.rowMapper != null || this.rowMapperClass != null,
				"'rowMapper' or 'rowMapperClass' is required");
	}


	@Override
	@SuppressWarnings("unchecked")
	protected RowMapper<T> newRowMapper(@Nullable Object[] parameters, @Nullable Map<?, ?> context) {
		if (this.rowMapper != null) {
			return this.rowMapper;
		}
		else {
			Assert.state(this.rowMapperClass != null, "No RowMapper set");
			return BeanUtils.instantiateClass(this.rowMapperClass);
		}
	}



	@Override
	public String toString() {
		return format(getMethod(), getUrl().toString(), getBody(), getHeaders());
	}

	static <T> String format(@Nullable HttpMethod httpMethod, String url, @Nullable T body, HttpHeaders headers) {
		StringBuilder builder = new StringBuilder("<");
		builder.append(httpMethod);
		builder.append(' ');
		builder.append(url);
		builder.append(',');
		if (body != null) {
			builder.append(body);
			builder.append(',');
		}
		builder.append(headers);
		builder.append('>');
		return builder.toString();
	}


	// Static builder methods

	/**
	 * Create a builder with the given method and url.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param url the URL
	 * @return the created builder
	 */
	public static BodyBuilder method(HttpMethod method, URI url) {
		return new DefaultBodyBuilder(method, url);
	}

	/**
	 * Create a builder with the given HTTP method, URI template, and variables.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 * @since 5.3
	 */
	public static BodyBuilder method(HttpMethod method, String uriTemplate, Object... uriVariables) {
		return new DefaultBodyBuilder(method, uriTemplate, uriVariables);
	}

	/**
	 * Create a builder with the given HTTP method, URI template, and variables.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param uriTemplate the uri template to use
	 * @return the created builder
	 * @since 5.3
	 */
	public static BodyBuilder method(HttpMethod method, String uriTemplate, Map<String, ?> uriVariables) {
		return new DefaultBodyBuilder(method, uriTemplate, uriVariables);
	}

