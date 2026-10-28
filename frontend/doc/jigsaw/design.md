
## CELLS - strength: AVERAGE,
const float SCALE = 2.0;                             // Number of cells across the height.
const float WOBBLE = 0.4;                            // How far cell centers move (0..0.5).
const float MOVE_SPEED = 0.3;                        // Speed of the cell movement.
const float HUE_RANGE = 0.35;                        // Spread of cell hues. 0 makes all cells the same hue.
const float HUE_OFFSET = 0.5;                        // Base hue.
const float HUE_SPEED = 0.1;                         // Speed at which all hues shift.
const float EDGE_WIDTH = 0.08;                       // Width of the glowing cell borders.
const vec3 EDGE_COLOR = vec3(0.2, 0.9, 1.0);         // Color of the cell borders.

speed = 0.25
This felt nice. It takes a while to get into it, but it's doable.

## WARP - strength: AVERAGE
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

speed = 1
This was a challenge but rewarding. It would be much simpler on speed = 0.25

