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

import static org.eclipse.keyple.distributed.MessageDto.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.JsonUtil;
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

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param localServiceName The name of the local service to build.
   * @since 2.0
   */
  LocalServiceClientAdapter(String localServiceName) {

    super(localServiceName);
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
      String serviceId,
      String localReaderName,
      Object initialCardContent,
      Object inputData,
      Class<T> outputDataClass) {

    // Check params.
    Assert.getInstance()
        .notEmpty(serviceId, "serviceId")
        .notEmpty(localReaderName, "localReaderName");

    // Generate a new session ID.
    String sessionId = generateSessionId();

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Start execution of remote service '{}' for local reader '{}' with session ID '{}'.",
          serviceId,
          localReaderName,
          sessionId);
    }

    // Build the message DTO.
    MessageDto message =
        buildMessage(serviceId, localReaderName, initialCardContent, inputData, sessionId);

    T outputData;
    try {
      // Open a new session on the node.
      getNode().openSession(sessionId);

      // Send the first message.
      message = getNode().sendRequest(message);

      // Process the entire transaction.
      message = processTransaction(message);

      // Extract output data from last received message.
      outputData = extractOutputData(message, outputDataClass);

    } finally {
      getNode().closeSessionSilently(sessionId);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Finish execution of remote service '{}' for local reader '{}' with session ID '{}'.",
          serviceId,
          localReaderName,
          sessionId);
    }

    return outputData;
  }

  /**
   * (private)<br>
   * Builds a message associated to the {@link Action#EXECUTE_REMOTE_SERVICE} action.
   *
   * @param serviceId The ticketing service ID.
   * @param localReaderName The name of the local reader.
   * @param initialCardContent The initial card content if needed.
   * @param inputData The additional information if needed.
   * @param sessionId The session ID to use.
   * @return A not null reference.
   */
  private MessageDto buildMessage(
      String serviceId,
      String localReaderName,
      Object initialCardContent,
      Object inputData,
      String sessionId) {

    JsonObject body = new JsonObject();

    // Service ID
    body.addProperty(JsonProperty.SERVICE_ID.name(), serviceId);

    // Initial card content
    if (initialCardContent != null) {
      body.add(
          JsonProperty.INITIAL_CARD_CONTENT.name(),
          JsonUtil.getParser().toJsonTree(initialCardContent));
      body.addProperty(
          JsonProperty.INITIAL_CARD_CONTENT_CLASS_NAME.name(),
          initialCardContent.getClass().getName());
    }

    // Input data
    if (inputData != null) {
      body.add(JsonProperty.INPUT_DATA.name(), JsonUtil.getParser().toJsonTree(inputData));
    }

    return new MessageDto()
        .setSessionId(sessionId)
        .setAction(Action.EXECUTE_REMOTE_SERVICE.name())
        .setLocalReaderName(localReaderName)
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
   * Extracts the output data from the provided message if configured.
   *
   * @param message The message.
   * @param outputDataClass The class of the output data.
   * @param <T> The type of the output data.
   * @return Null if there is no output data to extract.
   */
  private <T> T extractOutputData(MessageDto message, Class<T> outputDataClass) {
    if (outputDataClass == null) {
      return null;
    }
    Gson parser = JsonUtil.getParser();
    String outputDataJson =
        parser
            .fromJson(message.getBody(), JsonObject.class)
            .get(JsonProperty.OUTPUT_DATA.name())
            .getAsString();
    return parser.fromJson(outputDataJson, outputDataClass);
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onPluginEvent(String readerName, String jsonData) {
    throw new UnsupportedOperationException("onPluginEvent");
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  public void onReaderEvent(String readerName, String jsonData) {
    throw new UnsupportedOperationException("onReaderEvent");
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
