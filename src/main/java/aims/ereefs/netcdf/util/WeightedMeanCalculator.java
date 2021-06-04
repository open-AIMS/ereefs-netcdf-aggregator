package aims.ereefs.netcdf.util;

import java.util.Arrays;

/**
 * Utility for calculating the weighted mean of a set of values.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class WeightedMeanCalculator {

    static public double calculate(Double[] values, Double[] weights) {
        if (values.length != weights.length) {
            throw new RuntimeException("Weighted Mean - values and weights should be the same length. "
                + "values " + Arrays.toString(values)
                + "weights " + Arrays.toString(weights));
        }
        double sum = 0.0;
        double sumWeights = 0.0;
        boolean hasData = false;
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i]) && !Double.isNaN(weights[i])) {
                double value = values[i];
                double weight = weights[i];
                sum += value * weight;
                sumWeights += weight;
                hasData = true;

            }
        }

        if (hasData) {
            return sum / sumWeights;
        } else {
            return Double.NaN;
        }
    }

}
