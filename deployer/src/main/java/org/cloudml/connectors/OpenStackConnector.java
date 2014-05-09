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
package org.cloudml.connectors;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.cloudml.core.Node;
import org.cloudml.core.NodeInstance;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudml.core.Property;
import org.jclouds.ContextBuilder;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.*;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payloads;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideLoginCredentials;

import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

/**
 * Created by Nicolas Ferry on 08.05.14.
 */
public class OpenStackConnector implements Connector{

    private static final Logger journal = Logger.getLogger(JCloudsConnector.class.getName());
    private ComputeServiceContext computeContext;
    private ComputeService novaComputeService;
    private final String endpoint;

    public OpenStackConnector(String endPoint,String provider,String login,String secretKey){
        this.endpoint=endPoint;
        journal.log(Level.INFO, ">> Connecting to "+provider+" ...");
        Iterable<Module> modules = ImmutableSet.<Module> of(
                new SshjSshClientModule(),
                new NullLoggingModule());
        ContextBuilder builder = ContextBuilder.newBuilder(provider)
                .endpoint(endPoint)
                .credentials(login, secretKey)
                .modules(modules);


        journal.log(Level.INFO, ">> Authenticating ...");
        computeContext=builder.buildView(ComputeServiceContext.class);
        novaComputeService= computeContext.getComputeService();
    }

    /**
     * Retrieve information about a node
     * @param name name of a node
     * @return data about a node
     */
    public ComputeMetadata getNodeByName(String name){
        for(ComputeMetadata n : novaComputeService.listNodes()){
            if(n.getName() != null &&  n.getName().equals(name))
                return n;
        }
        return null;
    }

    /**
     * retrieve the list of nodes
     * @return a list of information about each node
     */
    public Set<? extends ComputeMetadata> listOfNodes(){
        return novaComputeService.listNodes();
    }

    /**
     * Retrieve data about a node
     * @param id id of a node
     * @return Information about a node
     */
    public NodeMetadata getNodeById(String id){
        return novaComputeService.getNodeMetadata(id);
    }

    /**
     * Close the connection
     */
    public void closeConnection(){
        novaComputeService.getContext().close();
        journal.log(Level.INFO, ">> Closing connection ...");
    }

    /**
     * Prepare the credential builder
     * @param login
     * @param key
     * @return
     */
    private org.jclouds.domain.LoginCredentials.Builder initCredentials(String login, String key){
        String contentKey;
        org.jclouds.domain.LoginCredentials.Builder b= LoginCredentials.builder();
        try {
            contentKey = FileUtils.readFileToString(new File(key));
            b.user(login);
            b.noPassword();
            b.privateKey(contentKey);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Upload a file on a selected node
     * @param sourcePath path to the file to be uploaded
     * @param destinationPath path to the file to be created
     * @param nodeId Id of a node
     * @param login user login
     * @param key key to connect
     */
    public void uploadFile(String sourcePath, String destinationPath, String nodeId, String login, String key){
        org.jclouds.domain.LoginCredentials.Builder b=initCredentials(login, key);
        SshClient ssh = novaComputeService.getContext().utils().sshForNode().apply(NodeMetadataBuilder.fromNodeMetadata(getNodeById(nodeId)).credentials(b.build()).build());
        try {
            ssh.connect();
            ssh.put(destinationPath, Payloads.newPayload(new File(sourcePath)));
        } finally {
            if (ssh != null)
                ssh.disconnect();
            journal.log(Level.INFO, ">> File uploaded!");
        }

    }


    /**
     * Execute a command on a group of nodes
     * @param group name of the group
     * @param command the command to be executed
     * @param login username
     * @param key sshkey
     * @throws RunScriptOnNodesException
     */
    public void execCommandInGroup(String group, String command, String login, String key) throws RunScriptOnNodesException {
        journal.log(Level.INFO, ">> executing command...");
        journal.log(Level.INFO, ">> "+ command);

        org.jclouds.domain.LoginCredentials.Builder b=initCredentials(login, key);
        Map<? extends NodeMetadata, ExecResponse> responses = novaComputeService.runScriptOnNodesMatching(
                runningInGroup(group),
                exec(command),
                overrideLoginCredentials(b.build())
                        .runAsRoot(false)
                        .wrapInInitScript(false));// run command directly

        for(Map.Entry<? extends NodeMetadata, ExecResponse> r : responses.entrySet())
            journal.log(Level.INFO, ">> "+r.getValue());
    }

    /**
     * Execute a command on a specified node
     * @param id id of the node
     * @param command the command to be executed
     * @param login username
     * @param key sshkey for connection
     */
    public void execCommand(String id, String command, String login, String key){
        journal.log(Level.INFO, ">> executing command...");
        journal.log(Level.INFO, ">> "+ command);

        org.jclouds.domain.LoginCredentials.Builder b=initCredentials(login, key);
        ExecResponse response = novaComputeService.runScriptOnNode(
                id,
                exec(command),
                overrideLoginCredentials(b.build())
                        .runAsRoot(false)
                        .wrapInInitScript(false));// run command directly

        journal.log(Level.INFO, ">> "+response.getOutput());
    }

    /**
     * Update the runtime metadata of a node if already deployed
     * @param a description of a node
     */
    public void updateNodeMetadata(NodeInstance a){
        ComputeMetadata cm= getNodeByName(a.getName());
        if(cm != null){
            a.setPublicAddress(getNodeById(cm.getId()).getPublicAddresses().iterator().next());
            a.setId(cm.getId());
        }
    }

    /**
     * Provision a node
     * @param a description of the node to be created
     * @return
     */
    public void createInstance(NodeInstance a){
        Node node= a.getType();
        ComputeMetadata cm= getNodeByName(a.getName());
		/* UPDATE THE MODEL */
        if(cm != null){
            updateNodeMetadata(a);
        }else{
            Template template=null;
            NodeMetadata nodeInstance = null;
            String groupName="cloudml-instance";
            if(!node.getGroupName().equals(""))
                groupName=node.getGroupName();

            TemplateBuilder templateBuilder = novaComputeService.templateBuilder();

            if(!node.getImageId().equals("")){
                templateBuilder.imageId(node.getImageId());
            }

            journal.log(Level.INFO, ">> Provisioning a node ...");

            if (node.getMinRam() > 0)
                templateBuilder.minRam(node.getMinRam());
            if (node.getMinCore() > 0)
                templateBuilder.minCores(node.getMinCore());
            if (!node.getLocation().equals(""))
                templateBuilder.locationId(node.getLocation());
            if (!node.getOS().equals(""))
                templateBuilder.imageDescriptionMatches(node.getOS());
            else templateBuilder.osFamily(OsFamily.UBUNTU);
            templateBuilder.os64Bit(node.getIs64os());

            template = templateBuilder.build();
            journal.log(Level.INFO, ">> node type: "+template.getHardware().getId()+" on location: "+template.getLocation().getId());
            a.getProperties().add(new Property("ProviderInstanceType", template.getHardware().getId()));
            a.getProperties().add(new Property("location", template.getLocation().getId()));

            template.getOptions().as(NovaTemplateOptions.class).keyPairName(node.getSshKey());
            template.getOptions().as(NovaTemplateOptions.class).securityGroups(node.getSecurityGroup());
            template.getOptions().as(NovaTemplateOptions.class).userMetadata("Name", a.getName());
            template.getOptions().as(NovaTemplateOptions.class).overrideLoginUser(a.getName());

            template.getOptions().blockUntilRunning(true);

            try {
                Set<? extends NodeMetadata> nodes = novaComputeService.createNodesInGroup(groupName, 1, template);
                nodeInstance = nodes.iterator().next();

                journal.log(Level.INFO, ">> Running node: "+nodeInstance.getName()+" Id: "+ nodeInstance.getId() +" with public address: "+nodeInstance.getPublicAddresses() +
                        " on OS:"+nodeInstance.getOperatingSystem()+ " " + nodeInstance.getCredentials().identity+":"+nodeInstance.getCredentials().getUser()+":"+nodeInstance.getCredentials().getPrivateKey());

            } catch (RunNodesException e) {
                e.printStackTrace();
                a.setStatusAsError();
            }

            if(nodeInstance.getPublicAddresses().iterator().hasNext()){
                a.setPublicAddress(nodeInstance.getPublicAddresses().iterator().next());
            }else{
                a.setPublicAddress(nodeInstance.getPrivateAddresses().iterator().next());
            }

            a.setId(nodeInstance.getId());
            a.setStatusAsRunning();
        }
    }

    /**
     * Terminate a specified node
     * @param id id of the node
     */
    public void destroyNode(String id){
        novaComputeService.destroyNode(id);
    }


}