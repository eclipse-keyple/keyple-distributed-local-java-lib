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
 * API of the <b>Local Service Client</b> associated to the <b>Remote Plugin Server</b>.
 *
 * <p>This service must be started by the application installed on a <b>Client</b> having local
 * access to the smart card reader but wishes to delegate all or part of the ticketing processing to
 * a remote application :
 *
 * <ul>
 *   <li>To <b>start</b> the service, use the class {@link LocalServiceClientFactoryBuilder} to
 *       build an instance of the factory {@link LocalServiceClientFactory} and register it to the
 *       Keyple service.
 *   <li>To <b>access</b> the service, use the available accessors in the Keyple service.
 *   <li>To <b>stop</b> the service, unregister it from the Keyple service.
 * </ul>
 *
 * @since 2.0
 */
public interface LocalServiceClient extends KeypleDistributedLocalServiceExtension {

  /**
   * Gets the associated {@link AsyncNodeClient} if the service is configured with an asynchronous
   * network protocol.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not configured with an asynchronous network
   *     protocol.
   * @since 2.0
   */
  AsyncNodeClient getAsyncNode();

  /**
   * Allows you to connect a local card reader to a remote server and execute a specific ticketing
   * service from the server.
   *
   * <p>The service is identify by the <b>serviceId</b> parameter.
   *
   * @param parameters The service parameters (serviceId, ...) (see {@link RemoteServiceParameters}
   *     documentation for all possible parameters).
   * @param classOfUserOutputData The class of the expected user output data.
   * @param <T> The generic type of the expected user output data.
   * @return a new instance of <b>T</b>.
   * @throws IllegalArgumentException If the input parameters or class is null, or if the reader
   *     observation is required but the local reader is not observable.
   * @throws IllegalStateException If the local reader is not registered.
   * @since 2.0
   */
  <T> T executeRemoteService(RemoteServiceParameters parameters, Class<T> classOfUserOutputData);
}
