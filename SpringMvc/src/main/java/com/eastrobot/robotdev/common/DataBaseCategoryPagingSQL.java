package com.eastrobot.robotdev.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.eastrobot.robotdev.utils.PropertiesUtil;

/**
 * 处理不同数据库的分页SQL
 * 
 * @author
 * @since 1.0.0(2011-06-30)
 * 
 */
@Component
@Lazy(value = false)
public class DataBaseCategoryPagingSQL implements InitializingBean {

	static boolean isMySql;
	static boolean isOralcle;
	static boolean isSqlServer;
	static boolean isDB2;

	public static String getStatement(String tableName, String baseSQL,
			long startSize, long endSize) {
		if (isMySql) {
			return baseSQL + " limit " + startSize + ","
					+ (endSize - startSize);
		} else if (isOralcle) {
			return "SELECT * FROM (SELECT A.*, ROWNUM RN FROM (" + baseSQL
					+ ") A WHERE " + "ROWNUM <= " + endSize + ") WHERE RN > "
					+ startSize;
		} else if (isSqlServer || isDB2) {
			String sql = getSqlServerLimitSQL(baseSQL);
			sql = sql.replace("rs.rownum > ?", "rs.rownum > "
					+ String.valueOf(startSize));
			sql = sql.replace("rs.rownum <= ?", "rs.rownum <= "
					+ String.valueOf(endSize));
			return sql;
		} else {
			return "Unknown database!";
		}
	}

	private static Pattern sqlServerSelectPtn = Pattern.compile("(?i)select");
	private static Pattern fromClausePtn = Pattern.compile("(?i)\\*\\s+from\\s+([\\S]+)");
	private static final String SQLSERVER_LIMIT_TEMPLATE = "SELECT * FROM (SELECT ROW_NUMBER() OVER (order by (select 0)) AS rownum, {main} ) rs WHERE rs.rownum > ? and rs.rownum <= ?";

	public static String getSqlServerLimitSQL(String baseSQL) {
		baseSQL = sqlServerSelectPtn.matcher(baseSQL).replaceFirst("");
		String tpl = SQLSERVER_LIMIT_TEMPLATE;
		if (isDB2){
			tpl = tpl.replace("ROW_NUMBER", "ROWNUMBER");
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = fromClausePtn.matcher(baseSQL);
			if (matcher.find()){
				matcher.appendReplacement(buffer, matcher.group(1)+"."+matcher.group(0));
			}
			matcher.appendTail(buffer);
			baseSQL = buffer.toString();
		}
		int orderByIdx = baseSQL.indexOf("order by");
		if (orderByIdx == -1)
			orderByIdx = baseSQL.indexOf("ORDER BY");
		String mainSql = baseSQL;
		if (orderByIdx > 0) {
			mainSql = baseSQL.substring(0, orderByIdx);
			String orderSql = baseSQL.substring(orderByIdx);
			tpl = tpl.replace("order by (select 0)", orderSql);
		}else if (isDB2){
			tpl = tpl.replace("select 0", "select 1 from sysibm.sysdummy1");
		}
		tpl = tpl.replace("{main}", mainSql);
		return tpl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String dialect = PropertiesUtil.getString("hibernate.dialect").toUpperCase();
		if (dialect.indexOf("MYSQL") != -1)
			isMySql = true;
		if (dialect.indexOf("ORACLE") != -1)
			isOralcle = true;
		if (dialect.indexOf("SQLSERVER") != -1)
			isSqlServer = true;
		if (dialect.indexOf("DB2") != -1)
			isDB2 = true;
	}
}
