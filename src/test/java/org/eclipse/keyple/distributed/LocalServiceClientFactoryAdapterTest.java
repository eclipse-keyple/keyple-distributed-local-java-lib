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
import static org.mockito.Mockito.mock;

import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalServiceClientFactoryAdapterTest {

  static final String SERVICE_NAME = "serviceName";

  static SyncEndpointClientSpi syncEndpointClientSpi;
  static AsyncEndpointClientSpi asyncEndpointClientSpi;

  static LocalServiceClientFactoryAdapter syncFactory;
  static LocalServiceClientFactoryAdapter asyncFactory;

  @BeforeClass
  public static void beforeClass() {

    syncEndpointClientSpi = mock(SyncEndpointClientSpi.class);
    syncFactory =
        (LocalServiceClientFactoryAdapter)
            LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
                .withSyncNode(syncEndpointClientSpi)
                .build();

    asyncEndpointClientSpi = mock(AsyncEndpointClientSpi.class);
    asyncFactory =
        (LocalServiceClientFactoryAdapter)
            LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
                .withAsyncNode(asyncEndpointClientSpi, 1)
                .build();
  }

  @Test
  public void getCommonsApiVersion_shouldReturnANotEmptyValue() {
    assertThat(syncFactory.getCommonsApiVersion()).isNotEmpty();
    assertThat(asyncFactory.getCommonsApiVersion()).isNotEmpty();
  }

  @Test
  public void getDistributedLocalApiVersion_shouldReturnANotEmptyValue() {
    assertThat(syncFactory.getDistributedLocalApiVersion()).isNotEmpty();
    assertThat(asyncFactory.getDistributedLocalApiVersion()).isNotEmpty();
  }

  @Test
  public void getLocalServiceName_shouldReturnTheProvidedName() {
    assertThat(syncFactory.getLocalServiceName()).isEqualTo(SERVICE_NAME);
    assertThat(asyncFactory.getLocalServiceName()).isEqualTo(SERVICE_NAME);
  }

  @Test
  public void getLocalService_whenSync_shouldReturnANotNullInstance() {
    assertThat(syncFactory.getLocalService())
        .isNotNull()
        .isInstanceOf(LocalServiceClient.class)
        .isInstanceOf(LocalServiceClientAdapter.class);
    assertThat(asyncFactory.getLocalService())
        .isNotNull()
        .isInstanceOf(LocalServiceClient.class)
        .isInstanceOf(LocalServiceClientAdapter.class);
  }
}
