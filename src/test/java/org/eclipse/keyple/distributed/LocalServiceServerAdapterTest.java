/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.distributed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalServiceServerAdapterTest {

  static final String SERVICE_NAME = "SERVICE_NAME";
  static final String SERVICE_ID = "serviceId";
  static final String LOCAL_READER_NAME = "localReaderName";
  static final String COMMAND = "command";
  static final String SESSION_ID = "sessionId";
  static final String CLIENT_NODE_ID = "clientNodeId";
  static final String SERVER_NODE_ID = "serverNodeId";
  static final String POOL_PLUGIN_NAME_1 = "poolPluginName1";
  static final String POOL_PLUGIN_NAME_2 = "poolPluginName2";
  static final String PLUGIN_EVENT_DATA = "pluginEventData";
  static final String READER_EVENT_DATA = "readerEventData";

  static String OUTPUT_DATA;

  static LocalServiceApi localServiceApi;

  static AsyncEndpointServerSpi asyncEndpointServerSpi;

  static LocalServiceServerFactoryAdapter syncFactory;
  static LocalServiceServerFactoryAdapter asyncFactory;

  static LocalServiceServerAdapter syncService;
  static LocalServiceServerAdapter asyncService;

  @BeforeClass
  public static void beforeClass() {

    localServiceApi = mock(LocalServiceApi.class);

    syncFactory =
        (LocalServiceServerFactoryAdapter)
            LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
                .withSyncNode()
                .withPoolPlugins(POOL_PLUGIN_NAME_1, POOL_PLUGIN_NAME_2)
                .build();
    syncService = (LocalServiceServerAdapter) syncFactory.getLocalService();

    asyncEndpointServerSpi = mock(AsyncEndpointServerSpi.class);
    asyncFactory =
        (LocalServiceServerFactoryAdapter)
            LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
                .withAsyncNode(asyncEndpointServerSpi)
                .withPoolPlugins(POOL_PLUGIN_NAME_1, POOL_PLUGIN_NAME_2)
                .build();
    asyncService = (LocalServiceServerAdapter) asyncFactory.getLocalService();
  }

  @Test
  public void getLocalServiceApi_whenConnectNotInvoked_shouldReturnNull() {
    assertThat(syncService.getLocalServiceApi()).isNull();
    assertThat(asyncService.getLocalServiceApi()).isNull();
  }

  @Test
  public void getLocalServiceApi_whenConnectIsInvoked_shouldReturnTheProvidedApi() {
    syncService.connect(localServiceApi);
    asyncService.connect(localServiceApi);
    assertThat(syncService.getLocalServiceApi()).isSameAs(localServiceApi);
    assertThat(asyncService.getLocalServiceApi()).isSameAs(localServiceApi);
  }

  @Test(expected = NullPointerException.class)
  public void connect_whenSyncAndApiIsNull_shouldThrowNPE() {
    syncService.connect(null);
  }

  @Test(expected = NullPointerException.class)
  public void connect_whenAsyncAndApiIsNull_shouldThrowNPE() {
    asyncService.connect(null);
  }

  @Test
  public void connect_whenApiIsSet_shouldSetPoolPluginsToApi() {
    LocalServiceApi syncLocalServiceApi = mock(LocalServiceApi.class);
    syncService.connect(syncLocalServiceApi);
    verify(syncLocalServiceApi).setPoolPluginNames(POOL_PLUGIN_NAME_1, POOL_PLUGIN_NAME_2);

    LocalServiceApi asyncLocalServiceApi = mock(LocalServiceApi.class);
    asyncService.connect(asyncLocalServiceApi);
    verify(asyncLocalServiceApi).setPoolPluginNames(POOL_PLUGIN_NAME_1, POOL_PLUGIN_NAME_2);
  }

  @Test
  public void getName_shouldReturnTheProvidedName() {
    assertThat(syncService.getName()).isEqualTo(SERVICE_NAME);
    assertThat(asyncService.getName()).isEqualTo(SERVICE_NAME);
  }

  @Test
  public void getNode_shouldReturnANotNullInstance() {
    assertThat(syncService.getNode()).isInstanceOf(SyncNodeServerAdapter.class);
    assertThat(asyncService.getNode()).isInstanceOf(AsyncNodeServerAdapter.class);
  }

  @Test(expected = IllegalStateException.class)
  public void getSyncNode_whenAsync_shouldThrowISE() {
    asyncService.getSyncNode();
  }

  @Test
  public void getSyncNode_whenSync_shouldReturnANotNullInstance() {
    SyncNodeServer node = syncService.getSyncNode();
    assertThat(node).isInstanceOf(SyncNodeServerAdapter.class);
  }

  @Test(expected = IllegalStateException.class)
  public void getAsyncNode_whenSync_shouldThrowISE() {
    syncService.getAsyncNode();
  }

  @Test
  public void getAsyncNode_whenAsync_shouldReturnANotNullInstance() {
    AsyncNodeServer node = asyncService.getAsyncNode();
    assertThat(node).isInstanceOf(AsyncNodeServerAdapter.class);
  }

  @Test
  public void onPluginEvent_whenNoPluginClientIsReferenced_shouldNotInvokeSendMessageOnEndpoint() {
    syncService.onPluginEvent(LOCAL_READER_NAME, "eventData");
    asyncService.onPluginEvent(LOCAL_READER_NAME, "eventData");
    verifyNoInteractions(asyncEndpointServerSpi);
  }

  @Test
  public void onReaderEvent_whenNoReaderClientIsReferenced_shouldNotInvokeSendMessageOnEndpoint() {
    syncService.onReaderEvent(LOCAL_READER_NAME, "eventData");
    asyncService.onReaderEvent(LOCAL_READER_NAME, "eventData");
    verifyNoInteractions(asyncEndpointServerSpi);
  }

  @Test
  public void onMessage_whenReaderCommandIsRejected_shouldNotKeepTheClientRegistered()
      throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, "unknownReader"))
        .thenThrow(new IllegalStateException("Reader 'unknownReader' is not registered"));

    List<MessageDto> responses =
        service.getSyncNode().onRequest(buildCommandMessage("unknownReader", CLIENT_NODE_ID));

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getAction()).isEqualTo(MessageDto.Action.ERROR.name());
    assertThat(getReaderClients(service)).isEmpty();
  }

  @Test
  public void onMessage_whenPluginCommandIsRejected_shouldNotKeepTheClientRegistered()
      throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, null))
        .thenThrow(new IllegalArgumentException("Malformed command"));

    service.getSyncNode().onRequest(buildCommandMessage(null, CLIENT_NODE_ID));

    assertThat(getPluginClients(service)).isEmpty();
  }

  @Test
  public void onMessage_whenReaderCommandIsAccepted_shouldRegisterTheClient() throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, LOCAL_READER_NAME))
        .thenReturn("outputData");

    service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID));

    assertThat(getReaderClients(service)).containsOnlyKeys(LOCAL_READER_NAME);
    assertThat(getReaderClients(service).get(LOCAL_READER_NAME)).hasSize(1);
  }

  @Test
  public void
      onMessage_whenCommandOfAnAlreadyRegisteredClientIsRejected_shouldKeepTheClientRegistered()
          throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, LOCAL_READER_NAME))
        .thenReturn("outputData")
        .thenThrow(new IllegalArgumentException("Malformed command"));

    service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID));
    service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID));

    assertThat(getReaderClients(service).get(LOCAL_READER_NAME)).hasSize(1);
  }

  @Test
  public void onMessage_whenMaxPluginClientsIsReached_shouldRejectOnlyNewClients()
      throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, null)).thenReturn("outputData");
    for (int i = 0; i < LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS; i++) {
      service.getSyncNode().onRequest(buildCommandMessage(null, CLIENT_NODE_ID + i));
    }

    List<MessageDto> newClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(null, "newClient"));
    List<MessageDto> existingClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(null, CLIENT_NODE_ID + 0));

    assertThat(newClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.ERROR.name());
    assertThat(existingClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.RESP.name());
    assertThat(getPluginClients(service)).hasSize(LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS);
    verify(
            service.getLocalServiceApi(),
            times(LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS + 1))
        .executeLocally(COMMAND, null);
  }

  @Test
  public void onMessage_whenMaxReaderClientsIsReached_shouldRejectOnlyNewClients()
      throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, LOCAL_READER_NAME))
        .thenReturn("outputData");
    for (int i = 0; i < LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS; i++) {
      service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID + i));
    }

    List<MessageDto> newClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, "newClient"));
    List<MessageDto> existingClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID + 0));

    assertThat(newClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.ERROR.name());
    assertThat(existingClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.RESP.name());
    assertThat(getReaderClients(service).get(LOCAL_READER_NAME))
        .hasSize(LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS);
  }

  @Test
  public void
      onMessage_whenMaxPluginClientsIsReachedAndOldClientsAreInactive_shouldPurgeThemAndAcceptNewClient()
          throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, null)).thenReturn("outputData");
    for (int i = 0; i < LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS; i++) {
      service.getSyncNode().onRequest(buildCommandMessage(null, CLIENT_NODE_ID + i));
    }
    // Only the first client observes the plugin events
    service.getSyncNode().onRequest(buildCheckPluginEventMessage(CLIENT_NODE_ID + 0));
    makeRegistrationsOld(getPluginClients(service));

    List<MessageDto> newClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(null, "newClient"));

    assertThat(newClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.RESP.name());
    assertThat(getClientNodeIds(getPluginClients(service)))
        .containsExactlyInAnyOrder(CLIENT_NODE_ID + 0, "newClient");
  }

  @Test
  public void
      onMessage_whenMaxReaderClientsIsReachedAndOldClientsAreInactive_shouldPurgeThemAndAcceptNewClient()
          throws Exception {
    LocalServiceServerAdapter service = buildConnectedSyncService();
    when(service.getLocalServiceApi().executeLocally(COMMAND, LOCAL_READER_NAME))
        .thenReturn("outputData");
    for (int i = 0; i < LocalServiceServerAdapter.MAX_REGISTERED_CLIENTS; i++) {
      service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, CLIENT_NODE_ID + i));
    }
    makeRegistrationsOld(getReaderClients(service).get(LOCAL_READER_NAME));

    List<MessageDto> newClientResponses =
        service.getSyncNode().onRequest(buildCommandMessage(LOCAL_READER_NAME, "newClient"));

    assertThat(newClientResponses.get(0).getAction()).isEqualTo(MessageDto.Action.RESP.name());
    assertThat(getClientNodeIds(getReaderClients(service).get(LOCAL_READER_NAME)))
        .containsExactly("newClient");
  }

  private static MessageDto buildCheckPluginEventMessage(String clientNodeId) {
    return new MessageDto()
        .setApiLevel(MessageDto.API_LEVEL)
        .setAction(MessageDto.Action.CHECK_PLUGIN_EVENT.name())
        .setSessionId(SESSION_ID)
        .setClientNodeId(clientNodeId)
        .setBody("{\"strategy\":\"POLLING\"}");
  }

  private static void makeRegistrationsOld(Set<?> clientInfos) throws Exception {
    for (Object clientInfo : clientInfos) {
      Field field = clientInfo.getClass().getDeclaredField("registrationDatetime");
      field.setAccessible(true);
      field.setLong(clientInfo, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(11));
    }
  }

  private static List<String> getClientNodeIds(Set<?> clientInfos) throws Exception {
    List<String> clientNodeIds = new ArrayList<>();
    for (Object clientInfo : clientInfos) {
      Field field = clientInfo.getClass().getDeclaredField("clientNodeId");
      field.setAccessible(true);
      clientNodeIds.add((String) field.get(clientInfo));
    }
    return clientNodeIds;
  }

  private static LocalServiceServerAdapter buildConnectedSyncService() {
    LocalServiceServerAdapter service =
        (LocalServiceServerAdapter)
            ((LocalServiceServerFactoryAdapter)
                    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME).withSyncNode().build())
                .getLocalService();
    service.connect(mock(LocalServiceApi.class));
    return service;
  }

  private static MessageDto buildCommandMessage(String localReaderName, String clientNodeId) {
    return new MessageDto()
        .setApiLevel(MessageDto.API_LEVEL)
        .setAction(MessageDto.Action.CMD.name())
        .setSessionId(SESSION_ID)
        .setClientNodeId(clientNodeId)
        .setLocalReaderName(localReaderName)
        .setBody(COMMAND);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Set<?>> getReaderClients(LocalServiceServerAdapter service)
      throws Exception {
    Field field = LocalServiceServerAdapter.class.getDeclaredField("readerClients");
    field.setAccessible(true);
    return (Map<String, Set<?>>) field.get(service);
  }

  private static Set<?> getPluginClients(LocalServiceServerAdapter service) throws Exception {
    Field field = LocalServiceServerAdapter.class.getDeclaredField("pluginClients");
    field.setAccessible(true);
    return (Set<?>) field.get(service);
  }
}
