package sample;

import java.util.Random;

public class SimulatedAnnealing {
    private static final double minRatio = Math.log(0.0085);

    public static boolean accept(double newScore, double currLocalScore, double temperature, Random random) {

        double diffScore = newScore - currLocalScore;
        if (diffScore > 0) {
            return true;
        }
        if (temperature == 0.0) {
            return false;
        }
        double ratio = diffScore / temperature;
        return ratio > minRatio && Math.pow(Math.E, ratio) > random.nextFloat();
    }
}
