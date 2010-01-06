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


import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

/**
 * 
 * @author Simon Fischer
 */
public abstract class ResourceAction extends ConditionalAction {

	private static final long serialVersionUID = -3699425760142415331L;

	private final String key;
//	public ResourceAction(String i18nKey) {
//		this(false, i18nKey);
//	}
//	
//	public ResourceAction(boolean smallIcon, String i18nKey) {
//		this(smallIcon, i18nKey, (Object[])null);
//	}
	
	public ResourceAction(String i18nKey, Object ... i18nArgs) {
		this(false, i18nKey, i18nArgs);
		
		setCondition(EDIT_IN_PROGRESS, DONT_CARE);
	}
	
	public ResourceAction(boolean smallIcon, String i18nKey, Object ... i18nArgs) {
		super((i18nArgs == null) || (i18nArgs.length == 0) ? 
				getMessage(i18nKey+".label") : 
					MessageFormat.format(getMessage(i18nKey+".label"), i18nArgs));
		this.key = i18nKey;
		String mne = getMessageOrNull(i18nKey + ".mne");
		if (mne != null) {			
			String name = (String)getValue(NAME);
			if (name.indexOf(mne.charAt(0)) == -1) {
				if (name.indexOf(mne.toUpperCase().charAt(0)) != -1) {
					mne = mne.toUpperCase();
					LogService.getRoot().warning("Mnemonic key "+mne+" not found for action " + i18nKey + " ("+name+"), converting to upper case.");
				} else {
					LogService.getRoot().warning("Mnemonic key "+mne+" not found for action " + i18nKey + " ("+name+")");
				}
			}
			putValue(MNEMONIC_KEY, (int)mne.charAt(0));
		}
		String tip = getMessageOrNull(i18nKey + ".tip");
		if (tip != null) {
			putValue(SHORT_DESCRIPTION, 
					(i18nArgs == null) || (i18nArgs.length == 0) ?
							tip :
							MessageFormat.format(tip, i18nArgs));
		}
		String icon = getMessageOrNull(i18nKey + ".icon");
		if (icon != null) {
			ImageIcon small = SwingTools.createIcon("16/"+icon);
			ImageIcon large = SwingTools.createIcon("24/"+icon);
			putValue(LARGE_ICON_KEY, smallIcon ? (small != null ? small : large) : large);
			putValue(SMALL_ICON, small != null ? small : large);
		}
		String acc = getMessageOrNull(i18nKey + ".acc");
		if (acc != null) {
			KeyStroke stroke = KeyStroke.getKeyStroke(acc);
			putValue(ACCELERATOR_KEY, stroke);			
		}
	}
	
	private static String getMessage(String key) {
		return I18N.getMessage(I18N.getGUIBundle(), "gui.action."+key);
	}
	
	private static String getMessageOrNull(String key) {
		return I18N.getMessageOrNull(I18N.getGUIBundle(), "gui.action."+key);
	}
	
	/** Adds the action to the input and action map of the component.
	 * 
	 * @param condition one out of WHEN_IN_FOCUES, ...
	 */
	public void addToActionMap(JComponent component, int condition) {
		KeyStroke keyStroke = (KeyStroke)getValue(ACCELERATOR_KEY);
		if (keyStroke != null) {
			component.getInputMap(condition).put(keyStroke, key);
			component.getActionMap().put(key, this);
		} else {
			LogService.getRoot().warning("Cannot add action "+key+" to input map: no accelerator defined.");
		}
	}
}
