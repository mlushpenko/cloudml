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
package org.cloudml.mrt.cmd.gen;

import java.util.List;
import org.cloudml.mrt.cmd.abstracts.Change;
import org.cloudml.mrt.cmd.abstracts.Instruction;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class RequestFlush extends Instruction {
  public String code;
  
  public RequestFlush() {
  }
  
  public RequestFlush(final String nouse) {
  }
  
  public RequestFlush(final Procedure1<RequestFlush> initializer) {
    initializer.apply(this);
  }
  
  @Override
  protected Object _execute(final Object context, final List<Change> changes) {
    final Procedure1<Flush> _function = new Procedure1<Flush>() {
        public void apply(final Flush it) {
          it.code = RequestFlush.this.code;
          it.object = context;
        }
      };
    Flush _flush = new Flush(_function);
    boolean _add = changes.add(_flush);
    return Boolean.valueOf(_add);
  }
}
