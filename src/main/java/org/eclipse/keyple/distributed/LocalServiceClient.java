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
 * API of the <b>Local Service Client</b> associated to a <b>Remote Plugin Server</b> to be used in
 * the <b>Reader Client Side</b> configuration mode.
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
   * Executes on a local reader a specific ticketing service remotely from the server.
   *
   * <p>A remote reader associated with the local reader is created during the transaction. Thus,
   * the remote application will be able to transparently interact directly with the local reader
   * via the remote reader.
   *
   * <p><u>Note</u> : The associated remote reader is not observable. If it is necessary to observe
   * the local reader, it is the responsibility of the local application to do so.
   *
   * @param serviceId The ticketing service ID. It will permit to indicate to the server which
   *     ticketing service to execute (Materialization, Validation, Control, etc...). This field is
   *     free.
   * @param localReaderName The name of the local reader to manage remotely from the server.
   * @param initialCardContent (optional) : A <b><code>
   *     org.calypsonet.terminal.reader.selection.SmartCard</code></b> containing the initial smart
   *     card content to transmit to the remote ticketing service.
   * @param inputData (optional) : A DTO containing additional information if needed. This field is
   *     free. This method uses Class.getClass() to get the type for the specified object, but the
   *     getClass() loses the generic type information because of the Type Erasure feature of Java.
   *     Note that this method works fine if the any of the object fields are of generic type, just
   *     the object itself should not be of a generic type.
   * @param outputDataClass (optional) : The class of the expected output data. At the end of the
   *     execution of the remote service, the server can transmit output data if needed. A null
   *     value indicates that the remote service will return nothing.
   * @param <T> The generic type of the expected output data.
   * @return A new instance of <b>T</b> or null if the provided output data class is null or if the
   *     returned server output data is null.
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
