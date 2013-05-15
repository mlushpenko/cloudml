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
package org.cloudml.codecs;

import java.util.HashMap;
import java.util.Map;
import org.cloudml.core.*;

/*
 * A bridge to translate CloudML POJOs into a KMF representation (which then
 * offers XMI and JSON serialization for free, with no deps) This stupid code
 * could go away if we decide to base the metamodel on KMF generated classes...
 */
public class KMFBridge {

    //TODO: BindingInstance
    public KMFBridge() {
    }

    public CloudMLElement toPOJO(net.cloudml.core.DeploymentModel kDeploy) {
        Map<String, Node> nodes = new HashMap<String, Node>();
        Map<String, NodePort> nodePorts = new HashMap<String, NodePort>();
        Map<String, Provider> providers = new HashMap<String, Provider>();
        Map<String, Artefact> artefacts = new HashMap<String, Artefact>();
        Map<String, ArtefactPort> artefactPorts = new HashMap<String, ArtefactPort>();
        Map<String, ArtefactInstance> artefactInstances = new HashMap<String, ArtefactInstance>();
        Map<String, ArtefactPortInstance> artefactPortInstances = new HashMap<String, ArtefactPortInstance>();
        Map<String, NodeInstance> nodeInstances = new HashMap<String, NodeInstance>();
        Map<String, NodePortInstance> nodePortInstances = new HashMap<String, NodePortInstance>();
        Map<String, Binding> bindings = new HashMap<String, Binding>();

        DeploymentModel model = new DeploymentModel(kDeploy.getName());

        for (net.cloudml.core.Provider kProvider : kDeploy.getProviders()) {
            Provider p = new Provider(kProvider.getName(), kProvider.getCredentials());
            initProperties(kProvider, p);
            model.getProviders().add(p);
            providers.put(p.getName(), p);
        }

        for (net.cloudml.core.Node kNode : kDeploy.getNodeTypes()) {
            Node node = new Node(kNode.getName());
            initProperties(kNode, node);

            Provider p = providers.get(kNode.getCloudProvider().getName());

            node.setProvider(p);
            node.setGroupName(kNode.getGroupName());
            node.setImageId(kNode.getImageID());
            node.setIs64os(kNode.getIs64os());
            node.setLocation(kNode.getLocation());
            node.setMinCore(kNode.getMinCore());
            node.setMinDisk(kNode.getMinDisk());
            node.setMinRam(kNode.getMinRam());
            node.setOS(kNode.getOS());
            node.setPrivateKey(kNode.getPrivateKey());
            node.setSecurityGroup(kNode.getSecurityGroup());
            node.setSshKey(kNode.getSshKey());

            nodes.put(node.getName(), node);

            for (net.cloudml.core.NodePort knp : kNode.getProvided()) {
                NodePort np = new NodePort(knp.getName(), node);
                initProperties(kNode, node);
                node.getProvided().add(np);

                nodePorts.put(np.getName(), np);
            }

            model.getNodeTypes().put(node.getName(), node);

        }

        for (net.cloudml.core.Artefact ka : kDeploy.getArtefactTypes()) {//first pass on the contained elements
            Artefact a = new Artefact(ka.getName());
            initProperties(ka, a);
            artefacts.put(a.getName(), a);

            for (net.cloudml.core.ArtefactPort kap : ka.getInputs()) {
                ArtefactPort ap = new ArtefactPort(kap.getName(), a);
                initProperties(kap, ap);
                ap.setPortNumber(kap.getPortNumber());
                a.getInputs().add(ap);

                artefactPorts.put(ap.getName(), ap);
            }

            for (net.cloudml.core.ArtefactPort kap : ka.getOutputs()) {
                ArtefactPort ap = new ArtefactPort(kap.getName(), a);
                initProperties(kap, ap);
                ap.setPortNumber(kap.getPortNumber());
                a.getOutputs().add(ap);

                artefactPorts.put(ap.getName(), ap);
            }

            for (net.cloudml.core.ArtefactPort kap : ka.getProvided()) {//TODO: duplicated code to be rationalized
                ArtefactPort ap = new ArtefactPort(kap.getName(), a);
                initProperties(kap, ap);
                ap.setPortNumber(kap.getPortNumber());
                a.getProvided().add(ap);//!

                artefactPorts.put(ap.getName(), ap);
            }

            Resource r = new Resource(ka.getResource().getName(), ka.getResource().getDeployingCommand(), ka.getResource().getRetrievingCommand(), ka.getResource().getConfigurationCommand(), ka.getResource().getStartCommand());

            a.setResource(r);


            model.getArtefactTypes().put(a.getName(), a);
        }

        for (net.cloudml.core.Artefact ka : kDeploy.getArtefactTypes()) {//second pass on the referenced elements
            Artefact a = artefacts.get(ka.getName());
            for (net.cloudml.core.ArtefactPort kap : ka.getRequired()) {
                ArtefactPort ap = artefactPorts.get(kap.getName());
                a.getRequired().add(ap);
            }

            if (ka.getDestination() != null) {
                a.setDestination(artefactPorts.get(ka.getDestination().getName()));
            }

        }

        for (net.cloudml.core.Binding kb : kDeploy.getBindingTypes()) {
            Binding b = new Binding(artefactPorts.get(kb.getClient().getName()), artefactPorts.get(kb.getServer().getName()));
            b.setName(kb.getName());
            if (kb.getClientResource() != null) {
                Resource cr = new Resource(kb.getClientResource().getName());
                if (kb.getClientResource().getDeployingCommand() != null) {
                    cr.setDeployingCommand(kb.getClientResource().getDeployingCommand());
                }
                if (kb.getClientResource().getRetrievingCommand() != null) {
                    cr.setRetrievingCommand(kb.getClientResource().getRetrievingCommand());
                }
                if (kb.getClientResource().getConfigurationCommand() != null) {
                    cr.setConfigurationCommand(kb.getClientResource().getConfigurationCommand());
                }
                if (kb.getClientResource().getStartCommand() != null) {
                    cr.setStartCommand(kb.getClientResource().getStartCommand());
                }
                b.setClientResource(cr);
            }
            if (kb.getServerResource() != null) {
                Resource cr = new Resource(kb.getServerResource().getName());
                if (kb.getServerResource().getDeployingCommand() != null) {
                    cr.setDeployingCommand(kb.getServerResource().getDeployingCommand());
                }
                if (kb.getServerResource().getRetrievingCommand() != null) {
                    cr.setRetrievingCommand(kb.getServerResource().getRetrievingCommand());
                }
                if (kb.getServerResource().getConfigurationCommand() != null) {
                    cr.setConfigurationCommand(kb.getServerResource().getConfigurationCommand());
                }
                if (kb.getServerResource().getStartCommand() != null) {
                    cr.setStartCommand(kb.getServerResource().getStartCommand());
                }
                b.setServerResource(cr);
            }
            model.getBindingTypes().put(b.getName(), b);
            bindings.put(b.getName(), b);
        }

        for (net.cloudml.core.NodeInstance kni : kDeploy.getNodeInstances()) {
            NodeInstance ni = new NodeInstance(kni.getName(), nodes.get(kni.getType().getName()));
            ni.setPublicAddress(kni.getPublicAddress());
            initProperties(kni, ni);

            nodeInstances.put(ni.getName(), ni);

            for (net.cloudml.core.NodePortInstance knpi : kni.getProvided()) {
                NodePortInstance npi = new NodePortInstance(knpi.getName(), nodePorts.get(knpi.getType().getName()), ni);
                initProperties(knpi, npi);
                ni.getProvided().add(npi);

                nodePortInstances.put(npi.getName(), npi);
            }

            model.getNodeInstances().add(ni);
        }


        for (net.cloudml.core.ArtefactInstance kai : kDeploy.getArtefactInstances()) {//pass1
            ArtefactInstance ai = new ArtefactInstance(kai.getName(), artefacts.get(kai.getType().getName()));
            initProperties(kai, ai);
            artefactInstances.put(ai.getName(), ai);

            if (kai.getDestination() != null) {
                ai.setDestination(nodePortInstances.get(kai.getDestination().getName()));
            }

            for (net.cloudml.core.ArtefactPortInstance kapi : kai.getInputs()) {
                ArtefactPortInstance api = new ArtefactPortInstance(kapi.getName(), artefactPorts.get(kapi.getType().getName()), ai);
                initProperties(kapi, api);
                ai.getInputs().add(api);
                artefactPortInstances.put(api.getName(), api);
            }

            for (net.cloudml.core.ArtefactPortInstance kapi : kai.getOutputs()) {
                ArtefactPortInstance api = new ArtefactPortInstance(kapi.getName(), artefactPorts.get(kapi.getType().getName()), ai);
                initProperties(kapi, api);
                ai.getOutputs().add(api);
                artefactPortInstances.put(api.getName(), api);
            }

            for (net.cloudml.core.ArtefactPortInstance kapi : kai.getProvided()) {
                ArtefactPortInstance api = new ArtefactPortInstance(kapi.getName(), artefactPorts.get(kapi.getType().getName()), ai);
                initProperties(kapi, api);
                ai.getProvided().add(api);
                artefactPortInstances.put(api.getName(), api);
            }

            model.getArtefactInstances().add(ai);
        }

        for (net.cloudml.core.ArtefactInstance kai : kDeploy.getArtefactInstances()) {//pass 2
            ArtefactInstance ai = artefactInstances.get(kai.getName());

            for (net.cloudml.core.ArtefactPortInstance kapi : kai.getRequired()) {
                ai.getRequired().add(artefactPortInstances.get(kapi.getName()));
            }
        }

        for (net.cloudml.core.BindingInstance kb : kDeploy.getBindingInstances()) {
            BindingInstance b = new BindingInstance(artefactPortInstances.get(kb.getClient().getName()), artefactPortInstances.get(kb.getServer().getName()), bindings.get(kb.getType().getName()));
            b.setName(kb.getName());
            model.getBindingInstances().add(b);
        }

        return model;
    }

    /**
     * Complements element with the properties (instances of
     * org.cloudml.property.Property) defined in kElement
     *
     * @param kElement
     * @param element
     */
    private void initProperties(net.cloudml.core.WithProperties kElement, WithProperties element) {
        for (net.cloudml.core.Property kp : kElement.getProperties()) {
            Property p = new Property(kp.getName(), kp.getValue());
            element.getProperties().add(p);
        }
    }

    /**
     * Complements kElement with the properties (instances of
     * org.cloudml.property.Property) defined in element
     *
     * @param element
     * @param kElement
     */
    private void initProperties(WithProperties element, net.cloudml.core.WithProperties kElement, net.cloudml.core.CoreFactory factory) {
        for (Property p : element.getProperties()) {
            net.cloudml.core.Property kp = factory.createProperty();
            kp.setName(p.getName());
            kp.setValue(p.getValue());
            kElement.addProperties(kp);
        }
    }

    public net.cloudml.core.DeploymentModel toKMF(DeploymentModel deploy) {
        Map<String, net.cloudml.core.Node> nodes = new HashMap<String, net.cloudml.core.Node>();
        Map<String, net.cloudml.core.NodePort> nodePorts = new HashMap<String, net.cloudml.core.NodePort>();
        Map<String, net.cloudml.core.Provider> providers = new HashMap<String, net.cloudml.core.Provider>();
        Map<String, net.cloudml.core.Artefact> artefacts = new HashMap<String, net.cloudml.core.Artefact>();
        Map<String, net.cloudml.core.ArtefactPort> artefactPorts = new HashMap<String, net.cloudml.core.ArtefactPort>();
        Map<String, net.cloudml.core.ArtefactInstance> artefactInstances = new HashMap<String, net.cloudml.core.ArtefactInstance>();
        Map<String, net.cloudml.core.ArtefactPortInstance> artefactPortInstances = new HashMap<String, net.cloudml.core.ArtefactPortInstance>();
        Map<String, net.cloudml.core.NodeInstance> nodeInstances = new HashMap<String, net.cloudml.core.NodeInstance>();
        Map<String, net.cloudml.core.NodePortInstance> nodePortInstances = new HashMap<String, net.cloudml.core.NodePortInstance>();
        Map<String, net.cloudml.core.Binding> bindings = new HashMap<String, net.cloudml.core.Binding>();

        net.cloudml.core.CoreFactory factory = new net.cloudml.factory.MainFactory().getCoreFactory();
        net.cloudml.core.DeploymentModel kDeploy = factory.createDeploymentModel();
        kDeploy.setName(deploy.getName());

        for (Provider p : deploy.getProviders()) {
            net.cloudml.core.Provider kProvider = factory.createProvider();
            initProperties(p, kProvider, factory);
            kProvider.setName(p.getName());
            kProvider.setCredentials(p.getCredentials());
            kDeploy.addProviders(kProvider);

            providers.put(kProvider.getName(), kProvider);
        }

        //TODO: continue cloning and conversion...
        for (Node node : deploy.getNodeTypes().values()) {
            net.cloudml.core.Node kNode = factory.createNode();
            initProperties(node, kNode, factory);
            kNode.setName(node.getName());

            kNode.setCloudProvider(providers.get(node.getProvider().getName()));
            kNode.setGroupName(node.getGroupName());
            kNode.setImageID(node.getImageId());
            kNode.setIs64os(node.getIs64os());
            kNode.setLocation(node.getLocation());
            kNode.setMinCore(node.getMinCore());
            kNode.setMinDisk(node.getMinDisk());
            kNode.setMinRam(node.getMinRam());
            kNode.setOS(node.getOS());
            kNode.setPrivateKey(node.getPrivateKey());
            kNode.setSecurityGroup(node.getSecurityGroup());
            kNode.setSshKey(node.getSshKey());

            nodes.put(kNode.getName(), kNode);

            for (NodePort np : node.getProvided()) {
                net.cloudml.core.NodePort knp = factory.createNodePort();
                initProperties(np, knp, factory);
                knp.setName(np.getName());

                kNode.addProvided(knp);
                nodePorts.put(knp.getName(), knp);
            }


            kDeploy.addNodeTypes(kNode);
        }


        for (Artefact a : deploy.getArtefactTypes().values()) {//first pass on the contained elements
            net.cloudml.core.Artefact ka = factory.createArtefact();
            ka.setName(a.getName());
            initProperties(a, ka, factory);

            artefacts.put(ka.getName(), ka);

            for (ArtefactPort ap : a.getInputs()) {
                net.cloudml.core.ArtefactPort kap = factory.createArtefactPort();
                kap.setName(ap.getName());
                initProperties(ap, kap, factory);
                kap.setPortNumber(ap.getPortNumber());
                ka.addInputs(kap);

                artefactPorts.put(kap.getName(), kap);
            }

            for (ArtefactPort ap : a.getProvided()) {//TODO: duplicated code to be rationalized
                net.cloudml.core.ArtefactPort kap = factory.createArtefactPort();
                kap.setName(ap.getName());
                initProperties(ap, kap, factory);
                kap.setPortNumber(ap.getPortNumber());
                ka.addProvided(kap);//!

                artefactPorts.put(kap.getName(), kap);
            }

            for (ArtefactPort ap : a.getOutputs()) {//TODO: duplicated code to be rationalized
                net.cloudml.core.ArtefactPort kap = factory.createArtefactPort();
                kap.setName(ap.getName());
                initProperties(ap, kap, factory);
                kap.setPortNumber(ap.getPortNumber());
                ka.addOutputs(kap);//!

                artefactPorts.put(kap.getName(), kap);
            }

            net.cloudml.core.Resource kr = factory.createResource();
            kr.setName(a.getResource().getName());
            kr.setDeployingCommand(a.getResource().getDeployingResourceCommand());
            kr.setRetrievingCommand(a.getResource().getRetrievingResourceCommand());
            kr.setConfigurationCommand(a.getResource().getConfigurationResourceCommand());
            kr.setStartCommand(a.getResource().getStartResourceCommand());
            ka.setResource(kr);

            kDeploy.addArtefactTypes(ka);
        }

        for (Artefact a : deploy.getArtefactTypes().values()) {//second pass on the referenced elements
            net.cloudml.core.Artefact ka = artefacts.get(a.getName());
            for (ArtefactPort ap : a.getRequired()) {
                net.cloudml.core.ArtefactPort kap = artefactPorts.get(ap.getName());
                ka.addRequired(kap);
            }

            if (a.getDestination() != null) {
                ka.setDestination(artefactPorts.get(a.getDestination().getName()));
            }
        }

        for (Binding b : deploy.getBindingTypes().values()) {
            net.cloudml.core.Binding kb = factory.createBinding();
            kb.setName(b.getName());
            kb.setClient(artefactPorts.get(b.getClient().getName()));
            kb.setServer(artefactPorts.get(b.getServer().getName()));

            if (b.getClientResource() != null) {
                net.cloudml.core.Resource cr = factory.createResource();
                cr.setName(b.getClientResource().getName());
                if (b.getClientResource().getDeployingResourceCommand() != null) {
                    cr.setDeployingCommand(b.getClientResource().getDeployingResourceCommand());
                }
                if (b.getClientResource().getRetrievingResourceCommand() != null) {
                    cr.setRetrievingCommand(b.getClientResource().getRetrievingResourceCommand());
                }
                if (b.getClientResource().getConfigurationResourceCommand() != null) {
                    cr.setConfigurationCommand(b.getClientResource().getConfigurationResourceCommand());
                }
                if (b.getClientResource().getStartResourceCommand() != null) {
                    cr.setStartCommand(b.getClientResource().getStartResourceCommand());
                }
                kb.setClientResource(cr);
            }

            if (b.getServerResource() != null) {
                net.cloudml.core.Resource cr = factory.createResource();
                cr.setName(b.getServerResource().getName());
                if (b.getServerResource().getDeployingResourceCommand() != null) {
                    cr.setDeployingCommand(b.getServerResource().getDeployingResourceCommand());
                }
                if (b.getServerResource().getRetrievingResourceCommand() != null) {
                    cr.setRetrievingCommand(b.getServerResource().getRetrievingResourceCommand());
                }
                if (b.getServerResource().getConfigurationResourceCommand() != null) {
                    cr.setConfigurationCommand(b.getServerResource().getConfigurationResourceCommand());
                }
                if (b.getServerResource().getStartResourceCommand() != null) {
                    cr.setStartCommand(b.getServerResource().getStartResourceCommand());
                }
                kb.setServerResource(cr);
            }


            kDeploy.addBindingTypes(kb);
            bindings.put(kb.getName(), kb);
        }

        for (NodeInstance ni : deploy.getNodeInstances()) {
            net.cloudml.core.NodeInstance kni = factory.createNodeInstance();
            kni.setName(ni.getName());
            kni.setPublicAddress(ni.getPublicAddress());
            kni.setType(nodes.get(ni.getType().getName()));
            initProperties(ni, kni, factory);

            nodeInstances.put(kni.getName(), kni);

            for (NodePortInstance npi : ni.getProvided()) {
                net.cloudml.core.NodePortInstance knpi = factory.createNodePortInstance();
                knpi.setName(npi.getName());
                knpi.setType(nodePorts.get(npi.getType().getName()));
                //kapi.setOwner(artefactInstances.get(ai.getName()));
                initProperties(npi, knpi, factory);
                kni.addProvided(knpi);

                nodePortInstances.put(knpi.getName(), knpi);
            }

            kDeploy.addNodeInstances(kni);
        }

        for (ArtefactInstance ai : deploy.getArtefactInstances()) {//pass 1
            net.cloudml.core.ArtefactInstance kai = factory.createArtefactInstance();
            kai.setName(ai.getName());
            kai.setType(artefacts.get(ai.getType().getName()));
            initProperties(ai, kai, factory);

            artefactInstances.put(kai.getName(), kai);

            if (ai.getDestination() != null) {
                kai.setDestination(nodePortInstances.get(ai.getDestination().getName()));
            }

            for (ArtefactPortInstance api : ai.getInputs()) {
                net.cloudml.core.ArtefactPortInstance kapi = factory.createArtefactPortInstance();
                kapi.setName(api.getName());
                kapi.setType(artefactPorts.get(api.getType().getName()));
                //kapi.setOwner(artefactInstances.get(ai.getName()));
                initProperties(api, kapi, factory);
                kai.addInputs(kapi);
                artefactPortInstances.put(kapi.getName(), kapi);
            }

            for (ArtefactPortInstance api : ai.getProvided()) {
                net.cloudml.core.ArtefactPortInstance kapi = factory.createArtefactPortInstance();
                kapi.setName(api.getName());
                kapi.setType(artefactPorts.get(api.getType().getName()));
                //kapi.setOwner(artefactInstances.get(ai.getName()));
                initProperties(api, kapi, factory);
                kai.addProvided(kapi);
                artefactPortInstances.put(kapi.getName(), kapi);
            }

            for (ArtefactPortInstance api : ai.getOutputs()) {
                net.cloudml.core.ArtefactPortInstance kapi = factory.createArtefactPortInstance();
                
                kapi.setName(api.getName());
                kapi.setType(artefactPorts.get(api.getType().getName()));
                //kapi.setOwner(artefactInstances.get(ai.getName()));
                initProperties(api, kapi, factory);
                kai.addOutputs(kapi);
                artefactPortInstances.put(kapi.getName(), kapi);
            }

            kDeploy.addArtefactInstances(kai);
        }

        for (ArtefactInstance ai : deploy.getArtefactInstances()) {//pass 2
            net.cloudml.core.ArtefactInstance kai = artefactInstances.get(ai.getName());

            for (ArtefactPortInstance api : ai.getRequired()) {
                kai.addRequired(artefactPortInstances.get(api.getName()));
            }

        }

        for (BindingInstance b : deploy.getBindingInstances()) {
            net.cloudml.core.BindingInstance kb = factory.createBindingInstance();
            kb.setName(b.getName());
            kb.setClient(artefactPortInstances.get(b.getClient().getName()));
            kb.setServer(artefactPortInstances.get(b.getServer().getName()));
            kb.setType(bindings.get(b.getType().getName()));

            kDeploy.addBindingInstances(kb);
        }

        return kDeploy;
        //TODO: remove the following (here just for "testing" purpose)
        /*
         * try { ModelJSONSerializer jsonSerializer = new ModelJSONSerializer();
         * File result = File.createTempFile(kDeploy.getName(), ".kmf.json");
         * OutputStream streamResult = new FileOutputStream(result);
         * jsonSerializer.serialize(kDeploy, streamResult); } catch (Exception
         * e) {}
         */

    }
    /**
     * Convenience procedure to convert JSON into XMI. Should be removed (or
     * moved) not to introduce a dependency to JSON here
     *
     * @param args
     */
    /*
     * public static void main(String[] args) { KMFBridge xmiCodec = new
     * KMFBridge(); JsonCodec jsonCodec = new JsonCodec();
     *
     * try { FilenameFilter filter = new FilenameFilter() {
     *
     * public boolean accept(File dir, String name) { return
     * name.endsWith(".json"); } }; File inputDirectory = new
     * File(xmiCodec.getClass().getResource("/").toURI()); for (File input :
     * inputDirectory.listFiles(filter)) { System.out.println("loading " +
     * input.getAbsolutePath() + ", " + jsonCodec); DeploymentModel model =
     * (DeploymentModel) jsonCodec.load(new FileInputStream(input));
     * xmiCodec.save(model, new FileOutputStream(new File(input.getParentFile(),
     * input.getName() + ".xmi"))); } } catch (FileNotFoundException ex) {
     * Logger.getLogger(KMFBridge.class.getName()).log(Level.SEVERE, null, ex);
     * } catch (URISyntaxException ex) {
     * Logger.getLogger(KMFBridge.class.getName()).log(Level.SEVERE, null, ex);
     * }
     *
     * }
     */
}
