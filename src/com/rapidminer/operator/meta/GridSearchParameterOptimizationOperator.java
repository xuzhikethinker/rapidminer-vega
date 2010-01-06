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
package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.value.ParameterValueRange;
import com.rapidminer.parameter.value.ParameterValues;


/**
 * <p>This operator finds the optimal values for a set of parameters using a grid
 * search. The parameter <var>parameters</var> is a list of key value pairs
 * where the keys are of the form <code>operator_name.parameter_name</code> and
 * the value is either a comma separated list of values (e.g. 10,15,20,25) or an 
 * interval definition in the format [start;end;stepsize] (e.g. [10;25;5]).
 * Alternatively a value grid pattern may be used by [e.g. [start;end;no_steps;scale],
 * where scale identifies the type of the pattern.</p> 
 * 
 * <p>The operator returns an
 * optimal {@link ParameterSet} which can as well be written to a file with a
 * {@link com.rapidminer.operator.io.ParameterSetWriter}. This parameter set
 * can be read in another process using a
 * {@link com.rapidminer.operator.io.ParameterSetLoader}.</p> 
 * 
 * <p>The file format of the parameter set file is straightforward and can easily be
 * generated by external applications. Each line is of the form
 * <center><code>operator_name.parameter_name = value</code></center>
 * </p>
 *   
 * <p>Please refer to section
 * {@rapidminer.ref sec:parameter_optimization|Advanced Processes/Parameter and performance analysis}
 * for an example application. Another parameter optimization schems like the 
 * {@link EvolutionaryParameterOptimizationOperator} might also be useful if the best ranges
 * and dependencies are not known at all. Another operator which works similar to this parameter 
 * optimization operator is the operator {@link ParameterIteration}. In contrast to the optimization
 * operator, this operator simply iterates through all parameter combinations. This might be 
 * especially useful for plotting purposes.
 * </p>
 * 
 * @author Simon Fischer, Helge Homburg, Ingo Mierswa, Tobias Malbrecht
 *          15:35:49 ingomierswa Exp $
 */
public class GridSearchParameterOptimizationOperator extends ParameterOptimizationOperator {
    
	protected Operator[] operators;

    protected String[] parameters;

    protected String[][] values;

    protected int[] currentIndex;

    protected int numberOfCombinations;
    
    protected int numberOfParameters;
    
	private ParameterSet best;

	public GridSearchParameterOptimizationOperator(OperatorDescription description) {
		super(description);
	}

	@Override
	public int getParameterValueMode() {
		return VALUE_MODE_DISCRETE;
	}
	
	/** Computes the performance for the current parameter values. */
	protected PerformanceVector computeCurrentPerformeance() {		
		// set all parameter values
		for (int j = 0; j < operators.length; j++) {
			operators[j].getParameters().setParameter(parameters[j], values[j][currentIndex[j]]);
			getLogger().fine(operators[j] + "." + parameters[j] + " = " + values[j][currentIndex[j]]);
		}
		return super.getPerformance();
	}
	
	protected void getParametersToOptimize() throws OperatorException {
		// check parameter values
		List<ParameterValues> parameterValuesList = parseParameterValues(getParameterList("parameters"));
		numberOfCombinations = 1;
		numberOfParameters = parameterValuesList.size();
		for (Iterator<ParameterValues> iterator = parameterValuesList.iterator(); iterator.hasNext(); ) {
			ParameterValues parameterValues = iterator.next();
			if (parameterValues instanceof ParameterValueRange) {
				logWarning("found (and deleted) parameter values range (" + parameterValues.getKey() + ") which makes no sense in grid parameter optimization");
				iterator.remove();
			}
			numberOfCombinations *= parameterValues.getNumberOfValues();
		}

		// initialize data structures
        operators = new Operator[parameterValuesList.size()];
        parameters = new String[parameterValuesList.size()];
        values = new String[parameterValuesList.size()][];
        currentIndex = new int[parameterValuesList.size()];
		
        // get parameter values and fill data structures
        int i = 0;
		for (Iterator<ParameterValues> iterator = parameterValuesList.iterator(); iterator.hasNext(); ) {
			ParameterValues parameterValues = iterator.next();
			operators[i] = parameterValues.getOperator();
			parameters[i] = parameterValues.getParameterType().getKey();
			values[i] = parameterValues.getValuesArray();
			i++;
		}
	}
	
	@Override
	public double getCurrentBestPerformance() {
	    if (best != null) {
	        return best.getPerformance().getMainCriterion().getAverage();
        } else {
	        return Double.NaN;
        }
    }
    
	@Override
	public void doWork() throws OperatorException {
        getParametersToOptimize();

        // start optimization
        log("Total number of combinations is " + numberOfCombinations);
        
        if (numberOfCombinations <= 1)
            throw new UserError(this, 922);
        
        int counter = 1;
		best = null;
		while (true) {
			getLogger().fine("Using parameter set " + counter + " / " + numberOfCombinations + ":");
			PerformanceVector performance = computeCurrentPerformeance();
			
			if ((best == null) || ((performance != null) && (performance.compareTo(best.getPerformance()) > 0))) {
				String[] bestValues = new String[parameters.length];
				for (int j = 0; j < parameters.length; j++) {
				    bestValues[j] = values[j][currentIndex[j]];
				}
				best = new ParameterSet(operators, parameters, bestValues, performance);
				passResultsThrough();
			}

			// next parameter values
			int k = 0;
			boolean ok = true;
			while (!(++currentIndex[k] < values[k].length)) {
				currentIndex[k] = 0;
				k++;
				if (k >= currentIndex.length) {
					ok = false;
					break;
				}
			}
			if (!ok)
				break;

			inApplyLoop();
            counter++;
		}

		deliver(best);
	}
}
