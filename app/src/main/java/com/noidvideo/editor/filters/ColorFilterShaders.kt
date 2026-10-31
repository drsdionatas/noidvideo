package com.noidvideo.editor.filters

/**
 * Fragment shaders GLSL dos filtros do NoidVideo.
 */
object ColorFilterShaders {

    private const val HEADER = """
precision mediump float;
uniform sampler2D uTexSampler;
uniform vec2 uTexSize;
varying vec2 vTexSamplingCoord;
"""

    val CINEMATIC = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    c.rgb = (c.rgb - 0.5) * 1.15 + 0.5;
    c.r = mix(c.r, c.r * 1.05, 0.5);
    c.b = mix(c.b, c.b * 1.10, 0.5);
    c.rgb = mix(c.rgb, vec3(dot(c.rgb, vec3(0.299, 0.587, 0.114))), 0.08);
    gl_FragColor = c;
}
"""

    val VIVID = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    c.rgb = mix(vec3(l), c.rgb, 1.45);
    c.rgb = (c.rgb - 0.5) * 1.08 + 0.5;
    gl_FragColor = c;
}
"""

    val WARM = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    c.r = min(1.0, c.r * 1.12);
    c.g = min(1.0, c.g * 1.03);
    c.b = c.b * 0.92;
    gl_FragColor = c;
}
"""

    val COOL = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    c.r = c.r * 0.92;
    c.g = min(1.0, c.g * 1.02);
    c.b = min(1.0, c.b * 1.14);
    gl_FragColor = c;
}
"""

    val BW = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    l = (l - 0.5) * 1.18 + 0.5;
    gl_FragColor = vec4(vec3(l), c.a);
}
"""

    val VINTAGE = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    vec3 sepia = vec3(l * 1.07, l * 0.95, l * 0.78);
    c.rgb = mix(c.rgb, sepia, 0.65);
    vec2 d = vTexSamplingCoord - 0.5;
    float vig = 1.0 - dot(d, d) * 0.9;
    c.rgb *= vig;
    gl_FragColor = c;
}
"""

    val SNOW = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    c.rgb = mix(c.rgb, vec3(0.85, 0.92, 1.0), smoothstep(0.6, 1.0, l) * 0.35);
    c.rgb = (c.rgb - 0.5) * 1.05 + 0.5;
    gl_FragColor = c;
}
"""

    val WATER = HEADER + """
void main() {
    vec4 c = texture2D(uTexSampler, vTexSamplingCoord);
    c.g = min(1.0, c.g * 1.08);
    c.b = min(1.0, c.b * 1.18);
    c.r = c.r * 0.94;
    float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    c.rgb = mix(vec3(l), c.rgb, 1.2);
    gl_FragColor = c;
}
"""
}
