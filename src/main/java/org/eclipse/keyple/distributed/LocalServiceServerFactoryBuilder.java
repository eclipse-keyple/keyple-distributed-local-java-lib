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

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;

/**
 * Builder of {@link LocalServiceServerFactory}.
 *
 * @since 2.0
 */
public final class LocalServiceServerFactoryBuilder {

  /**
   * (private)<br>
   * Constructor
   */
  private LocalServiceServerFactoryBuilder() {}

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
     * Configures the service with a {@link SyncNodeServer} node.
     *
     * @return Next configuration step.
     * @since 2.0
     */
    BuilderStep withSyncNode();

    /**
     * Configures the service with a {@link AsyncNodeServer} node.
     *
     * @param endpoint The {@link AsyncEndpointServerSpi} network endpoint to use.
     * @return Next configuration step.
     * @throws IllegalArgumentException If the endpoint is null.
     * @since 2.0
     */
    BuilderStep withAsyncNode(AsyncEndpointServerSpi endpoint);
  }

  /**
   * Last step : build a new instance.
   *
   * @since 2.0
   */
  public interface BuilderStep {

    /**
     * Configures the service with one or more pool plugin(s) in order to restrict the search area
     * for pool plugin only.
     *
     * @param poolPluginNames One or more pool plugin names.
     * @return Next configuration step.
     * @throws IllegalArgumentException If no pool plugin name is set or if some names are null or
     *     empty.
     * @since 2.0
     */
    BuilderStep withPoolPlugins(String... poolPluginNames);

    /**
     * Creates a new instance of {@link LocalServiceServerFactory} using the current configuration.
     *
     * @return A not null reference.
     * @since 2.0
     */
    LocalServiceServerFactory build();
  }

  /**
   * (private)<br>
   * The internal step builder.
   */
  private static final class Builder implements NodeStep, BuilderStep {

    private final String localServiceName;
    private AsyncEndpointServerSpi asyncEndpoint;
    private String[] poolPluginNames;

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
    public BuilderStep withSyncNode() {
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public BuilderStep withAsyncNode(AsyncEndpointServerSpi endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public BuilderStep withPoolPlugins(String... poolPluginNames) {
      Assert.getInstance().notNull(poolPluginNames, "poolPluginNames");
      for (String poolPluginName : poolPluginNames) {
        Assert.getInstance().notEmpty(poolPluginName, "poolPluginName");
      }
      this.poolPluginNames = poolPluginNames;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public LocalServiceServerFactory build() {
      return new LocalServiceServerFactoryAdapter(localServiceName, asyncEndpoint, poolPluginNames);
    }
  }
}
