package com.p6spy.engine.autocompensate.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;

public class AutoCompensateInsertHandler extends AutoCompensateHandler {

	private static AutoCompensateInsertHandler autoCompensateInsertHandler = null;

	public static AutoCompensateInsertHandler newInstance() {
		if (autoCompensateInsertHandler == null) {
			synchronized (AutoCompensateInsertHandler.class) {
				if (autoCompensateInsertHandler == null) {
					autoCompensateInsertHandler = new AutoCompensateInsertHandler();
				}
			}
		}
		return autoCompensateInsertHandler;
	}
	
	@Override
	public boolean saveAutoCompensationInfo(PreparedStatement delegate, SQLStatement sqlStatement, String executeSql, String localTxId, String server) throws SQLException {
		
		if (JdbcConstants.MYSQL.equals(sqlStatement.getDbType())) {
			return MySqlInsertHandler.newInstance().saveAutoCompensationInfo(delegate, sqlStatement, executeSql, localTxId, server);
		}
		
		return false;
	}

}