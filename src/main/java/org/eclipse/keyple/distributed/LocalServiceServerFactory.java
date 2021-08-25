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

import org.eclipse.keyple.core.common.KeypleDistributedLocalServiceExtensionFactory;

/**
 * Factory of {@link LocalServiceServer} to provide to the Keyple main service during the
 * registration process.
 *
 * @since 2.0.0
 */
public interface LocalServiceServerFactory extends KeypleDistributedLocalServiceExtensionFactory {}
