/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.web.v1;

import edu.kit.datamanager.idoris.dao.IDataTypeDao;
import edu.kit.datamanager.idoris.dao.IOperationDao;
import edu.kit.datamanager.idoris.domain.entities.Operation;
import edu.kit.datamanager.idoris.rules.validation.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RepositoryRestController
public class OperationController {
    @Autowired
    IOperationDao operationDao;

    @Autowired
    IDataTypeDao dataTypeDao;

    @GetMapping("v1/operations/{pid}/validate")
    public ResponseEntity<?> validate(@PathVariable("pid") String pid) {
        Operation operation = operationDao.findById(pid).orElseThrow();
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult result = operation.execute(validator);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(218).body(result);
        }
    }
}
