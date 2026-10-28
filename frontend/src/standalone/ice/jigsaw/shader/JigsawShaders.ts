/**
 * Procedural GLSL (WebGL 1) fragment shaders usable as jigsaw media via `shader:<name>`.
 *
 * Each shader receives `time` (seconds, scaled by the speed controls) and `resolution` (pixels).
 * The tweakable parameters are the constants at the start of each `main()`.
 *
 * They represent different kinds of motion, to experiment with how motion affects puzzle difficulty:
 *
 * - palette: fixed shape, only the colors cycle (easiest: shapes stay put).
 * - cells:   drifting Voronoi cells, structure moves slowly but stays recognizable.
 * - warp:    domain-warped noise flowing like ocean currents (structure drifts and morphs).
 * - julia:   Julia set whose shape morphs continuously (hard: the whole image changes).
 * - ripples: interference of moving wave sources, constant outward motion (hard).
 */

// Must start with "precision": Pixi prepends its own (mediump) precision otherwise.
const HEADER = `precision highp float;
uniform float time;
uniform vec2 resolution;
`

const PALETTE = HEADER + `
void main() {
    const vec2 CENTER = vec2(-0.7453, 0.1127);         // Point in the Mandelbrot set to look at (seahorse valley).
    const float ZOOM = 0.012;                           // Visible height in set coordinates. Smaller zooms in.
    const int MAX_ITERATIONS = 400;                     // More reveals finer detail near the set, costs GPU.
    const float BAND_DENSITY = 0.04;                    // Color bands per iteration. Higher gives narrower bands.
    const float CYCLE_SPEED = 0.12;                     // Palette cycles per second.
    const vec3 PALETTE_PHASE = vec3(0.0, 0.15, 0.3);    // Red/green/blue offsets: determines the hues.
    const vec3 INTERIOR_COLOR = vec3(0.0, 0.0, 0.02);   // Color inside the set.

    vec2 c = CENTER + (gl_FragCoord.xy - 0.5 * resolution) / resolution.y * ZOOM;
    vec2 z = vec2(0.0);
    float iteration = 0.0;
    for (int i = 0; i < MAX_ITERATIONS; i++) {
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        if (dot(z, z) > 256.0) break;
        iteration += 1.0;
    }
    if (iteration >= float(MAX_ITERATIONS)) {
        gl_FragColor = vec4(INTERIOR_COLOR, 1.0);
        return;
    }
    float smoothIteration = iteration - log2(log2(dot(z, z))) + 4.0;
    vec3 color = 0.5 + 0.5 * cos(6.2831 * (smoothIteration * BAND_DENSITY - time * CYCLE_SPEED + PALETTE_PHASE));
    gl_FragColor = vec4(color, 1.0);
}
`

const CELLS = HEADER + `
vec2 hash2(vec2 point) {
    point = vec2(dot(point, vec2(127.1, 311.7)), dot(point, vec2(269.5, 183.3)));
    return fract(sin(point) * 43758.5453);
}

void main() {
    const float SCALE = 6.0;                             // Number of cells across the height.
    const float WOBBLE = 0.4;                            // How far cell centers move (0..0.5).
    const float MOVE_SPEED = 0.3;                        // Speed of the cell movement.
    const float HUE_RANGE = 0.35;                        // Spread of cell hues. 0 makes all cells the same hue.
    const float HUE_OFFSET = 0.5;                        // Base hue.
    const float HUE_SPEED = 0.1;                         // Speed at which all hues shift.
    const float EDGE_WIDTH = 0.08;                       // Width of the glowing cell borders.
    const vec3 EDGE_COLOR = vec3(0.2, 0.9, 1.0);         // Color of the cell borders.

    vec2 uv = gl_FragCoord.xy / resolution.y * SCALE;
    vec2 cell = floor(uv);
    vec2 local = fract(uv);

    float nearest = 8.0;
    float second = 8.0;
    vec2 nearestId = vec2(0.0);
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 random = hash2(cell + offset);
            vec2 point = offset + 0.5 + WOBBLE * sin(time * MOVE_SPEED + 6.2831 * random) - local;
            float squaredDistance = dot(point, point);
            if (squaredDistance < nearest) {
                second = nearest;
                nearest = squaredDistance;
                nearestId = cell + offset;
            } else if (squaredDistance < second) {
                second = squaredDistance;
            }
        }
    }

    float edge = sqrt(second) - sqrt(nearest);
    vec2 cellRandom = hash2(nearestId);
    vec3 base = 0.5 + 0.5 * cos(6.2831 * (cellRandom.x * HUE_RANGE + HUE_OFFSET + vec3(0.0, 0.1, 0.2)) + time * HUE_SPEED);
    vec3 color = base * (0.2 + 0.7 * (1.0 - sqrt(nearest))) * (0.6 + 0.4 * cellRandom.y);
    color += EDGE_COLOR * smoothstep(EDGE_WIDTH, 0.0, edge);
    gl_FragColor = vec4(color, 1.0);
}
`

const WARP = HEADER + `
float hash(vec2 point) {
    point = fract(point * vec2(123.34, 456.21));
    point += dot(point, point + 45.32);
    return fract(point.x * point.y);
}

float noise(vec2 point) {
    vec2 cell = floor(point);
    vec2 local = fract(point);
    vec2 blend = local * local * (3.0 - 2.0 * local);
    return mix(
        mix(hash(cell), hash(cell + vec2(1.0, 0.0)), blend.x),
        mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), blend.x),
        blend.y);
}

float fbm(vec2 point) {
    const int OCTAVES = 5;                               // Layers of detail. Fewer is softer, more adds grain.
    const float DETAIL_FALLOFF = 0.5;                    // Strength of each finer layer. Higher is grittier.
    const float DETAIL_SCALE = 2.0;                      // Size step between layers.

    float value = 0.0;
    float amplitude = 0.5;
    mat2 rotation = mat2(0.8, 0.6, -0.6, 0.8);
    for (int octave = 0; octave < OCTAVES; octave++) {
        value += amplitude * noise(point);
        point = rotation * point * DETAIL_SCALE + vec2(1.7, 9.2);
        amplitude *= DETAIL_FALLOFF;
    }
    return value;
}

void main() {
    const float SCALE = 3.0;                             // Feature size. Higher gives more, smaller swirls.
    const float DRIFT_SPEED = 0.05;                      // How fast it flows.
    const float WARP_STRENGTH = 4.0;                     // Folding: 1-2 clouds, 4 marbled ink, 6+ chaotic.
    const vec2 SECOND_LAYER_DRIFT = vec2(0.7, -0.5);     // Flow of the second warp layer; differing values make shapes morph.
    const vec3 DEEP_COLOR = vec3(0.02, 0.05, 0.12);      // Darkest areas.
    const vec3 MAIN_COLOR = vec3(0.0, 0.55, 0.65);       // Dominant color.
    const vec3 ACCENT_COLOR = vec3(0.85, 0.25, 0.55);    // Where the first warp layer is strong.
    const vec3 HIGHLIGHT_COLOR = vec3(0.95, 0.9, 0.7);   // Bright streaks.
    const float BRIGHTNESS_FLOOR = 0.3;                  // Minimum brightness.
    const float CONTRAST = 1.3;                          // Brightness added by the pattern.

    vec2 uv = gl_FragCoord.xy / resolution.y * SCALE;
    float drift = time * DRIFT_SPEED;

    vec2 first = vec2(fbm(uv + vec2(0.0, drift)), fbm(uv + vec2(5.2, 1.3) - drift));
    vec2 second = vec2(
        fbm(uv + WARP_STRENGTH * first + vec2(1.7, 9.2) + SECOND_LAYER_DRIFT.x * drift),
        fbm(uv + WARP_STRENGTH * first + vec2(8.3, 2.8) + SECOND_LAYER_DRIFT.y * drift));
    float value = fbm(uv + WARP_STRENGTH * second);

    vec3 color = mix(DEEP_COLOR, MAIN_COLOR, clamp(value * value * 2.5, 0.0, 1.0));
    color = mix(color, ACCENT_COLOR, clamp(length(first) - 0.6, 0.0, 1.0));
    color = mix(color, HIGHLIGHT_COLOR, clamp(second.x * second.x - 0.2, 0.0, 1.0) * 0.6);
    gl_FragColor = vec4(color * (BRIGHTNESS_FLOOR + CONTRAST * value), 1.0);
}
`

const JULIA = HEADER + `
void main() {
    const float ZOOM = 1.9;                              // Visible height in set coordinates. 1.9 spans the full width.
    const float MORPH_SPEED = 0.04;                      // How fast the shape morphs.
    const float C_RADIUS = 0.7885;                       // Circle that c travels; around 0.78 stays at the interesting edge.
    const int MAX_ITERATIONS = 200;                      // More reveals finer detail, costs GPU.
    const float BAND_DENSITY = 0.15;                     // Color bands per iteration.
    const vec3 PALETTE_PHASE = vec3(3.0, 3.6, 4.0);      // Red/green/blue offsets: determines the hues.
    const float OUTER_BRIGHTNESS = 0.35;                 // Brightness far away from the set.
    const float GLOW_SHARPNESS = 10.0;                   // Higher gives thinner glowing filaments.
    const vec3 GLOW_COLOR = vec3(0.1, 0.25, 0.35);       // Filaments outside the set.
    const vec3 INTERIOR_COLOR = vec3(0.05, 0.0, 0.12);   // Inside the set.
    const vec3 INTERIOR_GLOW_COLOR = vec3(0.6, 0.3, 0.9);// Filaments inside the set.

    vec2 z = (gl_FragCoord.xy - 0.5 * resolution) / resolution.y * ZOOM;
    float angle = time * MORPH_SPEED;
    vec2 c = C_RADIUS * vec2(cos(angle), sin(angle));
    float iteration = 0.0;
    // Orbit trap: closest approach of the orbit to the axes. Gives filaments in regions far from the set,
    // so tiles away from the center are not empty.
    float trap = 1000.0;
    for (int i = 0; i < MAX_ITERATIONS; i++) {
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        trap = min(trap, abs(z.x * z.y));
        if (dot(z, z) > 256.0) break;
        iteration += 1.0;
    }
    float glow = exp(-trap * GLOW_SHARPNESS);
    if (iteration >= float(MAX_ITERATIONS)) {
        gl_FragColor = vec4(INTERIOR_COLOR + INTERIOR_GLOW_COLOR * glow, 1.0);
        return;
    }
    float smoothIteration = iteration - log2(log2(dot(z, z))) + 4.0;
    vec3 color = 0.5 + 0.5 * cos(smoothIteration * BAND_DENSITY + PALETTE_PHASE);
    color *= OUTER_BRIGHTNESS + (1.0 - OUTER_BRIGHTNESS) * clamp(smoothIteration * 0.08, 0.0, 1.0);
    color += GLOW_COLOR * glow;
    gl_FragColor = vec4(color, 1.0);
}
`

const RIPPLES = HEADER + `
void main() {
    const int SOURCE_COUNT = 4;                          // Number of wave sources.
    const float WAVE_DENSITY = 60.0;                     // Waves per screen height. Higher gives thinner rings.
    const float WAVE_SPEED = 0.8;                        // How fast the waves travel outward.
    const float SOURCE_SPEED = 0.06;                     // How fast the sources wander around.
    const vec3 BACKGROUND_COLOR = vec3(0.02, 0.02, 0.08);
    const vec3 WAVE_COLOR = vec3(0.95, 0.5, 0.1);
    const vec3 CREST_COLOR = vec3(0.3, 0.8, 1.0);        // Where waves reinforce each other.

    vec2 uv = gl_FragCoord.xy / resolution.y;
    float sum = 0.0;
    for (int i = 0; i < SOURCE_COUNT; i++) {
        float index = float(i);
        vec2 source = vec2(
            0.9 + 0.7 * sin(time * SOURCE_SPEED * 1.2 * (1.0 + 0.3 * index) + index * 1.9),
            0.5 + 0.45 * cos(time * SOURCE_SPEED * 0.8 * (1.0 + 0.2 * index) + index * 2.7));
        sum += sin(length(uv - source) * WAVE_DENSITY - time * WAVE_SPEED);
    }
    float value = sum / float(SOURCE_COUNT);
    vec3 color = mix(BACKGROUND_COLOR, WAVE_COLOR, smoothstep(-0.2, 0.6, value));
    color = mix(color, CREST_COLOR, smoothstep(0.5, 0.95, value));
    gl_FragColor = vec4(color, 1.0);
}
`

export const JIGSAW_SHADERS: { [name: string]: string } = {
    palette: PALETTE,
    cells: CELLS,
    warp: WARP,
    julia: JULIA,
    ripples: RIPPLES,
}
