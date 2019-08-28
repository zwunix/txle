/*
 * Copyright (c) 2018-2019 ActionTech.
 * based on code by ServiceComb Pack CopyrightHolder Copyright (C) 2018,
 * License: http://www.apache.org/licenses/LICENSE-2.0 Apache License 2.0 or higher.
 */

package org.apache.servicecomb.saga.alpha.core;

import org.apache.servicecomb.saga.common.EventType;

import java.util.*;

/**
 * Repository for {@link TxEvent}
 */
public interface TxEventRepository {

  /**
   * Save a {@link TxEvent}.
   *
   * @param event for global/sub transaction
   */
  void save(TxEvent event);

  /**
   * Find a {@link TxEvent} which satisfies below requirements:
   *
   * <ol>
   *   <li>{@link TxEvent#type} is {@link EventType#TxAbortedEvent}</li>
   *   <li>There are no {@link TxEvent} which has the same {@link TxEvent#globalTxId} and {@link TxEvent#type} is {@link EventType#TxEndedEvent} or {@link EventType#SagaEndedEvent}</li>
   * </ol>
   * @return event list
   */
  Optional<List<TxEvent>> findFirstAbortedGlobalTransaction();

  /**
   * Find timeout {@link TxEvent}s. A timeout TxEvent satisfies below requirements:
   *
   * <ol>
   *  <li>{@link TxEvent#type} is {@link EventType#TxStartedEvent} or {@link EventType#SagaStartedEvent}</li>
   *  <li>Current time greater than {@link TxEvent#expiryTime}</li>
   *  <li>There are no corresponding {@link TxEvent} which type is <code>TxEndedEvent</code> or <code>SagaEndedEvent</code></li>
   * </ol>
   *
   * @param unendedMinEventId the min identify of undone event
   * @return event list
   */
  List<TxEvent> findTimeoutEvents(long unendedMinEventId);

  TxEvent findTimeoutEventsBeforeEnding(String globalTxId);

  /**
   * Find a {@link TxEvent} which satisfies below requirements:
   * <ol>
   *   <li>{@link TxEvent#type} is {@link EventType#TxStartedEvent}</li>
   *   <li>{@link TxEvent#globalTxId} equals to param <code>globalTxId</code></li>
   *   <li>{@link TxEvent#localTxId} equals to param <code>localTxId</code></li>
   * </ol>
   *
   * @param globalTxId global transaction identify
   * @param localTxId sub-transaction identify
   * @return {@link TxEvent}
   */
  Optional<TxEvent> findTxStartedEvent(String globalTxId, String localTxId);

  /**
   * Find {@link TxEvent}s which satisfy below requirements:
   * <ol>
   *   <li>{@link TxEvent#globalTxId} equals to param <code>globalTxId</code></li>
   *   <li>{@link TxEvent#type} equals to param <code>type</code></li>
   * </ol>
   *
   * @param globalTxId globalTxId to search for
   * @param type       event type to search for
   * @return event list
   */
  List<TxEvent> findTransactions(String globalTxId, String type);

  /**
   * Find a {@link TxEvent} which satisfies below requirements:
   * <ol>
   *   <li>{@link TxEvent#type} equals to {@link EventType#TxEndedEvent}</li>
   *   <li>{@link TxEvent#surrogateId} greater than param <code>id</code></li>
   *   <li>{@link TxEvent#type} equals to param <code>type</code></li>
   *   <li>There is a corresponding <code>TxAbortedEvent</code></li>
   *   <li>There is no coresponding <code>TxCompensatedEvent</code></li>
   * </ol>
   *
   * @param id event id
   * @param type event type
   * @return event list
   */
  List<TxEvent> findFirstUncompensatedEventByIdGreaterThan(long id, String type);

  List<TxEvent> findSequentialCompensableEventOfUnended(long unendedMinEventId);

  /**
   * Delete duplicated {@link TxEvent}s which {@link TxEvent#type} equals param <code>type</code>.
   *
   * @param type event type
   */
  void deleteDuplicateEvents(String type);

  void deleteDuplicateEventsByTypeAndSurrogateIds(String type, List<Long> maxSurrogateIdList);

  List<Long> getMaxSurrogateIdGroupByGlobalTxIdByType(String type);

  Iterable<TxEvent> findAll();

  TxEvent findOne(long id);

  List<String> selectAllTypeByGlobalTxId(String globalTxId);

  List<TxEvent> selectPausedAndContinueEvent(String globalTxId);

  long count();

  boolean checkIsRetriedEvent(String globalTxId);

  Set<String> selectEndedGlobalTx(Set<String> localTxIdSet);

  boolean checkIsExistsTxCompensatedEvent(String globalTxId, String localTxId, String type);

  TxEvent selectAbortedTxEvent(String globalTxId);

  boolean checkTxIsAborted(String globalTxId, String localTxId);

  List<Map<String, Object>> findTxList(int pageIndex, int pageSize, String orderName, String direction, String searchText);

  List<TxEvent> selectTxEventByGlobalTxIds(List<String> globalTxIdList);

  long findTxCount(String searchText);

  List<Map<String, Object>> findSubTxList(String globalTxIds);

  List<TxEvent> selectSpecialColumnsOfTxEventByGlobalTxIds(List<String> globalTxIdList);

  List<TxEvent> selectUnendedTxEvents(long unendedMinEventId);

  long selectMinUnendedTxEventId(long unendedMinEventId);

  Date selectMinDateInTxEvent();

  List<Long> selectEndedEventIdsWithinSomePeriod(int pageIndex, int pageSize, Date startTime, Date endTime);

}
