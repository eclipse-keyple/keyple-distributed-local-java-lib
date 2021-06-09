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

import org.eclipse.keyple.core.distributed.local.spi.LocalServiceSpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceClientFactory}.
 *
 * @since 2.0
 */
final class LocalServiceClientFactoryAdapter extends AbstractLocalServiceFactoryAdapter
    implements LocalServiceClientFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(LocalServiceClientFactoryAdapter.class);

  private final SyncEndpointClientSpi syncEndpointClientSpi;
  private final AsyncEndpointClientSpi asyncEndpointClientSpi;
  private final int asyncNodeClientTimeoutSeconds;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param syncEndpointClientSpi The sync endpoint client to bind.
   * @param asyncEndpointClientSpi The async endpoint client to bind.
   * @param asyncNodeClientTimeoutSeconds The async node client timeout (in seconds).
   * @since 2.0
   */
  LocalServiceClientFactoryAdapter(
      String localServiceName,
      SyncEndpointClientSpi syncEndpointClientSpi,
      AsyncEndpointClientSpi asyncEndpointClientSpi,
      int asyncNodeClientTimeoutSeconds) {
    super(localServiceName);
    this.syncEndpointClientSpi = syncEndpointClientSpi;
    this.asyncEndpointClientSpi = asyncEndpointClientSpi;
    this.asyncNodeClientTimeoutSeconds = asyncNodeClientTimeoutSeconds;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public LocalServiceSpi getLocalService() {

    // Create the local service.
    LocalServiceClientAdapter localService = new LocalServiceClientAdapter(getLocalServiceName());

    // Bind the node.
    if (syncEndpointClientSpi != null) {
      logger.info(
          "Create a new 'LocalServiceClient' with name='{}', nodeType='SyncNodeClient'",
          getLocalServiceName());

      localService.bindSyncNodeClient(syncEndpointClientSpi, null, null);

    } else {
      logger.info(
          "Create a new 'LocalServiceClient' with name='{}', nodeType='AsyncNodeClient', timeoutSeconds={}",
          getLocalServiceName(),
          asyncNodeClientTimeoutSeconds);

      localService.bindAsyncNodeClient(asyncEndpointClientSpi, asyncNodeClientTimeoutSeconds);
    }

    return localService;
  }
}
