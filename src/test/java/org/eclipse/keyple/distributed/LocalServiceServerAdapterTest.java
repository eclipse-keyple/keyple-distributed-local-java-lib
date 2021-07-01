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
    verifyZeroInteractions(asyncEndpointServerSpi);
  }

  @Test
  public void onReaderEvent_whenNoReaderClientIsReferenced_shouldNotInvokeSendMessageOnEndpoint() {
    syncService.onReaderEvent(LOCAL_READER_NAME, "eventData");
    asyncService.onReaderEvent(LOCAL_READER_NAME, "eventData");
    verifyZeroInteractions(asyncEndpointServerSpi);
  }
}
