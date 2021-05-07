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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.common.KeypleReaderEvent;
import org.eclipse.keyple.core.common.KeypleSmartCard;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.eclipse.keyple.distributed.spi.ReaderEventFilterSpi;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Adapter of {@link LocalServiceClient}.
 *
 * @since 2.0
 */
final class LocalServiceClientAdapter extends AbstractLocalServiceAdapter
    implements LocalServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceClientAdapter.class);

  private final boolean withReaderObservation;
  private final ReaderEventFilterSpi readerEventFilterSpi;
  private final Map<String, String> localReaderNameToRemoteReaderNameMap;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @param syncEndpointClientSpi The sync endpoint client to bind.
   * @param asyncEndpointClientSpi The async endpoint client to bind.
   * @param asyncNodeClientTimeoutSeconds The async node client timeout (in seconds).
   * @param withReaderObservation With reader observation ?
   * @param readerEventFilterSpi The optional reader event filter to use if reader observation is
   *     requested.
   * @since 2.0
   */
  LocalServiceClientAdapter(
      String localServiceName,
      SyncEndpointClientSpi syncEndpointClientSpi,
      AsyncEndpointClientSpi asyncEndpointClientSpi,
      int asyncNodeClientTimeoutSeconds,
      boolean withReaderObservation,
      ReaderEventFilterSpi readerEventFilterSpi) {

    super(localServiceName);
    this.withReaderObservation = withReaderObservation;
    this.readerEventFilterSpi = readerEventFilterSpi;
    this.localReaderNameToRemoteReaderNameMap = new ConcurrentHashMap<String, String>();

    if (syncEndpointClientSpi != null) {
      logger.info(
          "Create a new 'LocalServiceClient' with name='{}', nodeType='SyncNodeClient', withReaderObservation={}, withReaderEventFilter={}.",
          localServiceName,
          withReaderObservation,
          readerEventFilterSpi != null);
      bindSyncNodeClient(syncEndpointClientSpi, null, null);
    } else {
      logger.info(
          "Create a new 'LocalServiceClient' with name='{}', nodeType='AsyncNodeClient', timeoutSeconds={}, withReaderObservation={}, withReaderEventFilter={}.",
          localServiceName,
          asyncNodeClientTimeoutSeconds,
          withReaderObservation,
          readerEventFilterSpi != null);
      bindAsyncNodeClient(asyncEndpointClientSpi, asyncNodeClientTimeoutSeconds);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public AsyncNodeClient getAsyncNode() {
    AbstractNodeAdapter node = getNode();
    if (node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) node;
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
  public <T> T executeRemoteService(
      RemoteServiceParameters parameters, Class<T> classOfUserOutputData) {

    // Check params.
    Assert.getInstance()
        .notNull(parameters, "parameters")
        .notNull(classOfUserOutputData, "classOfUserOutputData");

    // Check the local reader registration and observation compliance.
    boolean isReaderObservable =
        getLocalServiceApi().isReaderObservable(parameters.getLocalReaderName());

    if (withReaderObservation && !isReaderObservable) {
      throw new IllegalArgumentException(
          String.format(
              "Reader observation can not be activated because local reader '%s' is not observable.",
              parameters.getLocalReaderName()));
    }

    // Generate a new session ID.
    String sessionId = generateSessionId();

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Start execution of remote service '{}' for local reader '{}' with session ID '{}'.",
          parameters.getServiceId(),
          parameters.getLocalReaderName(),
          sessionId);
    }

    // Build the message DTO.
    MessageDto message = buildServiceMessage(parameters, sessionId, isReaderObservable);

    T userOutputData;
    try {
      // Open a new session on the node.
      getNode().openSession(sessionId);

      // Send the first message.
      message = getNode().sendRequest(message);

      // Start reader observation if requested.
      if (withReaderObservation) {

        // Register the remote reader name associated to the local reader name.
        localReaderNameToRemoteReaderNameMap.put(
            message.getLocalReaderName(), message.getRemoteReaderName());

        // Start the observation.
        getLocalServiceApi().startReaderObservation(message.getLocalReaderName());
      }

      // Process the entire transaction.
      message = processTransaction(message);

      // Extract user output data from last received message.
      userOutputData = extractUserOutputData(message, classOfUserOutputData);

    } finally {
      getNode().closeSessionSilently(sessionId);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Finish execution of remote service '{}' for local reader '{}' with session ID '{}'.",
          parameters.getServiceId(),
          parameters.getLocalReaderName(),
          sessionId);
    }

    return userOutputData;
  }

  /**
   * (private)<br>
   * Builds a message associated to the {@link Action#EXECUTE_REMOTE_SERVICE} action.
   *
   * @param parameters The main parameters.
   * @param sessionId The session ID to use.
   * @param isReaderObservable Is local reader observable ?
   * @return A not null reference.
   */
  private MessageDto buildServiceMessage(
      RemoteServiceParameters parameters, String sessionId, boolean isReaderObservable) {

    JsonObject body = new JsonObject();

    // Service ID
    body.addProperty(JsonProperty.SERVICE_ID.name(), parameters.getServiceId());

    // User input data
    Object userInputData = parameters.getUserInputData();
    if (userInputData != null) {
      body.add(JsonProperty.USER_INPUT_DATA.name(), JsonUtil.getParser().toJsonTree(userInputData));
    }

    // Initial card content
    KeypleSmartCard initialCardContent = parameters.getInitialCardContent();
    if (initialCardContent != null) {
      body.add(
          JsonProperty.INITIAL_CARD_CONTENT.name(),
          JsonUtil.getParser().toJsonTree(initialCardContent));
      body.addProperty(
          JsonProperty.INITIAL_CARD_CONTENT_CLASS_NAME.name(),
          initialCardContent.getClass().getName());
    }

    // Is reader observable ?
    body.addProperty(
        JsonProperty.IS_READER_OBSERVABLE.name(), withReaderObservation && isReaderObservable);

    return new MessageDto()
        .setSessionId(sessionId)
        .setAction(Action.EXECUTE_REMOTE_SERVICE.name())
        .setLocalReaderName(parameters.getLocalReaderName())
        .setBody(body.toString());
  }

  /**
   * (private)<br>
   * Process the entire transaction.<br>
   * Check server response : loop while message action is not a terminate service or an error, then
   * execute the command locally and send back response to the server.
   *
   * @param message The first message received from the server.
   * @return A not null reference.
   * @throws RuntimeException If an error occurs.
   */
  private MessageDto processTransaction(MessageDto message) {

    while (!message.getAction().equals(Action.END_REMOTE_SERVICE.name())
        && !message.getAction().equals(Action.ERROR.name())) {

      try {
        // Execute the command locally.
        String jsonResult =
            getLocalServiceApi().executeLocally(message.getBody(), message.getLocalReaderName());

        // Build the response to send back to the server.
        message.setAction(Action.RESP.name()).setBody(jsonResult);

      } catch (IllegalStateException e) {
        // Build the error response to send back to the client.
        message.setAction(MessageDto.Action.ERROR.name()).setBody(JsonUtil.toJson(e));
      }

      // Send the response and get the next command to process.
      message = getNode().sendRequest(message);
    }

    // Check if the last received message contains an error.
    checkError(message);

    return message;
  }

  /**
   * (private)<br>
   * Checks if the provided message contains an error.
   *
   * @param message The message to check.
   * @throws RuntimeException If the message contains an error.
   */
  private void checkError(MessageDto message) {
    if (message.getAction().equals(Action.ERROR.name())) {
      throw new RuntimeException( // NOSONAR
          JsonUtil.getParser().fromJson(message.getBody(), BodyError.class).getException());
    }
  }

  /**
   * (private)<br>
   * Extracts the user output data from the provided message if configured.
   *
   * @param msg The message.
   * @param classOfUserOutputData The class of the user output data.
   * @param <T> The type of the output data.
   * @return Null if there is no user data to extract.
   */
  private <T> T extractUserOutputData(MessageDto msg, Class<T> classOfUserOutputData) {
    if (classOfUserOutputData == null) {
      return null;
    }
    Gson parser = JsonUtil.getParser();
    String userOutputJsonData =
        parser
            .fromJson(msg.getBody(), JsonObject.class)
            .get(JsonProperty.USER_OUTPUT_DATA.name())
            .getAsString();
    return parser.fromJson(userOutputJsonData, classOfUserOutputData);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onReaderEvent(String readerName, String jsonData, KeypleReaderEvent readerEvent) {

    // Apply the user's filter if is set and get the optional user input data to send to the server.
    Object userInputData = null;
    if (readerEventFilterSpi != null) {
      try {
        userInputData = readerEventFilterSpi.beforeEventBroadcast(readerEvent);
      } catch (CancelEventBroadcastException e) {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "User's reader event filter cancels the broadcast of the reader event : {}",
              jsonData);
        }
        return;
      }
    }

    // Generate a new session ID.
    String sessionId = generateSessionId();

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Start execution of reader event '{}' for local reader '{}' with session ID '{}'.",
          jsonData,
          readerName,
          sessionId);
    }

    try {
      // Build the message DTO.
      MessageDto message = buildReaderEventMessage(readerName, jsonData, userInputData, sessionId);

      // Open a new session on the node.
      getNode().openSession(sessionId);

      // Send the first message.
      message = getNode().sendRequest(message);

      // Process the entire transaction.
      message = processTransaction(message);

      // If user's filter is set, then extract optional user output data from last received message.
      if (readerEventFilterSpi != null) {

        Object userOutputData =
            extractUserOutputData(message, readerEventFilterSpi.getUserOutputDataClass());

        // Invoke filter callback.
        readerEventFilterSpi.afterEventBroadcast(userOutputData);
      }

    } catch (RuntimeException e) {
      // If user's filter is set, then notify the error and check if observation must be stopped.
      if (readerEventFilterSpi != null) {
        boolean stopObservation = readerEventFilterSpi.onEventBroadcastError(e);
        if (stopObservation) {
          getLocalServiceApi().stopReaderObservation(readerName);
        }
      }
      throw e;

    } finally {
      getNode().closeSessionSilently(sessionId);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Finish execution of reader event '{}' for local reader '{}' with session ID '{}'.",
          jsonData,
          readerName,
          sessionId);
    }
  }

  /**
   * (private)<br>
   * Builds a message associated to the {@link Action#READER_EVENT} action.
   *
   * @param localReaderName The name of the local reader.
   * @param jsonData The JSON representation of the reader event.
   * @param userInputData The optional user input data.
   * @param sessionId The session ID
   * @return A not null reference.
   */
  private MessageDto buildReaderEventMessage(
      String localReaderName, String jsonData, Object userInputData, String sessionId) {

    JsonObject body = new JsonObject();

    // Reader event
    body.addProperty(JsonProperty.READER_EVENT.name(), jsonData);

    // User input data
    if (userInputData != null) {
      body.add(JsonProperty.USER_INPUT_DATA.name(), JsonUtil.getParser().toJsonTree(userInputData));
    }

    return new MessageDto()
        .setSessionId(sessionId)
        .setAction(Action.READER_EVENT.name())
        .setLocalReaderName(localReaderName)
        .setRemoteReaderName(localReaderNameToRemoteReaderNameMap.get(localReaderName))
        .setBody(body.toString());
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  void onMessage(MessageDto messageDto) {
    throw new UnsupportedOperationException("onMessage");
  }
}
