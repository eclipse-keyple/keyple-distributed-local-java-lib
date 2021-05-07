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

import static org.eclipse.keyple.distributed.MessageDto.*;

import org.eclipse.keyple.core.common.KeyplePluginEvent;
import org.eclipse.keyple.core.common.KeypleReaderEvent;
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
   * Gets the connected Keyple core local service api.
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

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public final void onPluginEvent(
      String readerName, String jsonData, KeyplePluginEvent pluginEvent) {
    sendMessage(Action.PLUGIN_EVENT, readerName, jsonData);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onReaderEvent(String readerName, String jsonData, KeypleReaderEvent readerEvent) {
    sendMessage(Action.READER_EVENT, readerName, jsonData);
  }

  /**
   * (private)<br>
   * Sends a message associated to a new session ID using the provided reader name for local and
   * remote reader.
   *
   * @param action The action.
   * @param readerName The reader name (local and remote).
   * @param jsonData The body content.
   */
  private void sendMessage(Action action, String readerName, String jsonData) {

    // Build a plugin event message with a new session ID.
    MessageDto message =
        new MessageDto()
            .setAction(action.name())
            .setLocalReaderName(readerName)
            .setRemoteReaderName(readerName)
            .setSessionId(generateSessionId())
            .setBody(jsonData);

    // Send the message.
    getNode().sendMessage(message);
  }
}
