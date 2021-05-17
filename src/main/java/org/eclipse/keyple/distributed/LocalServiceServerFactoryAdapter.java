/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.core.common.KeypleDistributedLocalServiceExtensionFactory;
import org.eclipse.keyple.core.distributed.local.spi.LocalServiceSpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceServerFactory}.
 *
 * @since 2.0
 */
final class LocalServiceServerFactoryAdapter extends AbstractLocalServiceFactoryAdapter
    implements LocalServiceServerFactory, KeypleDistributedLocalServiceExtensionFactory {

  private final AsyncEndpointServerSpi asyncEndpointServerSpi;
  private final String[] poolPluginNames;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param asyncEndpointServerSpi The async endpoint server to bind.
   * @param poolPluginNames One or more pool plugin names to bind (for pool only).
   * @since 2.0
   */
  LocalServiceServerFactoryAdapter(
      String localServiceName,
      AsyncEndpointServerSpi asyncEndpointServerSpi,
      String... poolPluginNames) {
    super(localServiceName);
    this.asyncEndpointServerSpi = asyncEndpointServerSpi;
    this.poolPluginNames = poolPluginNames;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public LocalServiceSpi getLocalService() {
    return new LocalServiceServerAdapter(
        getLocalServiceName(), asyncEndpointServerSpi, poolPluginNames);
  }
}
