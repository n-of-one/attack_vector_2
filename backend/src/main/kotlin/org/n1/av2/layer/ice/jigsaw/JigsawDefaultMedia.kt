package org.n1.av2.layer.ice.jigsaw

import org.n1.av2.site.entity.enums.IceStrength

/**
 * Default media (images / videos) available to the puzzle, keyed by ICE strength.
 *
 * The creator picks a random entry from the bucket matching the layer's strength at creation
 * time, so every hacker entering the same ICE sees the same media.
 *
 * Paths are served by the frontend under /img/frontier/ice/jigsaw/… — no backend static
 * serving change needed. Content classification per bucket is a tuning decision; adjust as
 * desired without touching creator logic.
 */
val DEFAULT_MEDIA: Map<IceStrength, List<String>> = mapOf(
    IceStrength.VERY_WEAK to listOf(
        "/img/frontier/ice/jigsaw/green.png",
    ),
    IceStrength.WEAK to listOf(
        "/img/frontier/ice/jigsaw/green.png",
        "/img/frontier/ice/jigsaw/gemini-aker.png",
    ),
    IceStrength.AVERAGE to listOf(
        "/img/frontier/ice/jigsaw/gemini-aker.png",
        "/img/frontier/ice/jigsaw/barbaraalane-fractal-1992882.jpg",
        "/img/frontier/ice/jigsaw/barbaraalane-fractal-2035686.jpg",
        "/img/frontier/ice/jigsaw/barbaraalane-fractal-2109980.jpg",
    ),
    IceStrength.STRONG to listOf(
        "/img/frontier/ice/jigsaw/tylijura-ai-generated-9396797_1920.png",
        "/img/frontier/ice/jigsaw/barbaraalane-fractal-2109980.jpg",
    ),
    IceStrength.VERY_STRONG to listOf(
        "/img/frontier/ice/jigsaw/7020150_Vj_Loop_1920x1080.mp4",
        "/img/frontier/ice/jigsaw/tylijura-ai-generated-9396797_1920.png",
    ),
    IceStrength.ONYX to listOf(
        "/img/frontier/ice/jigsaw/2026-04-07_22-29-34.mp4",
        "/img/frontier/ice/jigsaw/7020150_Vj_Loop_1920x1080.mp4",
    ),
)
