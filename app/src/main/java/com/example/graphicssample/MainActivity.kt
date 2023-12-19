package com.example.graphicssample

import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.graphicssample.ui.theme.GraphicsSampleTheme
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphicsSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AgslSample1()
                }
            }
        }
    }
}

@Language("AGSL")
private val COLOR_SHADER_SAMPLE1 = """
    half4 main(float2 fragCoord) {
        return half4(1, 0, 1, 1);
    }
    """.trimIndent()

@Language("AGSL")
private val COLOR_SHADER_SAMPLE2 = """
    uniform half4 iColor;
    half4 main(float2 fragCoord) {
       return iColor;
   }
   """.trimIndent()

@Language("AGSL")
private val COLOR_SHADER_SAMPLE3 = """
    uniform float2 iResolution;
    half4 main(float2 fragCoord) {
        float2 scaled = fragCoord / iResolution.xy;
        return half4(0, scaled, 1);
    }
    """.trimIndent()

private const val DURATION = 4f
@Language("AGSL")
private val COLOR_SHADER_SAMPLE4 = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float iDuration;
    half4 main(in float2 fragCoord) {
        float2 scaled = abs(1.0 - mod(fragCoord / iResolution.xy + iTime / (iDuration / 2.0), 2.0));
        return half4(scaled, 0, 1);
    }
    """.trimIndent()

@Language("AGSL")
private val COLOR_SHADER_SAMPLE5 = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float iDuration;
    uniform shader composable;
    half4 main(in float2 fragCoord) {
        float2 scaled = abs(1.0 - mod(fragCoord / iResolution.xy + iTime / (iDuration / 2.0), 2.0));
        return half4(scaled, 0, composable.eval(fragCoord).a);
    }
    """.trimIndent()

@Preview
@Composable
fun AgslSample1() {
    val colorShader = RuntimeShader(COLOR_SHADER_SAMPLE1)
    val shaderBrush = ShaderBrush(colorShader)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawRect(brush = shaderBrush)
    }
}

@Preview
@Composable
fun AgslSample2() {
    val colorShader = RuntimeShader(COLOR_SHADER_SAMPLE2)
    colorShader.setColorUniform("iColor", Color.MAGENTA)

    val shaderBrush = ShaderBrush(colorShader)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawRect(brush = shaderBrush)
    }
}

@Preview
@Composable
fun AgslSample3() {
    val colorShader = RuntimeShader(COLOR_SHADER_SAMPLE3)
    val shaderBrush = ShaderBrush(colorShader)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        colorShader.setFloatUniform(
            "iResolution",
            size.width,
            size.height
        )

        drawRect(brush = shaderBrush)
    }
}

@Preview
@Composable
fun AgslSample4() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .customAnimated()
    )
}

private fun Modifier.customAnimated(): Modifier = this.composed {
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                println(it)
                value = it / 1000f
            }
        }
    }
    Modifier.drawWithContent {
        val colorShader = RuntimeShader(COLOR_SHADER_SAMPLE4)
        val shaderBrush = ShaderBrush(colorShader)

        colorShader.setFloatUniform("iResolution", size.width, size.height)
        colorShader.setFloatUniform("iDuration", DURATION)
        colorShader.setFloatUniform("iTime", time)
        drawRect(brush = shaderBrush)
    }
}

@Preview
@Composable
fun AgslSample5() {
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val shader = RuntimeShader(COLOR_SHADER_SAMPLE5)

        Text(
            text = "COLOR",
            fontSize = 120.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .onSizeChanged { size ->
                    shader.setFloatUniform(
                        "iResolution",
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                    shader.setFloatUniform("iDuration", DURATION)
                }
                .graphicsLayer {
                    shader.setFloatUniform("iTime", time)
                    renderEffect =
                        RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                }
        )
    }
}
