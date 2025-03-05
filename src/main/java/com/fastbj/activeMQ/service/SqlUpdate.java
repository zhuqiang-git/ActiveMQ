	public int update(Object[] params, KeyHolder generatedKeyHolder) throws DataAccessException {
		if (!isReturnGeneratedKeys() && getGeneratedKeysColumnNames() == null) {
			throw new InvalidDataAccessApiUsageException(
					"The update method taking a KeyHolder should only be used when generated keys have " +
					"been configured by calling either 'setReturnGeneratedKeys' or " +
					"'setGeneratedKeysColumnNames'.");
		}
		validateParameters(params);
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(params), generatedKeyHolder);
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}


public int updateByNamedParam(Map<String, ?> paramMap) throws DataAccessException {
		validateNamedParameters(paramMap);
		ParsedSql parsedSql = getParsedSql();
		MapSqlParameterSource paramSource = new MapSqlParameterSource(paramMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, getDeclaredParameters());
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(sqlToUse, params));
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

	public int updateByNamedParam(Map<String, ?> paramMap, KeyHolder generatedKeyHolder) throws DataAccessException {
		validateNamedParameters(paramMap);
		ParsedSql parsedSql = getParsedSql();
		MapSqlParameterSource paramSource = new MapSqlParameterSource(paramMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, getDeclaredParameters());
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(sqlToUse, params), generatedKeyHolder);
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}
