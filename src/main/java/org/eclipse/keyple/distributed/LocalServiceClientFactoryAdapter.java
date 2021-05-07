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

import org.eclipse.keyple.core.distributed.local.spi.LocalServiceSpi;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.ReaderEventFilterSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceClientFactory}.
 *
 * @since 2.0
 */
final class LocalServiceClientFactoryAdapter extends AbstractLocalServiceFactoryAdapter
    implements LocalServiceClientFactory {

  private final SyncEndpointClientSpi syncEndpointClientSpi;
  private final AsyncEndpointClientSpi asyncEndpointClientSpi;
  private final int asyncNodeClientTimeoutSeconds;
  private final boolean withReaderObservation;
  private final ReaderEventFilterSpi readerEventFilterSpi;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param syncEndpointClientSpi The sync endpoint client to bind.
   * @param asyncEndpointClientSpi The async endpoint client to bind.
   * @param asyncNodeClientTimeoutSeconds The async node client timeout (in seconds).
   * @param withReaderObservation With reader observation ?
   * @param readerEventFilterSpi The optional reader event filter to use if reader observation is
   *     requested.
   * @since 2.0
   */
  LocalServiceClientFactoryAdapter(
      String localServiceName,
      SyncEndpointClientSpi syncEndpointClientSpi,
      AsyncEndpointClientSpi asyncEndpointClientSpi,
      int asyncNodeClientTimeoutSeconds,
      boolean withReaderObservation,
      ReaderEventFilterSpi readerEventFilterSpi) {
    super(localServiceName);
    this.syncEndpointClientSpi = syncEndpointClientSpi;
    this.asyncEndpointClientSpi = asyncEndpointClientSpi;
    this.asyncNodeClientTimeoutSeconds = asyncNodeClientTimeoutSeconds;
    this.withReaderObservation = withReaderObservation;
    this.readerEventFilterSpi = readerEventFilterSpi;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public LocalServiceSpi getLocalService() {
    return new LocalServiceClientAdapter(
        getLocalServiceName(),
        syncEndpointClientSpi,
        asyncEndpointClientSpi,
        asyncNodeClientTimeoutSeconds,
        withReaderObservation,
        readerEventFilterSpi);
  }
}
