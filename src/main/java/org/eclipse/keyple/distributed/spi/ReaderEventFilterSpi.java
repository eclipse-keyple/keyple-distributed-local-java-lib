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
package org.eclipse.keyple.distributed.spi;

import org.eclipse.keyple.core.common.KeypleReaderEvent;
import org.eclipse.keyple.distributed.CancelEventBroadcastException;
import org.eclipse.keyple.distributed.LocalServiceClient;

/**
 * SPI of the <b>local filter</b> associated to a <b>reader event</b>.
 *
 * <p>You may provide an implementation of this interface if you use a {@link LocalServiceClient}
 * and you plan to observe remotely the local reader.
 *
 * @since 2.0
 */
public interface ReaderEventFilterSpi {

  /**
   * Invoked when a reader event occurs, before to broadcast it to the server.
   *
   * <p>Then, you have the possibility to :
   *
   * <ul>
   *   <li>execute a specific treatment,
   *   <li>return if necessary a DTO to be transmitted to the remote service,
   *   <li>cancel the event's broadcast by throwing the exception {@link
   *       CancelEventBroadcastException}.
   * </ul>
   *
   * @param event The reader event.
   * @return The user input data of the remote service or null if you don't have any data to
   *     transmit to the server.
   * @throws CancelEventBroadcastException If you want to cancel the event's broadcast.
   * @since 2.0
   */
  Object beforeEventBroadcast(KeypleReaderEvent event) throws CancelEventBroadcastException;

  /**
   * Must return the class of the user output data expected at the output of the remote service.
   *
   * <p>Is invoked in order to deserialize the user output data before to invoke the method {@link
   * #afterEventBroadcast(Object)}.
   *
   * @return Null if there is no user output data to deserialize.
   * @since 2.0
   */
  Class<?> getUserOutputDataClass();

  /**
   * Invoked at the end of the processing of the remote service to deliver the result.
   *
   * @param userOutputData The user output data previously deserialized using the method {@link
   *     #getUserOutputDataClass()}, or null if there is no data.
   * @since 2.0
   */
  void afterEventBroadcast(Object userOutputData);

  /**
   * Invoked if an unexpected runtime exception occurs during the broadcast of the reader event
   * (network error, server error, server restarted, etc...).
   *
   * @param exception The runtime exception that occurred.
   * @return True if the local service must stop the observation of the local reader.
   */
  boolean onEventBroadcastError(RuntimeException exception);
}
