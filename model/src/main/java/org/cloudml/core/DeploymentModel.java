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
package org.cloudml.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudml.core.validation.CanBeValidated;
import org.cloudml.core.validation.Report;
import org.cloudml.core.visitors.Visitable;
import org.cloudml.core.visitors.Visitor;

public class DeploymentModel extends WithProperties implements Visitable, CanBeValidated {

    private final ProviderGroup providers;
    private final NodeTypeGroup nodeTypes;
    private final ArtefactTypeGroup artefactTypes;
    private final BindingTypeGroup bindingTypes;
    private final NodeInstanceGroup nodeInstances;
    private final ArtefactInstanceGroup artefactInstances;
    private List<BindingInstance> bindingInstances = new LinkedList<BindingInstance>();

    public DeploymentModel() {
        this.providers = new ProviderGroup(this);
        this.nodeTypes = new NodeTypeGroup(this);
        this.artefactTypes = new ArtefactTypeGroup(this);
        this.bindingTypes = new BindingTypeGroup(this);
        this.nodeInstances = new NodeInstanceGroup(this);
        this.artefactInstances = new ArtefactInstanceGroup(this);
    }

    public DeploymentModel(String name) {
        super(name);
        this.providers = new ProviderGroup(this);
        this.nodeTypes = new NodeTypeGroup(this);
        this.artefactTypes = new ArtefactTypeGroup(this);
        this.bindingTypes = new BindingTypeGroup(this);
        this.nodeInstances = new NodeInstanceGroup(this);
        this.artefactInstances = new ArtefactInstanceGroup(this);
    }

    @Deprecated
    public DeploymentModel(String name, List<Property> properties,
                           Map<String, Artefact> artefactTypes, List<ArtefactInstance> artefactInstances,
                           Map<String, Node> nodeTypes, List<NodeInstance> nodeInstances, List<Provider> providers) {
        super(name, properties);
        this.providers = new ProviderGroup(this);
        this.nodeTypes = new NodeTypeGroup(this);
        this.artefactTypes = new ArtefactTypeGroup(this);
        this.bindingTypes = new BindingTypeGroup(this);
        this.nodeInstances = new NodeInstanceGroup(this);
        this.artefactInstances = new ArtefactInstanceGroup(this);
    }

    @Deprecated
    public DeploymentModel(String name, List<Property> properties,
                           Map<String, Artefact> artefactTypes, List<ArtefactInstance> artefactInstances,
                           Map<String, Node> nodeTypes, List<NodeInstance> nodeInstances, List<Provider> providers, Map<String, Binding> bindingTypes, List<BindingInstance> bindingInstances) {
        super(name, properties);
        this.providers = new ProviderGroup(this);
        this.nodeTypes = new NodeTypeGroup(this);
        this.artefactTypes = new ArtefactTypeGroup(this);
        this.bindingTypes = new BindingTypeGroup(this);
        this.nodeInstances = new NodeInstanceGroup(this);
        this.artefactInstances = new ArtefactInstanceGroup(this);
        this.bindingInstances = bindingInstances;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDeploymentModel(this);
    }

    @Override
    public Report validate() {
        Report validation = new Report();
        if (isEmpty()) {
            validation.addWarning("empty deployment model");
        }
        return validation;
    }

    public boolean isEmpty() {
        return this.providers.isEmpty()
                && this.nodeTypes.isEmpty()
                && this.artefactTypes.isEmpty()
                && this.bindingTypes.isEmpty()
                && this.nodeInstances.isEmpty()
                && this.artefactInstances.isEmpty()
                && this.bindingInstances.isEmpty();
    }

   // Providers
    public ProviderGroup getProviders() {
        return providers;
    }

    public boolean isUsed(Provider provider) {
        return !getNodeTypes().providedBy(provider).isEmpty();
    }

    
    // Node types
    public NodeTypeGroup getNodeTypes() {
        return this.nodeTypes;
    }

    public boolean isUsed(Node node) {
        return !getNodeInstances().ofType(node).isEmpty();
    }

    // Artefact types
    public ArtefactTypeGroup getArtefactTypes() {
        return artefactTypes;
    }

    public boolean isUsed(Artefact artefact) {
        return !getArtefactInstances().ofType(artefact).isEmpty();
    }

    
    // Binding Types
    public BindingTypeGroup getBindingTypes() {
        return bindingTypes;
    }

    // Node Instances
    public NodeInstanceGroup getNodeInstances() {
        return nodeInstances;
    }


    // Artefact Instances
    public ArtefactInstanceGroup getArtefactInstances() {
        return artefactInstances;
    }

    
    public boolean isUsed(ArtefactInstance server) {
        boolean found = false;
        final Iterator<ServerPortInstance> iterator = server.getProvided().iterator();
        while (iterator.hasNext() && !found) {
            found = isBound(iterator.next());
        }
        return found;
    }
    

    // Bindings instances
    public void setBindingInstances(List<BindingInstance> bindingInstances) {
        this.bindingInstances = bindingInstances;
    }

    public List<BindingInstance> getBindingInstances() {
        return bindingInstances;
    }

    public void addBindingInstance(BindingInstance bindingToAdd) {
        this.bindingInstances.add(bindingToAdd);
    }

    public void removeBindingInstance(BindingInstance bindingToRemove) {
        this.bindingInstances.remove(bindingToRemove);
    }

    public BindingInstance findBindingInstanceByName(String bindingInstanceName) {
        return findByName(bindingInstanceName, this.bindingInstances);
    }

    public List<BindingInstance> findBindingInstancesByPort(ArtefactPortInstance<? extends ArtefactPort> port) {
        final ArrayList<BindingInstance> selection = new ArrayList<BindingInstance>();
        for (BindingInstance binding : bindingInstances) {
            if (binding.eitherEndIs(port)) {
                selection.add(binding);
            }
        }
        return selection;
    }

    public List<BindingInstance> findBindingInstancesByClientEnd(ClientPortInstance cpi) {
        return findBindingInstancesByPort(cpi);
    }

    public List<BindingInstance> findBindingInstancesByServerEnd(ServerPortInstance cpi) {
        return findBindingInstancesByPort(cpi);
    }

    public boolean isBound(ArtefactPortInstance<? extends ArtefactPort> port) {
        return !findBindingInstancesByPort(port).isEmpty();
    }

    public ServerPortInstance findServerPort(ClientPortInstance clientPort) {
        final List<BindingInstance> bindings = findBindingInstancesByPort(clientPort);
        if (bindings.isEmpty()) {
            final String message = String.format("client port '%s' is not yet bound to any server", clientPort.getName());
            throw new IllegalArgumentException(message);
        }
        return bindings.get(0).getServer();
    }

    public List<ClientPortInstance> findClientPorts(ServerPortInstance serverPort) {
        final List<BindingInstance> bindings = findBindingInstancesByPort(serverPort);
        if (bindings.isEmpty()) {
            final String message = String.format("server port '%s' is not yet bound to any server", serverPort.getName());
            throw new IllegalArgumentException(message);
        }
        final List<ClientPortInstance> clients = new ArrayList<ClientPortInstance>();
        for (BindingInstance binding : bindings) {
            clients.add(binding.getClient());
        }
        return clients;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DeploymentModel) {
            DeploymentModel otherDepModel = (DeploymentModel) other;
            return artefactTypes.equals(otherDepModel.artefactTypes) && artefactInstances.equals(otherDepModel.artefactInstances)
                    && nodeTypes.equals(otherDepModel.nodeTypes) && nodeInstances.equals(otherDepModel.nodeInstances)
                    && bindingTypes.equals(otherDepModel.bindingTypes) && bindingInstances.equals(otherDepModel.bindingInstances);
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Deployment model " + getName() + "{\n");
        builder.append("- Artefact types: {\n");
        for (Artefact t : artefactTypes) {
            builder.append("  - " + t + "\n");
        }
        builder.append("}\n");
        builder.append("- Binding types: {\n");
        for (Binding b : bindingTypes) {
            builder.append("  - " + b + "\n");
        }
        builder.append("}\n");
        builder.append("- Artefact instances: {\n");
        for (ArtefactInstance i : artefactInstances) {
            builder.append("  - " + i + "\n");
        }
        builder.append("}\n");
        builder.append("- Binding instances: {\n");
        for (BindingInstance b : bindingInstances) {
            builder.append("  - " + b + "\n");
        }
        builder.append("}\n");
        builder.append("- Node types: {\n");
        for (Node nt : nodeTypes) {
            builder.append("  - " + nt + "\n");
        }
        builder.append("}\n");
        builder.append("- Node instances: {\n");
        for (NodeInstance ni : nodeInstances) {
            builder.append("  - " + ni + "\n");
        }
        builder.append("}\n");
        return builder.toString();
    }
}