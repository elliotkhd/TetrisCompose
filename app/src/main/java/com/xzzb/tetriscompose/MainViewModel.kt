package com.xzzb.tetriscompose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import java.util.*


val blockWidth = 13.dp

class MainViewModel : ViewModel() {
    private val startLine = 19
    private val speedLevels = listOf(
        1000L, 850,
        700L, 550L,
        400L, 250L
    )
    private val timerDuration = 80L
    private val delayBeforeStarted = 200L
    var rightTimerStarted = false
    var leftTimerStarted = false
    var downTimerStarted = false
    private val autoTimerDurationWhenManual = 500L
    private var speedLevel = 5
    var paused = true
    private val shapeTypeStack = mutableListOf<MutableState<ShapeType>>()
    private lateinit var shape: Shape
    private lateinit var waitShape: Shape
    private var looping = false
    var moveHorizontally = false
    var moveVertically = false
    private var movingLines = false
    var nearBounce = false
    private var _leftInProcess = false
    private var _rightInProcess = false
    private var _downInProcess = false

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
        (0..9).map {
            mutableStateOf(
                if (i > startLine)
                    if (Random().nextInt(10) < 7)
                        BlockColor.Black else
                        BlockColor.White
                else BlockColor.White
            )
        }
    }

    val waitBlocks = (0..1).map {
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

    private fun initShapeOnTop() {
        val type = shapeTypeStack.first().value
        val y = if (type == ShapeType.I) 1 else 2
        shape = Shape(type, 4, -y)
        initShapeOnWaiting()
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


    private suspend fun startAutoDrop() {
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

    private fun hideShape() {
        for (value in shape.blocks) {
            if (value.y in 0..19 && value.x in 0..9) {
                blocks[value.y][value.x].value = BlockColor.White
            }
        }
    }

    private fun cancelAllTimer() {
        downTimerStarted = false
        leftTimerStarted = false
        rightTimerStarted = false
    }

    private fun canMoveDown(): Boolean {
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

    private fun drawShape() {
        val shapeBlocks = shape.blocks
        for (value in shapeBlocks) {
            if (value.y in 0..19 &&
                value.x in 0..9
            ) {
                blocks[value.y][value.x].value = BlockColor.Black
            }
        }
    }

    private fun isLineCompleted(line: List<MutableState<BlockColor>>): Boolean {
        for (value in line) {
            if (value.value == BlockColor.White) {
                return false
            }
        }
        return true
    }

    private suspend fun shineLines(lines: List<Int>) {
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

    private fun moveLineDownward(y: Int, num: Int) {
        val tmpLineY = listOf(*blocks[y].toTypedArray())
        for (i in 0..9) {
            blocks[y + num][i].value = tmpLineY[i].value
        }
    }

    private suspend fun removeCompletedLine() {
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

    private suspend fun moveDown() {
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

    private suspend fun shineShape() {
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

    private fun moveRight() {
        hideShape()
        for (block in shape.blocks) {
            if (block.x + 1 == getFirstActivatedBlockOnRight(block.x, block.y)) {
                drawShape()
                return
            }
        }
        shape.moveRight()
        drawShape()
    }

    private fun moveLeft() {
        hideShape()
        for (block in shape.blocks) {
            if (block.x - 1 == getFirstActivatedBlockOnLeft(block.x, block.y)) {
                drawShape()
                return
            }
        }
        shape.moveLeft()
        drawShape()
    }

    fun rotate() {
        hideShape()
        val tmpShape = shape.copy()
        tmpShape.rotate()
        for (block in tmpShape.blocks) {
            if (block.y >= 20 ||
                block.x < 0 ||
                block.x >= 10 ||
                (block.y >= 0 &&
                        blocks[block.y][block.x].value == BlockColor.Black)
            ) {
                drawShape()
                return
            }
        }
        shape.rotate()
        drawShape()
    }

    private fun getFirstActivatedBlockOnRight(x: Int, y: Int): Int {
        for (i in x..9) {
            if (y >= 0 && blocks[y][i].value === BlockColor.Black) {
                return i
            }
        }
        return 10
    }

    private fun getFirstActivatedBlockOnLeft(x: Int, y: Int): Int {
        for (i in x downTo 0) {
            if (y >= 0 && blocks[y][i].value === BlockColor.Black) {
                return i
            }
        }
        return 10
    }

    suspend fun turnAround() {
        for (i in 0..19) {
            for (j in 0..9) {
                blocks[i][j].value = BlockColor.White
            }
        }
        delay(5L)
        for (i in 0..19) {
            for (j in 0..9) {
                blocks[i][j].value = BlockColor.Black
            }
        }
    }

    suspend fun moveRightForTimes() {
        if (movingLines) return
        moveHorizontally = true
        cancelAllTimer()
        moveRight()
        if (_rightInProcess) return
        _rightInProcess = true
        rightTimerStarted = true
        delay(delayBeforeStarted)
        _rightInProcess = true
        if (!rightTimerStarted) {
            _rightInProcess = false
            return
        }
        while (rightTimerStarted) {
            moveRight()
            delay(timerDuration)
            _rightInProcess = false
        }
    }

    suspend fun moveLeftForTimes() {
        if (movingLines) return
        moveHorizontally = true
        cancelAllTimer()
        moveLeft()
        if (_leftInProcess) return
        _leftInProcess = true
        leftTimerStarted = true
        delay(delayBeforeStarted)
        _rightInProcess = true
        if (!leftTimerStarted) {
            _leftInProcess = false
            return
        }
        while (leftTimerStarted) {
            moveLeft()
            delay(timerDuration)
            _leftInProcess = false
        }
    }

    suspend fun moveDownForTimes() {
        if (movingLines) return
        moveVertically = true
        cancelAllTimer()
        moveDown()
        if (_downInProcess) return
        _downInProcess = true
        downTimerStarted = true
        delay(delayBeforeStarted)
        if (!downTimerStarted) {
            _downInProcess = false
            return
        }
        while (downTimerStarted) {
            moveDown()
            delay(timerDuration)
            _downInProcess = false
        }
    }
}