/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.saga.omega.transaction;

import org.apache.servicecomb.saga.omega.context.CompensationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class CompensationMessageHandler implements MessageHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MessageSender sender;

  private final CompensationContext context;

  public CompensationMessageHandler(MessageSender sender, CompensationContext context) {
    this.sender = sender;
    this.context = context;
  }

  @Override
  public void onReceive(String globalTxId, String localTxId, String parentTxId, String compensationMethod,
      Object... payloads) {
    try {
      context.apply(globalTxId, localTxId, compensationMethod, payloads);
    } catch (Exception e) {
      LOG.error("Failed to execute 'onReceive.context.apply' localTxId {}", localTxId, e);
    }
    sender.send(new TxCompensatedEvent(globalTxId, localTxId, parentTxId, compensationMethod));
  }
}
