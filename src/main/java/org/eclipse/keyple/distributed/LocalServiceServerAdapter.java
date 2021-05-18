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

import java.util.Arrays;
import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceServer}.
 *
 * @since 2.0
 */
final class LocalServiceServerAdapter extends AbstractLocalServiceAdapter
    implements LocalServiceServer {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceServerAdapter.class);

  private final String[] poolPluginNames;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param asyncEndpointServerSpi The async endpoint server to bind.
   * @param poolPluginNames One or more pool plugin names to bind (for pool only).
   * @since 2.0
   */
  LocalServiceServerAdapter(
      String localServiceName,
      AsyncEndpointServerSpi asyncEndpointServerSpi,
      String... poolPluginNames) {

    super(localServiceName);
    this.poolPluginNames = poolPluginNames;

    // Logging
    String nodeType = asyncEndpointServerSpi != null ? "AsyncNodeServer" : "SyncNodeServer";
    String withPoolPluginNames = Arrays.toString(poolPluginNames);
    logger.info(
        "Create a new 'LocalServiceServer' with name='{}', nodeType='{}', withPoolPluginNames={}",
        localServiceName,
        nodeType,
        withPoolPluginNames);

    if (asyncEndpointServerSpi == null) {
      bindSyncNodeServer();
    } else {
      bindAsyncNodeServer(asyncEndpointServerSpi);
    }
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
  void onMessage(MessageDto message) {

    switch (MessageDto.Action.valueOf(message.getAction())) {

      case START_PLUGINS_OBSERVATION:
        getLocalServiceApi().startPluginsObservation();
        break;

      case STOP_PLUGINS_OBSERVATION:
        getLocalServiceApi().stopPluginsObservation();
        break;

      default:
        MessageDto result;
        try {
          // Execute the command locally.
          String jsonResult =
              getLocalServiceApi().executeLocally(message.getBody(), message.getLocalReaderName());

          // Build the response to send back to the client.
          result =
              new MessageDto(message).setAction(MessageDto.Action.RESP.name()).setBody(jsonResult);

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
}
