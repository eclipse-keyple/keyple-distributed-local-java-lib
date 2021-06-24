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
import static org.mockito.Mockito.mock;

import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalServiceServerFactoryAdapterTest {

  static final String SERVICE_NAME = "serviceName";

  static AsyncEndpointServerSpi asyncEndpointServerSpi;

  static LocalServiceServerFactoryAdapter syncFactory;
  static LocalServiceServerFactoryAdapter asyncFactory;

  @BeforeClass
  public static void beforeClass() {
    syncFactory =
        (LocalServiceServerFactoryAdapter)
            LocalServiceServerFactoryBuilder.builder(SERVICE_NAME).withSyncNode().build();

    asyncEndpointServerSpi = mock(AsyncEndpointServerSpi.class);
    asyncFactory =
        (LocalServiceServerFactoryAdapter)
            LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
                .withAsyncNode(asyncEndpointServerSpi)
                .build();
  }

  @Test
  public void getCommonApiVersion_shouldReturnANotEmptyValue() {
    assertThat(syncFactory.getCommonApiVersion()).isNotEmpty();
    assertThat(asyncFactory.getCommonApiVersion()).isNotEmpty();
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
        .isInstanceOf(LocalServiceServer.class)
        .isInstanceOf(LocalServiceServerAdapter.class);
    assertThat(asyncFactory.getLocalService())
        .isNotNull()
        .isInstanceOf(LocalServiceServer.class)
        .isInstanceOf(LocalServiceServerAdapter.class);
  }
}
