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

import static org.assertj.core.api.Assertions.*;
import static org.eclipse.keyple.distributed.MessageDto.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import java.util.Collections;
import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class LocalServiceClientAdapterTest {

  static final String SERVICE_NAME = "SERVICE_NAME";
  static final String SERVICE_ID = "serviceId";
  static final String LOCAL_READER_NAME = "localReaderName";
  static final String COMMAND = "command";
  static final String SESSION_ID = "sessionId";
  static final String CLIENT_NODE_ID = "clientNodeId";
  static final String SERVER_NODE_ID = "serverNodeId";

  static String OUTPUT_DATA;

  static LocalServiceApi localServiceApi;

  static SyncEndpointClientSpi syncEndpointClientSpi;
  static AsyncEndpointClientSpi asyncEndpointClientSpi;

  static LocalServiceClientFactoryAdapter syncFactory;
  static LocalServiceClientFactoryAdapter asyncFactory;

  static LocalServiceClientAdapter syncService;
  static LocalServiceClientAdapter asyncService;

  static MessageDto endRemoteServiceMessage;

  static class InputData {
    String data = "inputData";
  }

  static class OutputData {
    String data = "outputData";
  }

  @BeforeClass
  public static void beforeClass() {

    JsonObject body = new JsonObject();
    body.add(JsonProperty.OUTPUT_DATA.getKey(), JsonUtil.getParser().toJsonTree(new OutputData()));
    OUTPUT_DATA = body.toString();

    endRemoteServiceMessage =
        new MessageDto()
            .setApiLevel(API_LEVEL)
            .setAction(Action.END_REMOTE_SERVICE.name())
            .setSessionId(SESSION_ID)
            .setClientNodeId(CLIENT_NODE_ID)
            .setServerNodeId(SERVER_NODE_ID)
            .setBody(OUTPUT_DATA);

    localServiceApi = mock(LocalServiceApi.class);
    doReturn(true).when(localServiceApi).isReaderContactless(LOCAL_READER_NAME);

    syncEndpointClientSpi = mock(SyncEndpointClientSpi.class);
    doReturn(Collections.singletonList(endRemoteServiceMessage))
        .when(syncEndpointClientSpi)
        .sendRequest(ArgumentMatchers.<MessageDto>any());
    syncFactory =
        (LocalServiceClientFactoryAdapter)
            LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
                .withSyncNode(syncEndpointClientSpi)
                .build();
    syncService = (LocalServiceClientAdapter) syncFactory.getLocalService();

    asyncEndpointClientSpi = mock(AsyncEndpointClientSpi.class);
    asyncFactory =
        (LocalServiceClientFactoryAdapter)
            LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
                .withAsyncNode(asyncEndpointClientSpi, 1)
                .build();
    asyncService = (LocalServiceClientAdapter) asyncFactory.getLocalService();
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

  @Test
  public void connect_whenApiIsNull_shouldConnectToNull() {
    syncService.connect(null);
    asyncService.connect(null);
    assertThat(syncService.getLocalServiceApi()).isNull();
    assertThat(asyncService.getLocalServiceApi()).isNull();
  }

  @Test
  public void getName_shouldReturnTheProvidedName() {
    assertThat(syncService.getName()).isEqualTo(SERVICE_NAME);
    assertThat(asyncService.getName()).isEqualTo(SERVICE_NAME);
  }

  @Test
  public void getNode_shouldReturnANotNullInstance() {
    assertThat(syncService.getNode()).isInstanceOf(SyncNodeClientAdapter.class);
    assertThat(asyncService.getNode()).isInstanceOf(AsyncNodeClientAdapter.class);
  }

  @Test(expected = IllegalStateException.class)
  public void getAsyncNode_whenSync_shouldThrowISE() {
    syncService.getAsyncNode();
  }

  @Test
  public void getAsyncNode_whenAsync_shouldReturnANotNullInstance() {
    AsyncNodeClient node = asyncService.getAsyncNode();
    assertThat(node).isInstanceOf(AsyncNodeClientAdapter.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenSyncAndServiceIdIsNull_shouldThrowIAE() {
    syncService.executeRemoteService(null, LOCAL_READER_NAME, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenAsyncAndServiceIdIsNull_shouldThrowIAE() {
    asyncService.executeRemoteService(null, LOCAL_READER_NAME, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenSyncAndServiceIdIsEmpty_shouldThrowIAE() {
    syncService.executeRemoteService("", LOCAL_READER_NAME, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenAsyncAndServiceIdIsEmpty_shouldThrowIAE() {
    asyncService.executeRemoteService("", LOCAL_READER_NAME, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenSyncAndLocalReaderNameIsNull_shouldThrowIAE() {
    syncService.executeRemoteService(SERVICE_ID, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenAsyncAndLocalReaderNameIsNull_shouldThrowIAE() {
    asyncService.executeRemoteService(SERVICE_ID, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenSyncAndLocalReaderNameIsEmpty_shouldThrowIAE() {
    syncService.executeRemoteService(SERVICE_ID, "", null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeRemoteService_whenAsyncAndLocalReaderNameIsEmpty_shouldThrowIAE() {
    asyncService.executeRemoteService(SERVICE_ID, "", null, null, null);
  }

  @Test
  public void executeRemoteService_whenSyncAndOutputDataClassIsNull_shouldReturnNull() {
    Object outputData =
        syncService.executeRemoteService(SERVICE_ID, LOCAL_READER_NAME, null, null, null);
    assertThat(outputData).isNull();
  }

  @Test
  public void executeRemoteService_whenSyncAndOutputDataClassIsSet_shouldReturnANotNullValue() {
    syncService.connect(localServiceApi);
    OutputData expectedOutputData = new OutputData();
    OutputData outputData =
        syncService.executeRemoteService(
            SERVICE_ID, LOCAL_READER_NAME, null, null, OutputData.class);
    assertThat(outputData).isEqualToComparingFieldByField(expectedOutputData);
    syncService.connect(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void onPluginEvent_shouldThrowUOE() {
    syncService.onPluginEvent(null, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void onReaderEvent_shouldThrowUOE() {
    syncService.onReaderEvent(null, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void onMessage_shouldThrowUOE() {
    syncService.onMessage(null);
  }
}
