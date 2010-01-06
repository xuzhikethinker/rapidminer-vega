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
package com.rapidminer.gui.properties;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.ParameterService;


/**
 * The settings dialog for user settings. These are stored in a
 * &quot;rapidminerrc&quot; file in the user directory &quot;.rapidminer&quot; and can
 * overwrite system wide settings. The settings are grouped in
 * {@link SettingsTabs} each of which contains a {@link SettingsPropertyTable}.
 * 
 * @author Ingo Mierswa
 */
public class SettingsDialog extends ButtonDialog {

	private static final long serialVersionUID = 6665295638614289994L;

	private final SettingsTabs tabs = new SettingsTabs();

    private final List<SettingsChangeListener> listeners = new LinkedList<SettingsChangeListener>();

	public SettingsDialog() {
		super("settings", true);
		
		// create buttons
    	Collection<AbstractButton> buttons = new LinkedList<AbstractButton>();
		buttons.add(new JButton(new ResourceAction("settings_apply") {		
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				tabs.applyProperties();
                fireSettingsChanged();
				dispose();
			}
		}));
		buttons.add(new JButton(new ResourceAction("settings_save") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				try {
					tabs.save();
                    fireSettingsChanged();
					dispose();
				} catch (IOException ioe) {
					SwingTools.showSimpleErrorMessage("cannot_save_properties", ioe);
				}
			}
		}));
		buttons.add(makeCancelButton());
		layoutDefault(tabs, NORMAL, buttons);
	}
	
	@Override
	public String getInfoText() {
		return I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.settings.message",
						   ParameterService.getUserConfigFile("rapidminerrc" + "." + System.getProperty("os.name")));
	}
        
    public void addSettingsChangedListener(SettingsChangeListener listener) {
        listeners.add(listener);
    }

    public void removeSettingsChangedListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireSettingsChanged() {
        Iterator<SettingsChangeListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().settingsChanged(System.getProperties());
        }
    }
}
