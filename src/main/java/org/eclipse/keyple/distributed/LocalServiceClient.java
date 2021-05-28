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
   * Executes a specific ticketing service remotely from the server on a local reader.
   *
   * @param serviceId The ticketing service ID. It will permit to indicate to the server which
   *     ticketing service to execute (Materialization, Validation, Control, etc...). This field is
   *     free.
   * @param localReaderName The name of the local reader to manage remotely from the server.
   * @param initialCardContent (optional) : A <b><code>
   *     org.calypsonet.terminal.reader.selection.SmartCard</code></b> containing the initial smart
   *     card content to transmit to the remote ticketing service.
   * @param inputData (optional) : A DTO containing additional information if needed.
   * @param outputDataClass The class of the expected output data. At the end of the execution of
   *     the remote service, the server can transmit output data if needed.
   * @param <T> The generic type of the expected output data.
   * @return a new instance of <b>T</b>.
   * @throws IllegalArgumentException If the service ID or the local reader name are null or empty.
   * @throws IllegalStateException If the local reader is not registered.
   * @since 2.0
   */
  <T> T executeRemoteService(
      String serviceId,
      String localReaderName,
      Object initialCardContent,
      Object inputData,
      Class<T> outputDataClass);
}
