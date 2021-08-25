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

import org.eclipse.keyple.core.common.CommonApiProperties;
import org.eclipse.keyple.core.distributed.local.DistributedLocalApiProperties;
import org.eclipse.keyple.core.distributed.local.spi.LocalServiceFactorySpi;

/**
 * (package-private)<br>
 * Abstract class of all local service factory adapters.
 *
 * @since 2.0.0
 */
abstract class AbstractLocalServiceFactoryAdapter implements LocalServiceFactorySpi {

  private final String localServiceName;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @since 2.0.0
   */
  AbstractLocalServiceFactoryAdapter(String localServiceName) {
    this.localServiceName = localServiceName;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public final String getDistributedLocalApiVersion() {
    return DistributedLocalApiProperties.VERSION;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public final String getCommonApiVersion() {
    return CommonApiProperties.VERSION;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public final String getLocalServiceName() {
    return localServiceName;
  }
}
