
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
