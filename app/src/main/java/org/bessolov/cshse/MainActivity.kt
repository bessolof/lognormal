package org.bessolov.cshse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import org.w3c.dom.Text
import kotlin.math.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        changeStatusBarColor("#000000")

        findViewById<TextView>(R.id.greeting).typeface = ResourcesCompat.getFont(this, R.font.hsesans_black)
        findViewById<TextView>(R.id.greeting).setTextSize(TypedValue.COMPLEX_UNIT_SP, 50.toFloat())

        val numberGenerator = NumberGenerator()

        val btt: Button = findViewById<Button>(R.id.get_random_num)
        btt.setOnClickListener {
            val mu : Double? = findViewById<TextView>(R.id.mean_val).text.toString().toDoubleOrNull()
            val sigmaSquared : Double? = findViewById<TextView>(R.id.variance_value).text.toString().toDoubleOrNull()

            if (sigmaSquared != null && mu != null) {
                if (mu != 0.0) {
                    findViewById<TextView>(R.id.random_number_result).text = numberGenerator.generateNumber(mu, sigmaSquared).toString()
                    if (findViewById<TextView>(R.id.random_number_result).text == "NaN") {
                        findViewById<TextView>(R.id.random_number_result).text = getString(R.string.error_numLimit)
                    }
                } else {
                    findViewById<TextView>(R.id.random_number_result).text = getString(R.string.error_wrongData)
                }
            } else {
                findViewById<TextView>(R.id.random_number_result).text = getString(R.string.error_wrongData)
            }
        }
    }

    /*fun generateNumber(mu : Double, sigmaSquared : Double) : Double {
        val u1 = 1.0 - Math.random()
        val u2 = 1.0 - Math.random()

        val z = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)

        val normalMean = ln(mu.pow(2.0) / kotlin.math.sqrt(mu.pow(2.0) + sigmaSquared))
        val normalStdDev = kotlin.math.sqrt(ln(1 + (sigmaSquared / mu.pow(2.0))))

        val normalRandom = z * normalStdDev + normalMean
        return exp(normalRandom)
    }*/

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }
}
