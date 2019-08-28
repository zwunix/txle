/*
 * Copyright (c) 2018-2019 ActionTech.
 * based on code by ServiceComb Pack CopyrightHolder Copyright (C) 2018,
 * License: http://www.apache.org/licenses/LICENSE-2.0 Apache License 2.0 or higher.
 */

package org.apache.servicecomb.saga.omega.transaction;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.servicecomb.saga.common.EventType;
import org.apache.servicecomb.saga.omega.context.CompensationContext;
import org.apache.servicecomb.saga.omega.transaction.accidentplatform.AccidentHandling;
import org.apache.servicecomb.saga.pack.contract.grpc.GrpcConfigAck;
import org.junit.Before;
import org.junit.Test;

public class CompensationMessageHandlerTest {

  private final List<TxEvent> events = new ArrayList<>();
  private final MessageSender sender = new MessageSender() {
    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void close() {

    }

    @Override
    public String target() {
      return "UNKNOWN"; }

    @Override
    public AlphaResponse send(TxEvent event) {
      events.add(event);
      return new AlphaResponse(false);
    }

    @Override
    public Set<String> send(Set<String> localTxIdSet) {
      return null;
    }

    @Override
    public String reportMessageToServer(KafkaMessage message) {
      return "";
    }

    @Override
    public String reportAccidentToServer(AccidentHandling accidentHandling) {
      return null;
    }

    @Override
    public GrpcConfigAck readConfigFromServer(int type, String category) {
      return null;
    }
  };

  private final String globalTxId = uniquify("globalTxId");
  private final String localTxId = uniquify("localTxId");
  private final String parentTxId = uniquify("parentTxId");

  private final String compensationMethod = getClass().getCanonicalName();
  private final String payload = uniquify("blah");

  private final CompensationContext context = mock(CompensationContext.class);

  private final CompensationMessageHandler handler = new CompensationMessageHandler(sender, context);

  @Before
  public void setUp() {
    events.clear();
  }

  @Test
  public void sendsCompensatedEventOnCompensationCompleted() {
    handler.onReceive(globalTxId, localTxId, parentTxId, compensationMethod, payload);

    assertThat(events.size(), is(1));

    TxEvent event = events.get(0);
    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxCompensatedEvent));
    assertThat(event.compensationMethod(), is(getClass().getCanonicalName()));
    assertThat(event.payloads().length, is(0));

    verify(context).apply(globalTxId, localTxId, compensationMethod, payload);
  }
}
