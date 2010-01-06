/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.tools;

import javax.swing.JMenu;

import com.rapidminer.gui.ConditionalAction;
/**
 * 
 * @author Simon Fischer
 */
public class ResourceMenu extends JMenu {
	private static final long serialVersionUID = -7711922457461154801L;

	public ResourceMenu(String i18Key) {
		super(new ResourceActionAdapter("menu."+i18Key) {
			private static final long serialVersionUID = 1L;
			{
				setCondition(EDIT_IN_PROGRESS, DONT_CARE);
			}
		});
	}
	
	/**
	 * Enables or Disables the menu, if an edit is in progress.
	 * Default is <code>true</code>.
	 * 
	 * @param enable <code>true</code> if the menu should be enabled and false otherwise
	 */
	public void enableOnEditInProgress( boolean enable ) {
		ResourceActionAdapter action = (ResourceActionAdapter)getAction();
		if(enable) {
			action.setCondition(	ConditionalAction.EDIT_IN_PROGRESS, 
									ConditionalAction.DONT_CARE);
		} else {
			action.setCondition(	ConditionalAction.EDIT_IN_PROGRESS, 
									ConditionalAction.DISALLOWED);
		}
	}
}
