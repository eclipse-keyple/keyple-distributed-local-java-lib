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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.distributed.local.LocalServiceApi;
import org.eclipse.keyple.core.util.json.BodyError;
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

  /** Max number of clients registered for plugin events, and for the events of each reader. */
  static final int MAX_REGISTERED_CLIENTS = 10000;

  /** Min registration duration before an inactive client can be purged (in milliseconds). */
  private static final long MIN_REGISTRATION_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(10);

  /** Min duration between two purges of the inactive clients (in milliseconds). */
  private static final long PURGE_PERIOD_MILLIS = TimeUnit.MINUTES.toMillis(1);

  private final String[] poolPluginNames;
  private final Set<ClientInfo> pluginClients;
  private final Map<String, Set<ClientInfo>> readerClients;
  private final Object readerClientsMonitor;
  private volatile long lastPurgeDatetime;
  private final Object purgeMonitor;

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
    this.purgeMonitor = new Object();
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
        "Local service '" + getName() + "' is not configured with a synchronous network protocol");
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
        "Local service '"
            + getName()
            + "' is not configured with an asynchronous network protocol");
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
            "Client of plugin event de-referenced due to an unexpected error [readerName={}, clientNodeId={}, sessionId={}, error={}]",
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
            "Client of reader event de-referenced due to an unexpected error [readerName={}, clientNodeId={}, sessionId={}, error={}]",
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

    MessageDto result;
    ClientInfo newClientInfo = null;
    try {
      // Register the client for events management.
      newClientInfo = registerClient(message);

      // Execute the command locally.
      String jsonResult =
          getLocalServiceApi().executeLocally(message.getBody(), message.getLocalReaderName());

      // Build the response to send back to the client.
      result = new MessageDto(message).setAction(MessageDto.Action.RESP.name()).setBody(jsonResult);

    } catch (Exception e) {
      // The client is only kept if the command has been processed.
      if (newClientInfo != null) {
        unregisterClient(message.getLocalReaderName(), newClientInfo);
      }
      // Build the error response to send back to the client.
      result =
          new MessageDto(message)
              .setAction(MessageDto.Action.ERROR.name())
              .setBody(JsonUtil.toJson(new BodyError(e)));
    }

    // Send the response.
    getNode().sendMessage(result);
  }

  /**
   * Registers a client.
   *
   * @param message The incoming message.
   * @return The client info if the client has been newly registered, null otherwise.
   * @throws IllegalStateException If the client is not registered yet and the max number of
   *     registered clients is reached.
   */
  private ClientInfo registerClient(MessageDto message) {

    ClientInfo clientInfo =
        new ClientInfo(message.getApiLevel(), message.getClientNodeId(), message.getSessionId());

    if (message.getLocalReaderName() != null) {
      // Reader command
      synchronized (readerClientsMonitor) {
        Set<ClientInfo> readerClientInfos = readerClients.get(message.getLocalReaderName());
        if (readerClientInfos == null) {
          readerClientInfos = Collections.newSetFromMap(new ConcurrentHashMap<>(1));
          readerClients.put(message.getLocalReaderName(), readerClientInfos);
        }
        return addClient(readerClientInfos, clientInfo);
      }
    } else {
      // Plugin command
      return addClient(pluginClients, clientInfo);
    }
  }

  /**
   * Adds a client to the provided set if it is not already present.
   *
   * @param clientInfos The set of registered clients.
   * @param clientInfo The client info to add.
   * @return The client info if the client has been newly added, null otherwise.
   * @throws IllegalStateException If the client is not present and the max number of registered
   *     clients is reached.
   */
  private ClientInfo addClient(Set<ClientInfo> clientInfos, ClientInfo clientInfo) {
    if (clientInfos.contains(clientInfo)) {
      return null;
    }
    if (clientInfos.size() >= MAX_REGISTERED_CLIENTS) {
      purgeInactiveClients();
    }
    if (clientInfos.size() >= MAX_REGISTERED_CLIENTS) {
      throw new IllegalStateException(
          "Max number of registered clients reached ("
              + MAX_REGISTERED_CLIENTS
              + "): client node ID '"
              + clientInfo.clientNodeId
              + "' rejected");
    }
    return clientInfos.add(clientInfo) ? clientInfo : null;
  }

  /**
   * Unregisters a client and removes the reader entry if it no longer has any client.
   *
   * @param localReaderName The local reader name (null for a plugin command).
   * @param clientInfo The client info to remove.
   */
  private void unregisterClient(String localReaderName, ClientInfo clientInfo) {
    if (localReaderName != null) {
      synchronized (readerClientsMonitor) {
        Set<ClientInfo> readerClientInfos = readerClients.get(localReaderName);
        if (readerClientInfos != null) {
          readerClientInfos.remove(clientInfo);
          if (readerClientInfos.isEmpty()) {
            readerClients.remove(localReaderName);
          }
        }
      }
    } else {
      pluginClients.remove(clientInfo);
    }
  }

  /**
   * Removes the clients registered for a while which are no longer active according to the node, at
   * most once per purge period.
   */
  private void purgeInactiveClients() {
    long now = System.currentTimeMillis();
    if (now < lastPurgeDatetime + PURGE_PERIOD_MILLIS) {
      return;
    }
    synchronized (purgeMonitor) {
      if (now < lastPurgeDatetime + PURGE_PERIOD_MILLIS) {
        return;
      }
      lastPurgeDatetime = now;
    }
    int nbPurgedClients = purgeInactiveClients(pluginClients, now);
    for (Set<ClientInfo> readerClientInfos : readerClients.values()) {
      nbPurgedClients += purgeInactiveClients(readerClientInfos, now);
    }
    logger.info("Inactive clients purged [localService={}, count={}]", getName(), nbPurgedClients);
  }

  /**
   * Removes the inactive clients of the provided set.
   *
   * @param clientInfos The set of registered clients.
   * @param now The current datetime (in milliseconds).
   * @return The number of removed clients.
   */
  private int purgeInactiveClients(Set<ClientInfo> clientInfos, long now) {
    int nbPurgedClients = 0;
    Iterator<ClientInfo> iterator = clientInfos.iterator();
    while (iterator.hasNext()) {
      ClientInfo clientInfo = iterator.next();
      if (now - clientInfo.registrationDatetime > MIN_REGISTRATION_DURATION_MILLIS
          && !getNode().isClientActive(clientInfo.clientNodeId, clientInfo.sessionId)) {
        iterator.remove();
        nbPurgedClients++;
      }
    }
    return nbPurgedClients;
  }

  /** Client info. */
  private static class ClientInfo {

    private final int clientDistributedApiLevel;
    private final String clientNodeId;
    private final String sessionId;
    private final long registrationDatetime;

    private ClientInfo(int clientDistributedApiLevel, String clientNodeId, String sessionId) {
      this.clientDistributedApiLevel = clientDistributedApiLevel;
      this.clientNodeId = clientNodeId;
      this.sessionId = sessionId;
      this.registrationDatetime = System.currentTimeMillis();
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
