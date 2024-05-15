package org.bessolov.cshse

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MainActivityTest {

    private lateinit var numberGenerator: NumberGenerator

    @Before
    fun setUp() {
        numberGenerator = NumberGenerator()
    }

    @Test
    fun testGenerateNumber() {
        val mu = 10.0
        val sigmaSquared = 4.0

        val result = numberGenerator.generateNumber(mu, sigmaSquared)

        assertFalse(result.isNaN())

        assertTrue(result > 0)
    }

    @Test
    fun testGenerateNumberWithNegativeSigmaSquared() {
        val mu = 10.0
        val sigmaSquared = -4.0

        val result = numberGenerator.generateNumber(mu, sigmaSquared)

        assertTrue(result.isNaN())
    }

    @Test
    fun testGenerateNumberWithZeroSigmaSquared() {
        val mu = 10.0
        val sigmaSquared = 0.0

        val result = numberGenerator.generateNumber(mu, sigmaSquared)
        assertEquals(mu, result, 0.01)
    }
}
