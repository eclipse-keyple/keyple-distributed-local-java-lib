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

import org.eclipse.keyple.core.common.KeypleSmartCard;
import org.eclipse.keyple.core.util.Assert;

/**
 * This POJO contains the <b>parameters</b> of the method {@link
 * LocalServiceClient#executeRemoteService(RemoteServiceParameters, Class)}.
 *
 * <ul>
 *   <li><b>serviceId</b> : The ticketing service ID. It will permit to indicate to the server which
 *       ticketing service to execute (Materialization, Validation, Control, etc...). This field is
 *       free and is for the user's use only.
 *   <li><b>localReaderName</b> : The name of the local reader to manage remotely from the server.
 *   <li><b>userInputData</b> (optional) : A DTO containing the user input data if you want to
 *       transmit personal data to the remote ticketing service.
 *   <li><b>initialCardContent</b> (optional) : A {@link KeypleSmartCard} containing the initial
 *       smart card content to transmit to the remote ticketing service.
 * </ul>
 *
 * @since 2.0
 */
public final class RemoteServiceParameters {

  private final String serviceId;
  private final String localReaderName;
  private final Object userInputData;
  private final KeypleSmartCard initialCardContent;

  private RemoteServiceParameters(Builder builder) {
    serviceId = builder.serviceId;
    localReaderName = builder.localReaderName;
    userInputData = builder.userInputData;
    initialCardContent = builder.initialCardContent;
  }

  /**
   * Gets a builder to use in order to create a new instance.
   *
   * @param serviceId The ticketing service ID. It will permit to indicate to the server which
   *     ticketing service to execute (Materialization, Validation, Control, etc...). This field is
   *     free and is for the user's use only.
   * @param localReaderName The name of the local reader to manage remotely from the server.
   * @return A not null reference.
   * @throws IllegalArgumentException If the service ID or the reader name are null or empty.
   * @since 2.0
   */
  public static Builder builder(String serviceId, String localReaderName) {
    return new Builder(serviceId, localReaderName);
  }

  /**
   * Builder of {@link RemoteServiceParameters}.
   *
   * @since 2.0
   */
  public static final class Builder {

    private final String serviceId;
    private final String localReaderName;
    private Object userInputData;
    private KeypleSmartCard initialCardContent;

    private Builder(String serviceId, String localReaderName) {
      Assert.getInstance()
          .notEmpty(serviceId, "serviceId")
          .notEmpty(localReaderName, "localReaderName");
      this.serviceId = serviceId;
      this.localReaderName = localReaderName;
    }

    /**
     * Adds a DTO containing user input data to transmit to the remote ticketing service.
     *
     * @param userInputData The object containing the user input data.
     * @return The current builder instance.
     * @since 2.0
     */
    public Builder withUserInputData(Object userInputData) {
      this.userInputData = userInputData;
      return this;
    }

    /**
     * Adds an {@link KeypleSmartCard} containing the initial smart card content to transmit to the
     * remote ticketing service.
     *
     * @param initialCardContent The initial smart card content.
     * @return The current builder instance.
     * @since 2.0
     */
    public Builder withInitialCardContent(KeypleSmartCard initialCardContent) {
      this.initialCardContent = initialCardContent;
      return this;
    }

    /**
     * Creates a new instance of {@link RemoteServiceParameters} using the current configuration.
     *
     * @return A not null reference.
     * @since 2.0
     */
    public RemoteServiceParameters build() {
      return new RemoteServiceParameters(this);
    }
  }

  /**
   * (package-private)<br>
   * Gets the ticketing service ID.
   *
   * @return a not empty string.
   * @since 2.0
   */
  String getServiceId() {
    return serviceId;
  }

  /**
   * (package-private)<br>
   * Gets the name of the local reader.
   *
   * @return a not empty string.
   * @since 2.0
   */
  String getLocalReaderName() {
    return localReaderName;
  }

  /**
   * (package-private)<br>
   * Gets the user input data.
   *
   * @return Null if no user input data is set.
   * @since 2.0
   */
  Object getUserInputData() {
    return userInputData;
  }

  /**
   * (package-private)<br>
   * Gets the initial smart card content.
   *
   * @return Null if no initial smart card content is set.
   * @since 2.0
   */
  KeypleSmartCard getInitialCardContent() {
    return initialCardContent;
  }
}
