package org.bessolov.cshse

import org.apache.commons.math3.distribution.LogNormalDistribution
import java.util.Random
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.pow

class NumberGenerator {
    private val random = Random()

    fun generateNumberfromLognormal(mu : Double, sigmaSquared: Double): Double {
        val u1 = random.nextDouble()
        val u2 = random.nextDouble()
        val z = sqrt(-2 * ln(u1)) * cos(2 * Math.PI * u2)
        return mu + z * sigmaSquared
    }

    fun generateNumber(mean: Double, std: Double): Double =
        LogNormalDistribution(mean, std).sample()
}