package com.xzzb.tetriscompose

import kotlin.math.max
import kotlin.math.min


enum class ShapeType { Z, S, T, O, L, J, I }
enum class BlockColor { Black, White, Red }


class Shape(val type: ShapeType, x: Int, y: Int, var rotateIndex: Int = 0) {
    var blocks: List<BlockStatus> = getBlocksByCoordinates(type, x, y, rotateIndex)
    var x = x
        set(value) {
            field = value
            blocks = getBlocksByCoordinates(type, value, y, rotateIndex)
        }
    var y = y
        set(value) {
            field = value
            blocks = getBlocksByCoordinates(type, x, value, rotateIndex)
        }

    fun top(): Int {
        var result = 20
        for (value in blocks) {
            result = min(value.y, result)
        }
        return result
    }

    fun bottom(): Int {
        var result = 0
        for (value in blocks) {
            result = max(value.y, result)
        }
        return result
    }

    fun left(): Int {
        var result = 10
        for (value in blocks) {
            result = min(value.x, result)
        }
        return result
    }

    fun right(): Int {
        var result = 0
        for (value in blocks) {
            result = max(value.x, result)
        }
        return result
    }

    fun bottomOnX(x: Int): Int {
        var result = 0;
        for (value in blocks) {
            if (value.x == x) result = max(value.y, result);
        }
        return result;
    }

    fun moveRight() {
        for (block in blocks) {
            if (block.x + 1 >= 10) return;
        }
        x += 1;
    }

    fun moveLeft() {
        for (block in blocks) {
            if (block.x - 1 < 0) return;
        }
        x -= 1;
    }

    fun moveDown() {
        for (block in blocks) {
            if (block.y + 1 == 20) return;
        }
        y += 1;
    }

    companion object {
        fun getBlocksByCoordinates(
            type: ShapeType, x: Int, y: Int, rotateIndex: Int
        ): List<BlockStatus> {
            val tmpList = mutableListOf<BlockStatus>()

            val shapeList = rotateMap[type]
            val fixedRotateIndex = rotateIndex % shapeList!!.size
            val tmp = shapeList[fixedRotateIndex]
            tmp.forEachIndexed { i, item ->
                item.forEachIndexed { j, _ ->
                    if (tmp[i][j] == 1) {
                        tmpList.add(
                            BlockStatus(
                                x + j + offsetMap[type]!![fixedRotateIndex][0],
                                y + i + offsetMap[type]!![fixedRotateIndex][1]
                            )
                        )
                    }
                }
            }
            return tmpList
        }

        val offsetMap = mapOf(
            ShapeType.I to listOf(
                listOf(-1, 0),
                listOf(0, -1)
            ),
            ShapeType.T to listOf(
                listOf(0, 0),
                listOf(0, 0),
                listOf(0, 1),
                listOf(1, 0)
            ),
            ShapeType.Z to listOf(
                listOf(0, 0),
                listOf(0, 0)
            ),
            ShapeType.S to listOf(
                listOf(0, 0),
                listOf(0, 0)
            ),
            ShapeType.O to listOf(
                listOf(0, 0)
            ),
            ShapeType.L to listOf(
                listOf(0, 0),
                listOf(0, 0),
                listOf(0, 0),
                listOf(0, 0),
            ),
            ShapeType.J to listOf(
                listOf(0, 0),
                listOf(0, 0),
                listOf(0, 0),
                listOf(0, 0),
            ),
        )


        val rotateMap = mapOf(

            ShapeType.I to listOf(
                listOf(
                    listOf(1, 1, 1)
                ),
                listOf(
                    listOf(1),
                    listOf(1),
                    listOf(1),
                    listOf(1),
                )
            ),
            ShapeType.T to listOf(
                listOf(
                    listOf(0, 1, 0),
                    listOf(1, 1, 1),
                ),
                listOf(
                    listOf(0, 1),
                    listOf(1, 1),
                    listOf(0, 1),
                ),
                listOf(
                    listOf(1, 1, 1),
                    listOf(0, 1, 0),
                ),
                listOf(
                    listOf(1, 0),
                    listOf(1, 1),
                    listOf(1, 0),
                ),
            ),
            ShapeType.Z to listOf(
                listOf(
                    listOf(1, 1, 0),
                    listOf(0, 1, 1),
                ),
                listOf(
                    listOf(0, 1),
                    listOf(1, 1),
                    listOf(1, 0),
                )
            ),
            ShapeType.S to listOf(
                listOf(
                    listOf(0, 1, 1),
                    listOf(1, 1, 0),
                ),
                listOf(
                    listOf(1, 0),
                    listOf(1, 1),
                    listOf(0, 1),
                )
            ),
            ShapeType.O to listOf(
                listOf(
                    listOf(1, 1),
                    listOf(1, 1),
                )
            ),
            ShapeType.L to listOf(
                listOf(
                    listOf(0, 0, 1),
                    listOf(1, 1, 1),
                ),
                listOf(
                    listOf(1, 1),
                    listOf(0, 1),
                    listOf(0, 1),
                ),
                listOf(
                    listOf(1, 1, 1),
                    listOf(1, 0, 0),
                ),
                listOf(
                    listOf(1, 0),
                    listOf(1, 0),
                    listOf(1, 1),
                ),
            ),
            ShapeType.J to listOf(
                listOf(
                    listOf(1, 0, 0),
                    listOf(1, 1, 1),
                ),
                listOf(
                    listOf(0, 1),
                    listOf(0, 1),
                    listOf(1, 1),
                ),
                listOf(
                    listOf(1, 1, 1),
                    listOf(0, 0, 1),
                ),
                listOf(
                    listOf(1, 1),
                    listOf(1, 0),
                    listOf(1, 0),
                ),
            )

        )

    }
}

class BlockStatus(var x: Int, var y: Int, var activated: Boolean = true)