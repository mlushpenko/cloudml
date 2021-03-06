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
package org.cloudml.core.actions;

import org.cloudml.core.*;

public class Migrate extends AbstractAction<InternalComponentInstance> {

    private final InternalComponentInstance instance;

    public Migrate(StandardLibrary library, InternalComponentInstance instance) {
        super(library);
        this.instance = rejectIfInvalid(instance);
    }

    private InternalComponentInstance rejectIfInvalid(InternalComponentInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("'null' is not a valid instance for migration!");
        }
        return instance;
    }

    @Override
    public InternalComponentInstance applyTo(Deployment deployment) {
        final ComponentInstance<? extends Component> newHost = getLibrary().findAlternativeDestinationFor(deployment, instance);
        final ExecuteInstance execution = deployment.getExecuteInstances().withSubject(instance);
        assert execution != null:
               String.format("There should be an execute instance whose required end points to '%s'", instance.getName());
        deployment.getExecuteInstances().remove(execution);
        deployment.deploy(instance, newHost);
        return instance;
    }

   
}
