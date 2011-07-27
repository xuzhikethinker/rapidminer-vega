/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.operator.preprocessing.transformation.aggregation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.table.DataRow;

/**
 * This is an {@link Aggregator} for the {@link LeastOccuringAggregationFunction}.
 * If the least value that at least occurrs once is not unique, the first value from the nominal mapping will be used.
 * 
 * @author Sebastian Land
 */
public class LeastOccurringAggregator implements Aggregator {

    private Attribute sourceAttribute;
    private int[] frequencies;

    public LeastOccurringAggregator(AggregationFunction function) {
        this.sourceAttribute = function.getSourceAttribute();
        frequencies = new int[sourceAttribute.getMapping().size()];
    }

    @Override
    public void count(Example example) {
        double value = example.getValue(sourceAttribute);
        if (!Double.isNaN(value))
            frequencies[(int) value]++;
    }

    @Override
    public void set(Attribute attribute, DataRow row) {
        int minIndex = -1;
        int minFrequency = Integer.MAX_VALUE;
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0 && frequencies[i] < minFrequency) {
                minIndex = i;
                minFrequency = frequencies[i];
            }
        }
        // if any counter was greater 0, set result to maximum
        if (minIndex > -1)
            row.set(attribute, minIndex);
        else
            row.set(attribute, Double.NaN);
    }
}
