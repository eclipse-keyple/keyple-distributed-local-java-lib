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

import org.eclipse.keyple.core.distributed.local.spi.LocalServiceFactorySpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalServiceServerFactoryBuilderTest {

  static final String SERVICE_NAME = "serviceName";
  static AsyncEndpointServerSpi asyncEndpointServerSpi;

  @BeforeClass
  public static void beforeClass() {
    asyncEndpointServerSpi = mock(AsyncEndpointServerSpi.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenLocalServiceNameIsNull_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(null).withSyncNode().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenLocalServiceNameIsEmpty_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder("").withSyncNode().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenAsyncNodeAndEndpointIsNull_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME).withAsyncNode(null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenSyncNodeAndPoolPluginsNameContainsNull_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
        .withSyncNode()
        .withPoolPlugins(null)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenAsyncNodeAndPoolPluginsNameContainsNull_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
        .withAsyncNode(asyncEndpointServerSpi)
        .withPoolPlugins(null)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenSyncNodeAndPoolPluginsNameContainsEmpty_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
        .withSyncNode()
        .withPoolPlugins("")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void builder_whenAsyncNodeAndPoolPluginsNameContainsEmpty_shouldThrowIAE() {
    LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
        .withAsyncNode(asyncEndpointServerSpi)
        .withPoolPlugins("")
        .build();
  }

  @Test
  public void builder_whenSyncNodeSuccess_shouldReturnANotNullInstance() {
    LocalServiceServerFactory factory =
        LocalServiceServerFactoryBuilder.builder(SERVICE_NAME).withSyncNode().build();
    assertThat(factory)
        .isNotNull()
        .isInstanceOf(LocalServiceFactorySpi.class)
        .isInstanceOf(LocalServiceServerFactoryAdapter.class);
  }

  @Test
  public void builder_whenAsyncNodeSuccess_shouldReturnANotNullInstance() {
    LocalServiceServerFactory factory =
        LocalServiceServerFactoryBuilder.builder(SERVICE_NAME)
            .withAsyncNode(asyncEndpointServerSpi)
            .build();
    assertThat(factory)
        .isNotNull()
        .isInstanceOf(LocalServiceFactorySpi.class)
        .isInstanceOf(LocalServiceServerFactoryAdapter.class);
  }
}
