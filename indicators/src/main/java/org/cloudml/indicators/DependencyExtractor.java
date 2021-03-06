/**
 * This file is part of CloudML [ http://cloudml.org ]
 *
 * Copyright (C) 2012 - SINTEF ICT
 * Contact: Franck Chauvel <franck.chauvel@sintef.no>
 *
 * Module: root
 *
 * CloudML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * CloudML is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with CloudML. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.cloudml.indicators;

import eu.diversify.trio.core.requirements.Requirement;
import org.cloudml.core.ComponentInstance;

/**
 * Extract the requirement of a single CloudML component.
 *
 * This a strategy object, used to configure the TrioExporter. For the record, a
 * requirement in TRIO is a logical expression that describe the condition under
 * which a given component fails, when failure occur in its direct environment.
 */
public interface DependencyExtractor {

    /**
     * Compute the TRIO requirement associated with a single CloudML component
     * instance.
     *
     * @param componentInstance the component instance whose requirement are
     * needed.
     *
     * @return the associated requirement object.
     */
    Requirement from(ComponentInstance<?> componentInstance);

}
