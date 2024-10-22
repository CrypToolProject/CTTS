/*
    Copyright 2023 George Lasry & CrypTool 2 Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cryptool.ctts.cryptanalysis;

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
