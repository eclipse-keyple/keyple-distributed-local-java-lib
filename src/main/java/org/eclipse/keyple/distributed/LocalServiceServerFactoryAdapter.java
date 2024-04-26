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

import java.util.Arrays;
import org.eclipse.keyple.core.common.KeypleDistributedLocalServiceExtensionFactory;
import org.eclipse.keyple.core.distributed.local.spi.LocalServiceSpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter of {@link LocalServiceServerFactory}.
 *
 * @since 2.0.0
 */
final class LocalServiceServerFactoryAdapter extends AbstractLocalServiceFactoryAdapter
    implements LocalServiceServerFactory, KeypleDistributedLocalServiceExtensionFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(LocalServiceServerFactoryAdapter.class);

  private final AsyncEndpointServerSpi asyncEndpointServerSpi;
  private final String[] poolPluginNames;

  /**
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param asyncEndpointServerSpi The async endpoint server to bind.
   * @param poolPluginNames One or more pool plugin names to bind (for pool only).
   * @since 2.0.0
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
   * @since 2.0.0
   */
  @Override
  public LocalServiceSpi getLocalService() {

    // Create the local service.
    LocalServiceServerAdapter localService =
        new LocalServiceServerAdapter(getLocalServiceName(), poolPluginNames);

    // Bind the node.
    String nodeType = asyncEndpointServerSpi != null ? "AsyncNodeServer" : "SyncNodeServer";
    String withPoolPluginNames = Arrays.toString(poolPluginNames);
    logger.info(
        "Create new 'LocalServiceServer' (name: {}, nodeType: {}, withPoolPluginNames: {})",
        getLocalServiceName(),
        nodeType,
        withPoolPluginNames);

    if (asyncEndpointServerSpi == null) {
      localService.bindSyncNodeServer();
    } else {
      localService.bindAsyncNodeServer(asyncEndpointServerSpi);
    }

    return localService;
  }
}
