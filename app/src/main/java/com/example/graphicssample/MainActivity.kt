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
    // 何も起きない。。。
    // なぜなのか？
    // これはShader側でなく、Compose側で色を指定する必要がある
    // なおCompose側ではComposeのColorを使ってもコンパイルエラーになる（Editor側で型が違うよって怒られる）
    // ここで指定する色は"android.graphics.Color"じゃないといけない
    // iColorはこのシェーダーを使用するCompose側で指定する
    // つまり、Compose側で定義した値をこちらの言語でも書くことができる！
    // layout(color)は必須、"android.graphics.Color"でCompose側で指定するときはこれが必要
    // layout(color) uniform half4 iColor;
    // 普通にマゼンタをfloatで指定してあげるには"uniform half4 iColor"を宣言して
    // Compose側で"setFloatUniform"をする必要がある
//    uniform half4 iColor;
//    half4 main(float2 fragCoord) {
//        return iColor;
//    }


@Language("AGSL")
private val COLOR_SHADER_SAMPLE3 = """
    // これはまずResolutionなる値をCompose側から取得している
    // sampleでは画面の縦横のサイズを取っていた
    // ここfragCoordの値が何なのかを把握する必要がある
    // 最後のhalf4は4次元のfloatを表しているはず
    // ここで書かれているfragCoordは2次元
    // またiResolutionも2次元の定数となってる
    // 左上が暗く（こっちは黒）、右下が明るい(ていうか黄色？)
    // つまり右上の値はhalf4(0, 0, 0, 1)で、右下の値はhalf4(1, 1, 0, 1)
    // つまり、このfragCoordはピクセルの位置を表していそう！
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
fun AgslSample2Preview() {
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
fun AgslSample3Preview() {
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

// Composeだとアニメーションができないじゃないか？！！
// ここからは正直手探りでした。
// まず日本語で記事を探してみた
// あった！！
// ただどうやらViewでやっている。。。
// 自分はどうしてもComposeで試せるようにしたい！！！！
// 英語で記事を探しますか！！
// みつけた！！
// やはりGoogleのサンプルの中にいいものがおちていた
@Preview
@Composable
fun AgslSample4Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .customAnimated()
    ){
//        drawRect(Color.TRANSPARENT)
    }
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

@Composable
fun AgslSample5Preview() {
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

@Composable
fun AgslSample6Preview() {
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
                    println((time * 10) % 360)
                    shader.setFloatUniform("iTime", time)
                    renderEffect =
                        RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    this.rotationY = time % 360
                }
        )
    }
}
