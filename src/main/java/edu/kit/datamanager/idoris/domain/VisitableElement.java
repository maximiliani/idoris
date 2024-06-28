/*
 * Copyright (c) 2024 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.domain;

import edu.kit.datamanager.idoris.visitors.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VisitableElement {
    private static final Logger LOG = LoggerFactory.getLogger(VisitableElement.class);

//    private final Set<String> visitedBy = new HashSet<>();

    public <T> T execute(Visitor<T> visitor, Object... args) {
//        if (isVisitedBy(visitor)) {
//            LOG.info("Class {} already visited by visitor {}. ABORTING!", this.getClass().getName(), visitor.getClass().getName());
//            return null;
//        } else {
        LOG.info("Visiting class {} with visitor {}", this.getClass().getName(), visitor.getClass().getName());
//            visitedBy.add(visitor.getClass().getName());
        return accept(visitor, args);
//        }
    }

    protected abstract <T> T accept(Visitor<T> visitor, Object... args);

//    boolean isVisitedBy(Visitor<?> visitor) {
//        return visitedBy.contains(visitor.getClass().getName());
//    }
//
//    Set<String> getVisitedBy() {
//        return visitedBy;
//    }
//
//    void clearVisitedBy() {
//        visitedBy.clear();
//    }
}