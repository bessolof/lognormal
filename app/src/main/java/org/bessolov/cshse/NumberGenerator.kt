package org.bessolov.cshse

import java.util.Random
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.pow

class NumberGenerator {
    private val random = Random()

    fun generateNumber(mu: Double, sigmaSquared: Double): Double {
        val z = random.nextGaussian() // Generate a Gaussian random number

        val normalMean = ln(mu.pow(2) / sqrt(mu.pow(2) + sigmaSquared))
        val normalStdDev = sqrt(ln(1 + (sigmaSquared / mu.pow(2))))

        val normalRandom = z * normalStdDev + normalMean
        return exp(normalRandom)
    }
}