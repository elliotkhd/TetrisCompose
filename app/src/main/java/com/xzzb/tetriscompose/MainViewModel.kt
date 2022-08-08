package com.xzzb.tetriscompose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import java.util.*

val blockWidth = 13.dp

class MainViewModel : ViewModel() {
    val startLine = 9
    val speedLevels = listOf(1000L, 900L, 800L, 700L, 600L, 500L)
    val timerDuration = 80L
    val delayBeforeStarted = 200L
    val autoTimerDuration = 300L
    var rightTimerStarted = false
    var leftTimerStarted = false
    var downTimerStarted = false
    val autoTimerDurationWhenManual = 500L
    var speedLevel = 5
    var paused = true
    val shapeTypeStack = mutableListOf<MutableState<ShapeType>>()
    lateinit var shape: Shape
    lateinit var waitShape: Shape
    var looping = false
    var moveHorizontally = false
    var moveVertically = false
    var movingLines = false
    var nearBounce = false
    val sampleShapes = listOf(
        listOf(
            0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0
        ),
        listOf(
            0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0
        ),
        listOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )
    )
    val blocks = (0..19).map { i ->
        (0..9).map { j ->
            mutableStateOf(
                if (i > startLine)
                    if (Random().nextInt(10) < 7)
                        BlockColor.Black else
                        BlockColor.White
                else BlockColor.White
            )
        }
    }

    val waitBlocks = (0..1).map { i ->
        (0..3).map {
            mutableStateOf(BlockColor.White)
        }
    }

    init {
        shapeTypeStack.addAll(
            listOf(
                mutableStateOf(ShapeType.values()[(0..10000).random() % 7]),
                mutableStateOf(ShapeType.values()[(0..10000).random() % 7])
            )
        )
        initShapeOnTop()
    }

    fun reInitAllBlocks() {
        for (i in 0..19) {
            for (j in 0..9) {
                blocks[i][j].value = if (i > startLine)
                    if (Random().nextInt(10) < 7)
                        BlockColor.Black else
                        BlockColor.White
                else BlockColor.White
            }
        }
    }

    fun initShapeOnTop() {
        val type = shapeTypeStack.first().value
        val y = if (type == ShapeType.I) 1 else 2
        shape = Shape(type, 4, -y)

    }

    fun initShapeOnWaiting() {
        val waitingType = shapeTypeStack.last().value
        waitShape = Shape(
            waitingType,
            if (waitingType == ShapeType.I || waitingType == ShapeType.O) 1 else 0,
            0
        )
        val blocks = waitShape.blocks
        for (i in 0..1) {
            for (j in 0..3) {
                waitBlocks[i][j].value = BlockColor.White
            }
        }
        for (value in blocks) {
            if (value.y in 0..1 && value.x in 0..3) {
                waitBlocks[value.y][value.x].value = BlockColor.Black
            }
        }
        shapeTypeStack.first().value = shapeTypeStack.last().value
        shapeTypeStack.last().value = ShapeType.values()[(0..10000).random() % 7]
    }


    suspend fun startAutoDrop() {
        looping = true
        while (true) {
            delay(
                if (moveHorizontally) autoTimerDurationWhenManual
                else speedLevels[speedLevel]
            )
            if (paused || moveVertically || movingLines) continue
            moveDown()
        }
    }

    fun hideShape() {
        for (value in shape.blocks) {
            if (value.y in 0..19 && value.x in 0..9) {
                blocks[value.y][value.x].value = BlockColor.White
            }
        }
    }

    fun cancelAllTimer() {
        downTimerStarted = false
        leftTimerStarted = false
        rightTimerStarted = false
    }

    fun canMoveDown(): Boolean {
        for (block in shape.blocks) {
            if (block.y + 1 == getFirstBlockBelowShapeInColumnX(block.x)) {
                return false
            }
        }
        return true
    }

    private fun getFirstBlockBelowShapeInColumnX(x: Int): Int {
        for (i in (shape.bottomOnX(x) + 1)..19) {
            if (blocks[i][x].value == BlockColor.Black) {
                return i
            }
        }
        return 20
    }

    fun drawShape() {
        val shapeBlocks = shape.blocks
        for (value in shapeBlocks) {
            if (value.y in 0..19 &&
                value.x in 0..9
            ) {
                blocks[value.y][value.x].value = BlockColor.Black
            }
        }
    }

    fun isLineCompleted(line: List<MutableState<BlockColor>>): Boolean {
        for (value in line) {
            if (value.value == BlockColor.White) {
                return false
            }
        }
        return true
    }

    suspend fun shineLines(lines: List<Int>) {
        paused = true
        for (value in lines) {
            for (value1 in blocks[value]) {
                value1.value = BlockColor.Red
            }
        }
        for (i in 0..6) {
            delay(70L)
            for (value in lines) {
                for (value1 in blocks[value]) {
                    value1.value = if (value1.value == BlockColor.Red)
                        BlockColor.White
                    else BlockColor.Red
                }
            }
        }
    }

    fun moveLineDownward(y: Int, num: Int) {
        val tmpLineY = listOf(*blocks[y].toTypedArray())
        for (i in 0..9) {
            blocks[y + num][i].value = tmpLineY[i].value
        }
    }

    suspend fun removeCompletedLine() {
        movingLines = true
        val completedLines = mutableListOf<Int>()
        for (i in 0..19) {
            if (isLineCompleted(blocks[i])) {
                completedLines.add(i)
            }
        }

        if (completedLines.isEmpty()) {
            movingLines = false
            return
        }
        shineLines(completedLines)
        for (i in 19 downTo 0) {
            val emptyLinesBelow =
                completedLines.filter { it > i }.size
            moveLineDownward(i, emptyLinesBelow)
        }
        paused = false
        movingLines = false
    }

    suspend fun moveDown() {
        hideShape()
        if (!canMoveDown()) {
            cancelAllTimer()
            drawShape()
            removeCompletedLine()
            initShapeOnTop()
            return
        }
        shape.moveDown()
        drawShape()
    }

    suspend fun shineShape() {
        movingLines = true
        for (value in (shape.blocks)) {
            blocks[value.y][value.x].value = BlockColor.Red
        }
        delay(70L)
        for (value in (shape.blocks)) {
            blocks[value.y][value.x].value = BlockColor.Black
        }
        movingLines = false
    }

    suspend fun moveToBottom() {
        if (movingLines) return
        if (paused) {
            paused = false
            if (!looping) startAutoDrop()
            return
        }
        hideShape()
        while (canMoveDown()) {
            shape.moveDown()
        }
        cancelAllTimer()
        drawShape()
        shineShape()
        removeCompletedLine()
        initShapeOnTop()
    }
}