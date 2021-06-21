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
import static org.mockito.Mockito.*;

import org.eclipse.keyple.core.distributed.local.spi.LocalServiceFactorySpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalServiceClientFactoryBuilderTest {

  static final String SERVICE_NAME = "serviceName";
  static SyncEndpointClientSpi syncEndpointClientSpi;
  static AsyncEndpointClientSpi asyncEndpointClientSpi;

  @BeforeClass
  public static void beforeClass() {
    syncEndpointClientSpi = mock(SyncEndpointClientSpi.class);
    asyncEndpointClientSpi = mock(AsyncEndpointClientSpi.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenLocalServiceNameIsNull_shouldThrowIAE() {
    LocalServiceClientFactoryBuilder.builder(null).withSyncNode(syncEndpointClientSpi).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenLocalServiceNameIsEmpty_shouldThrowIAE() {
    LocalServiceClientFactoryBuilder.builder("").withSyncNode(syncEndpointClientSpi).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenSyncNodeAndEndpointIsNull_shouldThrowIAE() {
    LocalServiceClientFactoryBuilder.builder(SERVICE_NAME).withSyncNode(null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenAsyncNodeAndEndpointIsNull_shouldThrowIAE() {
    LocalServiceClientFactoryBuilder.builder(SERVICE_NAME).withAsyncNode(null, 1).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenAsyncNodeAndTimeoutIsLessThan1_shouldThrowIAE() {
    LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
        .withAsyncNode(asyncEndpointClientSpi, 0)
        .build();
  }

  @Test
  public void builder_whenSyncNodeSuccess_shouldReturnANotNullInstance() {
    LocalServiceClientFactory factory =
        LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
            .withSyncNode(syncEndpointClientSpi)
            .build();
    assertThat(factory)
        .isNotNull()
        .isInstanceOf(LocalServiceFactorySpi.class)
        .isInstanceOf(LocalServiceClientFactoryAdapter.class);
  }

  @Test
  public void builder_whenAsyncNodeSuccess_shouldReturnANotNullInstance() {
    LocalServiceClientFactory factory =
        LocalServiceClientFactoryBuilder.builder(SERVICE_NAME)
            .withAsyncNode(asyncEndpointClientSpi, 1)
            .build();
    assertThat(factory)
        .isNotNull()
        .isInstanceOf(LocalServiceFactorySpi.class)
        .isInstanceOf(LocalServiceClientFactoryAdapter.class);
  }
}
