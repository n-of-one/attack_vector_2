import {Geometry, GlProgram, Mesh, Renderer, RenderTexture, Shader, UniformGroup} from "pixi.js";
import {IMAGE_HEIGHT, IMAGE_WIDTH} from "../component/JigsawShapes";
import {JIGSAW_SHADERS} from "./JigsawShaders";

/**
 * Render resolution relative to IMAGE_WIDTH x IMAGE_HEIGHT. The puzzle is displayed at PUZZLE_SCALE (0.8), so rendering at full
 * size is wasted work. Must be a multiple of 1/32 so both 1376 and 768 scale to whole pixels: 26/32 gives 1118 x 624, just above
 * the displayed 1101 x 614. 16/32 (half) is 4x cheaper than full size and still looks fine for soft shaders like warp.
 */
const RENDER_RESOLUTION = 26 / 32

const VERTEX_SHADER = `
attribute vec2 aPosition;
void main() {
    gl_Position = vec4(aPosition, 0.0, 1.0);
}
`

/**
 * Renders a procedural fragment shader into a RenderTexture inside Pixi's own WebGL context; that texture is
 * the shared puzzle texture. Rendering in the same context avoids copying each frame between WebGL contexts,
 * which Firefox does via the CPU. The jigsaw ticker drives `advance`.
 */
export class ShaderAnimation {

    readonly texture: RenderTexture
    private readonly mesh: Mesh<Geometry, Shader>
    private readonly uniforms: UniformGroup
    private seconds = 0
    private speed = 1

    constructor(name: string) {
        const fragmentSource = JIGSAW_SHADERS[name]
        if (!fragmentSource) throw new Error(`Unknown jigsaw shader: ${name}`)

        // Logical size stays IMAGE_WIDTH x IMAGE_HEIGHT, so the pieces need no changes; only the pixel size shrinks.
        this.texture = RenderTexture.create({width: IMAGE_WIDTH, height: IMAGE_HEIGHT, resolution: RENDER_RESOLUTION})

        // The shaders normalize gl_FragCoord by this, so the picture is the same at any render resolution.
        const pixelSize = new Float32Array([this.texture.source.pixelWidth, this.texture.source.pixelHeight])
        this.uniforms = new UniformGroup({
            time: {value: 0, type: 'f32'},
            resolution: {value: pixelSize, type: 'vec2<f32>'},
        })
        const shader = new Shader({
            glProgram: GlProgram.from({vertex: VERTEX_SHADER, fragment: fragmentSource}),
            resources: {shaderUniforms: this.uniforms},
        })
        // One quad covering the whole render target, in clip space: the vertex shader ignores Pixi's transforms.
        const geometry = new Geometry({
            attributes: {
                aPosition: {buffer: new Float32Array([-1, -1, 1, -1, 1, 1, -1, 1]), format: 'float32x2'},
            },
            indexBuffer: new Uint32Array([0, 1, 2, 0, 2, 3]),
        })
        this.mesh = new Mesh({geometry, shader})
    }

    advance(renderer: Renderer, deltaMilliseconds: number) {
        if (this.speed === 0) return
        this.seconds += (deltaMilliseconds / 1000) * this.speed
        this.render(renderer)
    }

    /** Renders the current frame and returns a copy of it at IMAGE_WIDTH x IMAGE_HEIGHT, for the pixelated background. */
    snapshot(renderer: Renderer): HTMLCanvasElement {
        this.render(renderer)
        const frame = renderer.extract.canvas(this.texture) as HTMLCanvasElement
        const canvas = document.createElement('canvas')
        canvas.width = IMAGE_WIDTH
        canvas.height = IMAGE_HEIGHT
        canvas.getContext('2d')!.drawImage(frame, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        return canvas
    }

    setSpeed(speed: number) {
        this.speed = speed
    }

    private render(renderer: Renderer) {
        this.uniforms.uniforms.time = this.seconds
        renderer.render({container: this.mesh, target: this.texture})
    }
}
