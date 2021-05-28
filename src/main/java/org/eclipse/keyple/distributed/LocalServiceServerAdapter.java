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

import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.util.json.JsonUtil;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceServer}.
 *
 * @since 2.0
 */
final class LocalServiceServerAdapter extends AbstractLocalServiceAdapter
    implements LocalServiceServer {

  private final String[] poolPluginNames;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param poolPluginNames One or more pool plugin names to bind (for pool only).
   * @since 2.0
   */
  LocalServiceServerAdapter(String localServiceName, String... poolPluginNames) {
    super(localServiceName);
    this.poolPluginNames = poolPluginNames;
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public SyncNodeServer getSyncNode() {
    AbstractNodeAdapter node = getNode();
    if (node instanceof SyncNodeServer) {
      return (SyncNodeServer) node;
    }
    throw new IllegalStateException(
        String.format(
            "Local service '%s' is not configured with a synchronous network protocol.",
            getName()));
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public AsyncNodeServer getAsyncNode() {
    AbstractNodeAdapter node = getNode();
    if (node instanceof AsyncNodeServer) {
      return (AsyncNodeServer) node;
    }
    throw new IllegalStateException(
        String.format(
            "Local service '%s' is not configured with an asynchronous network protocol.",
            getName()));
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void connect(LocalServiceApi localServiceApi) {
    super.connect(localServiceApi);
    getLocalServiceApi().setPoolPluginNames(poolPluginNames);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onPluginEvent(String readerName, String jsonData) {
    sendMessage(MessageDto.Action.PLUGIN_EVENT, readerName, jsonData);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onReaderEvent(String readerName, String jsonData) {
    sendMessage(MessageDto.Action.READER_EVENT, readerName, jsonData);
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
  private void sendMessage(MessageDto.Action action, String readerName, String jsonData) {

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

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  void onMessage(MessageDto message) {

    MessageDto result;
    try {
      // Execute the command locally.
      String jsonResult =
          getLocalServiceApi().executeLocally(message.getBody(), message.getLocalReaderName());

      // Build the response to send back to the client.
      result = new MessageDto(message).setAction(MessageDto.Action.RESP.name()).setBody(jsonResult);

    } catch (IllegalStateException e) {
      // Build the error response to send back to the client.
      result =
          new MessageDto(message)
              .setAction(MessageDto.Action.ERROR.name())
              .setBody(JsonUtil.toJson(e));
    }

    // Send the response.
    getNode().sendMessage(result);
  }
}
