/*
 * Copyright (c) 2018-2019 ActionTech.
 * based on code by ServiceComb Pack CopyrightHolder Copyright (C) 2018,
 * License: http://www.apache.org/licenses/LICENSE-2.0 Apache License 2.0 or higher.
 */

package org.apache.servicecomb.saga.omega.transaction;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import javax.transaction.InvalidTransactionException;

import org.apache.servicecomb.saga.omega.context.OmegaContext;
import org.apache.servicecomb.saga.omega.transaction.annotations.Compensable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ForwardRecovery is used to execute business logic with the given retries times.
 * If retries == 0, use the default recovery to execute only once.
 * If retries > 0, it will use the forward recovery and retry the given times at most.
 * If retries < 0, it will use the forward recovery and retry forever until interrupted.
 */
public class ForwardRecovery extends DefaultRecovery {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // 2018/03/10 we do not support retry with timeout yet
  @Override
  public Object apply(ProceedingJoinPoint joinPoint, Compensable compensable, CompensableInterceptor interceptor,
      OmegaContext context, String parentTxId, int retries) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    int remains = retries;
    try {
      while (true) {
        try {
          return super.apply(joinPoint, compensable, interceptor, context, parentTxId, remains);
        } catch (Throwable throwable) {
          if (throwable instanceof InvalidTransactionException) {
            throw throwable;
          }

          if (remains > 0) {
            remains--;
          } else if (remains == 0) {
            LOG.error(
                    "Retried sub tx failed maximum times, global tx id: {}, local tx id: {}, method: {}, retried times: {}",
                    context.globalTxId(), context.localTxId(), method.toString(), retries);
            throw throwable;
          }

          LOG.warn("Retrying sub tx failed, global tx id: {}, local tx id: {}, method: {}, remains: {}",
              context.globalTxId(), context.localTxId(), method.toString(), remains);
          Thread.sleep(compensable.retryDelayInMilliseconds());
        }
      }
    } catch (InterruptedException e) {
      String errorMessage = "Failed to handle tx because it is interrupted, global tx id: " + context.globalTxId()
          + ", local tx id: " + context.localTxId() + ", method: " + method.toString();
      LOG.error(errorMessage);
      interceptor.onError(parentTxId, compensationMethodSignature(joinPoint, compensable, method), e);
      throw new OmegaException(errorMessage);
    } catch (Exception e) {
      throw e;
    }
  }
}
