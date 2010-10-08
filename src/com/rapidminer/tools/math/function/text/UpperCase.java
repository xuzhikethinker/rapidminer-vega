/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2010 by Rapid-I and the contributors
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
package com.rapidminer.tools.math.function.text;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 * Transforms the given string to upper case characters.
 * 
 * @author Ingo Mierswa
 */
public class UpperCase extends PostfixMathCommand {

	public UpperCase() {
		numberOfParameters = 1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(Stack stack) throws ParseException {
		if (stack.size() != 1)
			throw new ParseException("Needs one argument: The text which should be transformed to upper case.");

		// initialize the result to the first argument
		Object textObject = stack.pop();
		if (!(textObject instanceof String)) {
			throw new ParseException("Invalid argument type, must be (string)");
		}
		
		String text = (String) textObject;
		
		stack.push(text.toUpperCase());
	}
}