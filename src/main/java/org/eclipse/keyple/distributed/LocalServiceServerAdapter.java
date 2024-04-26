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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter of {@link LocalServiceServer}.
 *
 * @since 2.0.0
 */
final class LocalServiceServerAdapter extends AbstractLocalServiceAdapter
    implements LocalServiceServer {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceServerAdapter.class);

  private final String[] poolPluginNames;
  private final Set<ClientInfo> pluginClients;
  private final Map<String, Set<ClientInfo>> readerClients;
  private final Object readerClientsMonitor;

  /**
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param poolPluginNames One or more pool plugin names to bind (for pool only).
   * @since 2.0.0
   */
  LocalServiceServerAdapter(String localServiceName, String... poolPluginNames) {
    super(localServiceName);
    this.poolPluginNames = poolPluginNames;
    this.pluginClients = Collections.newSetFromMap(new ConcurrentHashMap<>(1));
    this.readerClients = new ConcurrentHashMap<>(1);
    this.readerClientsMonitor = new Object();
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public SyncNodeServer getSyncNode() {
    if (isBoundToSyncNode()) {
      return (SyncNodeServer) getNode();
    }
    throw new IllegalStateException(
        String.format(
            "Local service [%s] is not configured with a synchronous network protocol", getName()));
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public AsyncNodeServer getAsyncNode() {
    if (!isBoundToSyncNode()) {
      return (AsyncNodeServer) getNode();
    }
    throw new IllegalStateException(
        String.format(
            "Local service [%s] is not configured with an asynchronous network protocol",
            getName()));
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public void connect(LocalServiceApi localServiceApi) {
    super.connect(localServiceApi);
    getLocalServiceApi().setPoolPluginNames(poolPluginNames);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public void onPluginEvent(String readerName, String jsonData) {
    Set<ClientInfo> pluginClientsCopy = new HashSet<>(pluginClients);
    for (ClientInfo clientInfo : pluginClientsCopy) {
      try {
        sendMessage(MessageDto.Action.PLUGIN_EVENT, readerName, jsonData, clientInfo);
      } catch (Exception e) {
        pluginClients.remove(clientInfo);
        logger.warn(
            "Client of plugin event de-referenced due to an unexpected error (readerName: {}, clientNodeId: {}, sessionId: {}, error: {})",
            readerName,
            clientInfo.clientNodeId,
            clientInfo.sessionId,
            e.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  public void onReaderEvent(String readerName, String jsonData) {
    Set<ClientInfo> readerClientsRef = readerClients.get(readerName);
    if (readerClientsRef == null) {
      return;
    }
    Set<ClientInfo> readerClientsCopy = new HashSet<>(readerClientsRef);
    for (ClientInfo clientInfo : readerClientsCopy) {
      try {
        sendMessage(MessageDto.Action.READER_EVENT, readerName, jsonData, clientInfo);
      } catch (Exception e) {
        readerClientsRef.remove(clientInfo);
        logger.warn(
            "Client of reader event de-referenced due to an unexpected error (readerName: {}, clientNodeId: {}, sessionId: {}, error: {})",
            readerName,
            clientInfo.clientNodeId,
            clientInfo.sessionId,
            e.getMessage());
      }
    }
  }

  /**
   * Sends a message using the provided reader name for local and remote reader.
   *
   * @param action The action.
   * @param readerName The reader name (local and remote).
   * @param jsonData The body content.
   * @param clientInfo The client information.
   */
  private void sendMessage(
      MessageDto.Action action, String readerName, String jsonData, ClientInfo clientInfo) {
    getNode()
        .sendMessage(
            new MessageDto()
                .setApiLevel(clientInfo.clientDistributedApiLevel)
                .setAction(action.name())
                .setLocalReaderName(readerName)
                .setRemoteReaderName(readerName)
                .setClientNodeId(clientInfo.clientNodeId)
                .setSessionId(clientInfo.sessionId)
                .setBody(jsonData));
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Override
  void onMessage(MessageDto message) {

    // Register the client for events management.
    registerClient(message);

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

  /**
   * Registers a client.
   *
   * @param message The incoming message.
   */
  private void registerClient(MessageDto message) {

    if (message.getLocalReaderName() != null) {
      // Reader command
      Set<ClientInfo> readerClientInfos = readerClients.get(message.getLocalReaderName());
      if (readerClientInfos == null) {
        synchronized (readerClientsMonitor) {
          readerClientInfos = readerClients.get(message.getLocalReaderName());
          if (readerClientInfos == null) {
            readerClientInfos = Collections.newSetFromMap(new ConcurrentHashMap<>(1));
            readerClients.put(message.getLocalReaderName(), readerClientInfos);
          }
        }
      }
      readerClientInfos.add(
          new ClientInfo(message.getApiLevel(), message.getClientNodeId(), message.getSessionId()));

    } else {
      // Plugin command
      pluginClients.add(
          new ClientInfo(message.getApiLevel(), message.getClientNodeId(), message.getSessionId()));
    }
  }

  /** Client info. */
  private static class ClientInfo {

    private final int clientDistributedApiLevel;
    private final String clientNodeId;
    private final String sessionId;

    private ClientInfo(int clientDistributedApiLevel, String clientNodeId, String sessionId) {
      this.clientDistributedApiLevel = clientDistributedApiLevel;
      this.clientNodeId = clientNodeId;
      this.sessionId = sessionId;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Comparison is based on "clientNodeId" field.
     *
     * @since 2.0.0
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ClientInfo that = (ClientInfo) o;
      return clientNodeId.equals(that.clientNodeId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Hash code is based on "clientNodeId" field.
     *
     * @since 2.0.0
     */
    @Override
    public int hashCode() {
      return clientNodeId.hashCode();
    }
  }
}
