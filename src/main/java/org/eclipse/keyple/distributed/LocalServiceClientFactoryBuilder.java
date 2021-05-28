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

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;

/**
 * Builder of {@link LocalServiceClientFactory}.
 *
 * @since 2.0
 */
public final class LocalServiceClientFactoryBuilder {

  /**
   * (private)<br>
   * Constructor
   */
  private LocalServiceClientFactoryBuilder() {}

  /**
   * Gets the first step of the builder to use in order to create a new factory instance.
   *
   * @param localServiceName The identifier of the local service.
   * @return Next configuration step.
   * @throws IllegalArgumentException If the service name is null or empty.
   * @since 2.0
   */
  public static NodeStep builder(String localServiceName) {
    return new Builder(localServiceName);
  }

  /**
   * Step to configure the node associated with the service.
   *
   * @since 2.0
   */
  public interface NodeStep {

    /**
     * Configures the service with a {@link SyncNodeClient} node.
     *
     * @param endpoint The {@link SyncEndpointClientSpi} network endpoint to use.
     * @return Next configuration step.
     * @throws IllegalArgumentException If the provided endpoint is null.
     * @since 2.0
     */
    BuilderStep withSyncNode(SyncEndpointClientSpi endpoint);

    /**
     * Configures the service with a {@link AsyncNodeClient} node.
     *
     * @param endpoint The {@link AsyncEndpointClientSpi} network endpoint to use.
     * @param timeoutSeconds This timeout (in seconds) defines how long the async client waits for a
     *     server order before cancelling the global transaction.
     * @return Next configuration step.
     * @throws IllegalArgumentException If the endpoint is null or the timeout {@code <} 1.
     * @since 2.0
     */
    BuilderStep withAsyncNode(AsyncEndpointClientSpi endpoint, int timeoutSeconds);
  }

  /**
   * Last step : build a new instance.
   *
   * @since 2.0
   */
  public interface BuilderStep {

    /**
     * Creates a new instance of {@link LocalServiceClientFactory} using the current configuration.
     *
     * @return A not null reference.
     * @since 2.0
     */
    LocalServiceClientFactory build();
  }

  /**
   * (private)<br>
   * The internal step builder.
   */
  private static final class Builder implements NodeStep, BuilderStep {

    private final String localServiceName;
    private SyncEndpointClientSpi syncEndpoint;
    private AsyncEndpointClientSpi asyncEndpoint;
    private int timeoutSeconds;

    private Builder(String localServiceName) {
      Assert.getInstance().notEmpty(localServiceName, "localServiceName");
      this.localServiceName = localServiceName;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public BuilderStep withSyncNode(SyncEndpointClientSpi endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.syncEndpoint = endpoint;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public BuilderStep withAsyncNode(AsyncEndpointClientSpi endpoint, int timeoutSeconds) {
      Assert.getInstance()
          .notNull(endpoint, "endpoint")
          .greaterOrEqual(timeoutSeconds, 1, "timeoutSeconds");
      this.asyncEndpoint = endpoint;
      this.timeoutSeconds = timeoutSeconds;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public LocalServiceClientFactory build() {
      return new LocalServiceClientFactoryAdapter(
          localServiceName, syncEndpoint, asyncEndpoint, timeoutSeconds);
    }
  }
}
