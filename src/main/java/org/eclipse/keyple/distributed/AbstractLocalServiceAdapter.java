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

import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.distributed.local.spi.LocalServiceSpi;

/**
 * (package-private)<br>
 * Abstract class of all local service adapters.
 *
 * @since 2.0
 */
abstract class AbstractLocalServiceAdapter extends AbstractMessageHandlerAdapter
    implements LocalServiceSpi {

  private final String localServiceName;

  private LocalServiceApi localServiceApi;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service.
   * @since 2.0
   */
  AbstractLocalServiceAdapter(String localServiceName) {
    this.localServiceName = localServiceName;
  }

  /**
   * (package-private)<br>
   * Gets the connected Keyple core local service API.
   *
   * @return Null if the current local service is not yet registered to the Keyple main service.
   * @since 2.0
   */
  LocalServiceApi getLocalServiceApi() {
    return localServiceApi;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void connect(LocalServiceApi localServiceApi) {
    this.localServiceApi = localServiceApi;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public final String getName() {
    return localServiceName;
  }
}
