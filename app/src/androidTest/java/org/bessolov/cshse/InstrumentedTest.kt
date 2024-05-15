package org.bessolov.cshse

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.*
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import java.util.*
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.hamcrest.Matcher
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
internal class InstrumentedTest {

    // add/remove seed
    private val random = Random()
    var lastNumber = 0.0
    var widgetFlag = false

    private val limit = 1_500
    private var mean = 0.0
    private var variance = 1.0

    private val meanDelta = 100.0
    private val varianceDelta = 100.0
    private val skewnessDelta = 100.0
    private val kurtosisDelta = 100.0

    private var generatedNums = ArrayList<Double>(0)

    private var activityScenario: ActivityScenario<MainActivity>? = null
    private var handler: DescriptionFailureHandler? = null

    private lateinit var appContext: Context
    private lateinit var mInstrumentation: Instrumentation

    @Before
    fun setUp() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation()
        handler = DescriptionFailureHandler(mInstrumentation)
        Espresso.setFailureHandler(handler)

        val nonLocalizedContext = mInstrumentation.targetContext
        val configuration = nonLocalizedContext.resources.configuration
        configuration.setLocale(Locale.UK)
        appContext = nonLocalizedContext.createConfigurationContext(configuration)

        mean = random.nextDouble()
        variance = random.nextDouble()

        val intent = Intent(appContext, MainActivity::class.java)
        activityScenario = ActivityScenario.launch(intent)

        meanId = appContext.resources
            .getIdentifier("mean_val", "id", appContext.opPackageName)
        varianceId = appContext.resources
            .getIdentifier("variance_value", "id", appContext.opPackageName)
        getNumId = appContext.resources
            .getIdentifier("get_random_num", "id", appContext.opPackageName)
        resultNumId = appContext.resources
            .getIdentifier("random_number_result", "id", appContext.opPackageName)
    }

    private fun checkInterface(ids: IntArray, message: String = "?") {
        var id = 1
        for (e in ids) {
            id *= e
        }
        if (message != "?") {
            Assert.assertNotEquals(message, 0, id.toLong())
        } else {
            Assert.assertNotEquals(0, id.toLong())
        }
    }

    @Test(timeout = MAX_TIMEOUT)
    fun distributionTest() {
        checkInterface(
            intArrayOf(
                meanId, varianceId, resultNumId
            )
        )
        distributionCheckStep()
    }

    private fun distributionCheckStep() {
        class SearchScreen : Screen<SearchScreen>() {
            val meanView = KEditText { withId(meanId) }
            val varianceView = KEditText { withId(varianceId) }
            val getNum = KButton { withId(getNumId) }
            val resultNum = KTextView { withId(resultNumId) }
        }

        val screen = SearchScreen()
        screen {
            meanView.clearText()
            varianceView.clearText()
            meanView.typeText("$mean")
            varianceView.typeText("$variance")
            for (i in 0..limit) {
                getNum.click()
                Thread.sleep(THREAD_DELAY)
                resultNum.assert {
                    DoubleComparison(mean, variance, this@InstrumentedTest)
                }
            }
            // check saving state after rotation
            handler?.extraMessage = "Rotating device"
            rotateDevice(true)
            resultNum.hasText("$lastNumber")
            rotateDevice(false)
            resultNum.hasText("$lastNumber")

            checkLogNorm(
                generatedNums,
                exp(mean + variance / 2.0),
                exp(2 * mean + variance) * (exp(variance) - 1),
                sqrt(exp(variance) - 1) * (exp(variance) + 2),
                exp(4 * variance) + 2 * exp(3 * variance) + 3 * exp(2 * variance) - 6
            )
        }
    }

    fun addGeneratedNumber(e: Double) {
        generatedNums.add(e)
    }

    /**
     * checks mean and std^2 for the whole sample
     * mean and variance
     */
    private fun checkLogNorm(a: ArrayList<Double>, m: Double, v: Double, sk: Double, kur: Double) {
        val d = a.toDoubleArray()
        val gm = StatUtils.mean(d)
        val gv = StatUtils.variance(d)
        val gskewness = DescriptiveStatistics(d).skewness
        val gkurtosis = DescriptiveStatistics(d).kurtosis
        Log.d(
            "DistributionTest",
            "${abs(gm - m)} ${abs(gv - v)} " +
                    "${abs(gskewness - sk)} ${abs(gkurtosis - kur)}"
        )
        assertEquals("Mean is different", gm, m, meanDelta)
        assertEquals("Variance is different", gv, v, varianceDelta)
        assertEquals("Skewness is different", gskewness, sk, skewnessDelta)
        assertEquals("Kurtosis is different", gkurtosis, kur, kurtosisDelta)
    }

    @Throws(InterruptedException::class)
    private fun rotateDevice(landscapeMode: Boolean) {
        if (landscapeMode) {
            activityScenario!!.onActivity { activity ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        } else {
            activityScenario!!.onActivity { activity ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    companion object {
        private const val THREAD_DELAY: Long = 10
        private const val MAX_TIMEOUT: Long = 500_000

        private var meanId = 0
        private var varianceId = 0
        private var getNumId = 0
        private var resultNumId = 0

        @BeforeClass
        @JvmStatic
        fun enableAccessibilityChecks() {
            IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS);
            IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.SECONDS);
        }
    }
}

internal class DoubleComparison(
    private val mean: Double,
    private val std: Double,
    private val testInstance: InstrumentedTest
) :
    ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) throw noViewFoundException
        assertTrue(view is TextView)
        val gotValue = (view as TextView).text.toString()

        if (testInstance.widgetFlag || view.accessibilityClassName == "android.widget.TextView") {
            testInstance.widgetFlag = true
        } else {
            assertEquals("View has an incorrect accessibilityClassName", "TextView", "EditText")
        }
        val num = gotValue.toDouble()
        testInstance.lastNumber = num
        testInstance.addGeneratedNumber(num)
    }
}

class DescriptionFailureHandler(instrumentation: Instrumentation) : FailureHandler {
    var extraMessage = ""
    private var delegate: DefaultFailureHandler =
        DefaultFailureHandler(instrumentation.targetContext)

    override fun handle(error: Throwable?, viewMatcher: Matcher<View>?) {
        if (error != null) {
            val newError = Throwable(
                "$extraMessage " + error.message, error.cause
            )
            delegate.handle(newError, viewMatcher)
        }
    }
}