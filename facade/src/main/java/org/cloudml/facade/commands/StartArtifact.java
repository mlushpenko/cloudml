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
package org.cloudml.facade.commands;


/**
 * Capture a request for starting a artefact whose ID is given
 * 
 * @author Franck Chauvel - SINTEF ICT
 * @since 1.0
 */
public class StartArtifact extends ManageableCommand {

	private final String artifactId;
	
	
	public StartArtifact(CommandHandler handler, String artifactId) {
		super(handler);
		this.artifactId = artifactId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.cloudml.facade.commands.Command#execute(org.cloudml.facade.Facade)
	 */
	public void execute(CommandHandler target) {
		target.handle(this);
	}

	/**
	 * @return the ID of the artifact to start
	 */
	public String getArtifactId() {
		return artifactId;
	}

}
