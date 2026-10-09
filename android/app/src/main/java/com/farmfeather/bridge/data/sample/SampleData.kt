package com.farmfeather.bridge.data.sample

import com.farmfeather.bridge.domain.model.Mission

object SampleData {

    val missions: List<Mission> = listOf(
        Mission(
            id = 1,
            name = "Creek Crossing",
            hint = "One straight creek runs between the coop and the meadow",
            grid = listOf(
                "...B..",
                "......",
                "......",
                "~~~~~~",
                "......",
                "......",
                "..C..."
            ),
            straightPlanks = 3,
            cornerPlanks = 0
        ),
        Mission(
            id = 2,
            name = "Willow Bend",
            hint = "The creek bends under the old willow, turn a corner plank",
            grid = listOf(
                "...B..",
                "......",
                "...~..",
                "..~~..",
                "......",
                "......",
                "..C..."
            ),
            straightPlanks = 2,
            cornerPlanks = 2
        ),
        Mission(
            id = 3,
            name = "Cart Track",
            hint = "A farm cart rolls down the lane, time the crossing",
            grid = listOf(
                "...B..",
                "......",
                "......",
                "======",
                ".~~~~.",
                "......",
                "..C..."
            ),
            straightPlanks = 3,
            cornerPlanks = 2
        ),
        Mission(
            id = 4,
            name = "Twin Streams",
            hint = "Two streams and one lane stand between the flock and the barn",
            grid = listOf(
                "...B..",
                ".~~~~.",
                "......",
                "~~~~~~",
                "......",
                "======",
                "..C..."
            ),
            straightPlanks = 4,
            cornerPlanks = 3
        ),
        Mission(
            id = 5,
            name = "Stony Ford",
            hint = "A rock cluster blocks the shortest line, go around it",
            grid = listOf(
                "...B..",
                "......",
                "======",
                "~~#~~~",
                "......",
                "......",
                "..C..."
            ),
            straightPlanks = 3,
            cornerPlanks = 3
        ),
        Mission(
            id = 6,
            name = "Dusk Run",
            hint = "Two lanes, two streams and the last light of the day",
            grid = listOf(
                "...B..",
                ".~~~~.",
                "======",
                "......",
                "~~~~~~",
                "======",
                "..C..."
            ),
            straightPlanks = 5,
            cornerPlanks = 4
        )
    )
}
