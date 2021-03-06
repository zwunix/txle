/*
 * Copyright (c) 2018-2019 ActionTech.
 * License: http://www.apache.org/licenses/LICENSE-2.0 Apache License 2.0 or higher.
 */

package org.apache.servicecomb.saga.omega.transaction.autocompensate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public interface IAutoCompensateHandler {

    void prepareCompensationBeforeExecuting(PreparedStatement delegate, String executeSql, Map<String, Object> standbyParams) throws SQLException;

    void prepareCompensationAfterExecuting(PreparedStatement delegate, String executeSql, Map<String, Object> standbyParams) throws SQLException;

}
