package com.xzzb.tetriscompose

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xzzb.tetriscompose.ui.theme.TetrisComposeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TetrisComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xffeac216)) {
                    Tetris()
                }
            }
        }
    }
}

@Composable
fun Tetris(viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val drawBorder: DrawScope.() -> Unit = {

        val strokeWidth = 4.dp.value * density
        val width = size.width
        val height = size.height
        val pathTop = Path()
        pathTop.apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width - strokeWidth, strokeWidth)
            lineTo(strokeWidth, strokeWidth)
            close()
        }
        val pathLeft = Path()
        pathLeft.apply {
            moveTo(0f, 0f)
            lineTo(0f, height)
            lineTo(strokeWidth, height - strokeWidth)
            lineTo(strokeWidth, strokeWidth)
            close()
        }
        val pathBottom = Path()
        pathBottom.apply {
            moveTo(width, height)
            lineTo(0f, height)
            lineTo(strokeWidth, height - strokeWidth)
            lineTo(width - strokeWidth, height - strokeWidth)
            close()
        }
        val pathRight = Path()
        pathRight.apply {
            moveTo(width, height)
            lineTo(width, 0f)
            lineTo(width - strokeWidth, strokeWidth)
            lineTo(width - strokeWidth, height - strokeWidth)
            close()
        }
        drawPath(pathTop, color = Color(0xff856d0f))
        drawPath(pathLeft, color = Color(0xff856d0f))
        drawPath(pathBottom, color = Color(0xfff9e05a))
        drawPath(pathRight, color = Color(0xfff9e05a))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = blockWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                viewModel.sampleShapes.map {
                    Column {
                        it.map {
                            Box(Modifier.size(blockWidth)) {
                                if (it == 1) Block(color = remember {
                                    mutableStateOf(BlockColor.Black)
                                })
                            }
                        }
                    }
                }
            }
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color(0xff8d9f73))
                    .drawBehind(onDraw = drawBorder)
                    .padding(10.dp)

            ) {
                Row {
                    Column(
                        Modifier
                            .border(BorderStroke(1.dp, Color.Black))
                            .padding(2.dp)
                    ) {
                        viewModel.blocks.forEachIndexed { _, row ->
                            Row {
                                row.forEachIndexed { _, block ->
                                    Block(block)
                                }
                            }
                        }
                    }
                    Column(
                        Modifier.width((blockWidth.value * 6).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        viewModel.waitBlocks.map { states ->
                            Row {
                                states.map {
                                    Block(color = it)
                                }
                            }
                        }
                    }
                }
            }
            Row {
                viewModel.sampleShapes.reversed().map {
                    Column {
                        it.map {
                            Box(Modifier.size(blockWidth)) {
                                if (it == 1) Block(color = remember {
                                    mutableStateOf(BlockColor.Black)
                                })
                            }
                        }
                    }
                }
            }
        }
        ControlArea(viewModel)

    }

}

@Composable
fun ControlArea(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            Arrangement.SpaceAround,
            Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ControlButton(
                    text = "暂停",
                    color = Color(0xff2bbd1a),
                    onTapDown = { },
                    onTapUp = { viewModel.paused = viewModel.paused.not() },
                    onTap = { },
                    type = 3
                )
                ControlButton(
                    text = "音效",
                    color = Color(0xff2bbd1a),
                    onTapDown = { },
                    onTapUp = {
                        coroutineScope.launch {
                            viewModel.turnAround()
                        }
                    },
                    onTap = {},
                    type = 3
                )
                ControlButton(
                    text = "重玩",
                    color = Color(0xffd10015),
                    onTapDown = { },
                    onTapUp = { viewModel.reInitAllBlocks() },
                    onTap = { },
                    type = 3
                )
            }
            ControlButton(
                text = "掉落",
                color = Color(0xff4749ee),
                onTapDown = { /*TODO*/ },
                onTapUp = {
                    coroutineScope.launch {
                        viewModel.moveToBottom()
                    }
                },
                onTap = { /*TODO*/ },
                type = 1
            )
            Spacer(Modifier.height(10.dp))
        }
        Box(
            Modifier
                .weight(1f)
                .height(220.dp)
                .padding(10.dp)
        ) {
            ControlButton(Modifier.align(Alignment.TopCenter),
                color = Color(0xff4749ee),
                onTapDown = { viewModel.rotate() },
                onTapUp = { /*TODO*/ },
                onTap = { /*TODO*/ })
            ControlButton(Modifier.align(Alignment.BottomCenter),
                text = "下移", color = Color(0xff4749ee),
                onTapDown = {
                    coroutineScope.launch {
                        viewModel.moveDownForTimes()
                    }
                },
                onTapUp = {
                    viewModel.downTimerStarted = false
                    viewModel.moveVertically = false
                },
                onTap = { /*TODO*/ })
            ControlButton(Modifier.align(Alignment.CenterEnd),
                text = "右移", color = Color(0xff4749ee),
                onTapDown = {
                    coroutineScope.launch {
                        viewModel.moveRightForTimes()
                    }
                },
                onTapUp = {
                    viewModel.rightTimerStarted = false
                    viewModel.moveHorizontally = false
                },
                onTap = { /*TODO*/ })
            ControlButton(Modifier.align(Alignment.CenterStart),
                text = "左移", color = Color(0xff4749ee),
                onTapDown = {
                    coroutineScope.launch {
                        viewModel.moveLeftForTimes()
                    }
                },
                onTapUp = {
                    viewModel.leftTimerStarted = false
                    viewModel.moveHorizontally = false
                },
                onTap = { /*TODO*/ })

            Triangle(Modifier.align(BiasAlignment(0f, 0.03f)).rotate(180f))

            Triangle(Modifier.align(BiasAlignment(0.15f, -0.1f)).rotate(90f))

            Triangle(Modifier.align(BiasAlignment(-0.15f, -0.1f)).rotate(-90f))

            Triangle(Modifier.align(BiasAlignment(0f, -0.23f)))

            Text(
                "旋转", Modifier.align(BiasAlignment(0.6f, -0.9f)),
                style = TextStyle(fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun Triangle(modifier: Modifier) {
    Canvas(
        modifier.size(12.dp)
            .aspectRatio(1f)
    ) {
        val rect = Rect(Offset.Zero, size)
        val trianglePath = Path().apply {
            moveTo(rect.topCenter.x, rect.topCenter.y)
            lineTo(rect.bottomRight.x, rect.bottomRight.y)
            lineTo(rect.bottomLeft.x, rect.bottomLeft.y)
            close()
        }

        drawIntoCanvas { canvas ->
            canvas.drawOutline(
                outline = Outline.Generic(trianglePath),
                paint = Paint().apply {
                    color = Color.Black
                    pathEffect = PathEffect.cornerPathEffect(rect.maxDimension / 3)
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ControlButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color,
    onTapDown: () -> Unit,
    onTapUp: () -> Unit,
    onTap: (() -> Unit)?,
    type: Int = 2,
) {
    val size = when (type) {
        1 -> 100f
        2 -> 65f
        else -> 33f
    }
    val stop = when (type) {
        1 -> 0.85f
        2 -> 0.8f
        else -> 0.68f
    }
    var pressed by remember {
        mutableStateOf(false)
    }
    Column(modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .clickable(onClick = onTap ?: {})
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            pressed = true
                            onTapDown()
                            return@pointerInteropFilter true
                        }
                        MotionEvent.ACTION_UP -> {
                            pressed = false
                            onTapUp()
                            return@pointerInteropFilter true
                        }
                        else -> return@pointerInteropFilter false
                    }
                }
                .size(size.dp)
                .clip(CircleShape)
                .border(BorderStroke(0.5.dp, Color.Black), CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (pressed)
                            listOf(Color.Black, Color.White)
                        else listOf(Color.White, Color.Black)
                    )
                ),
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                Pair(0f, color),
                                Pair(stop, color),
                                Pair(1f, color.copy(alpha = 0.3f)),
                            )
                        )
                    )
            )
        }
        text?.let { Text(it, style = TextStyle(fontSize = 12.sp)) }
    }
}

@Composable
fun Block(color: MutableState<BlockColor>) {
    val blockColor = when (color.value) {
        BlockColor.Black -> Color.Black
        BlockColor.White -> Color(0x33000000)
        BlockColor.Red -> Color(0xff660000)
    }
    Box(
        Modifier
            .size(blockWidth)
            .padding(0.5.dp)
            .border(1.5.dp, blockColor)
            .background(Color.Transparent)
    ) {
        Surface(
            Modifier
                .matchParentSize()
                .padding(2.5.dp),
            color = blockColor
        ) {

        }
    }
}