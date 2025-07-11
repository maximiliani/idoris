/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.idoris.pids.client;

import edu.kit.datamanager.idoris.pids.client.model.PIDRecord;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/**
 * Client for the Typed PID Maker service.
 * This interface defines the operations for interacting with the service.
 */
@HttpExchange("/api/v1/pit/pid")
public interface TypedPIDMakerClient {

    /**
     * Creates a new PID record using the SimplePidRecord format.
     *
     * @param record The PID record to create
     * @return The created PID record
     */
    @PostExchange(value = "/", accept = "application/vnd.datamanager.pid.simple+json", contentType = "application/vnd.datamanager.pid.simple+json")
    PIDRecord createPIDRecord(PIDRecord record);


    /**
     * Gets a PID record by its PID using the SimplePidRecord format.
     *
     * @param pid The PID of the record to get
     * @return The PID record
     */
    @GetExchange(value = "/{pid}", accept = "application/vnd.datamanager.pid.simple+json")
    PIDRecord getPIDRecord(String pid);

    /**
     * Updates an existing PID record using the SimplePidRecord format.
     *
     * @param pid    The PID of the record to update
     * @param record The updated PID record
     * @return The updated PID record
     */
    @PutExchange(value = "/{pid}", accept = "application/vnd.datamanager.pid.simple+json", contentType = "application/vnd.datamanager.pid.simple+json")
    PIDRecord updatePIDRecord(String pid, PIDRecord record);
}
