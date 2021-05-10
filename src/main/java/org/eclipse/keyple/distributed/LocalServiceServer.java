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

import org.eclipse.keyple.core.common.KeypleDistributedLocalServiceExtension;

/**
 * API of the <b>Local Service Server</b> associated to the <b>Remote Plugin Client</b>.
 *
 * <p>This service must be started by the application installed on a <b>Server</b> having local
 * access to the smart card reader but wishes to delegate all or part of the ticketing processing to
 * a remote application :
 *
 * <ul>
 *   <li>To <b>start</b> the service, use the class {@link LocalServiceServerFactoryBuilder} to
 *       build an instance of the factory {@link LocalServiceServerFactory} and register it to the
 *       Keyple service.
 *   <li>To <b>access</b> the service, use the available accessors in the Keyple service, but it is
 *       not necessary because this service is only used internally by Keyple.
 *   <li>To <b>stop</b> the service, unregister it from the Keyple service.
 * </ul>
 *
 * @since 2.0
 */
public interface LocalServiceServer extends KeypleDistributedLocalServiceExtension {

  /**
   * Gets the associated {@link SyncNodeServer} if the service is configured with a synchronous
   * network protocol.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not configured with a synchronous network
   *     protocol.
   * @since 2.0
   */
  SyncNodeServer getSyncNode();

  /**
   * Gets the associated {@link AsyncNodeServer} if the service is configured with an asynchronous
   * network protocol.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not configured with an asynchronous network
   *     protocol.
   * @since 2.0
   */
  AsyncNodeServer getAsyncNode();
}
