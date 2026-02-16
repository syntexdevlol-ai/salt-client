#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BlurSampler;

uniform float EdgeStart;
uniform float EdgeEnd;
uniform float BlendStrength;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 base = texture(DiffuseSampler, texCoord);
    vec4 blurred = texture(BlurSampler, texCoord);

    // Blend blur only at left/right screen edges, keep center crisp.
    float leftMask = 1.0 - smoothstep(EdgeStart, EdgeEnd, texCoord.x);
    float rightMask = smoothstep(1.0 - EdgeEnd, 1.0 - EdgeStart, texCoord.x);
    float edgeMask = clamp(max(leftMask, rightMask), 0.0, 1.0);

    float amount = edgeMask * BlendStrength;
    fragColor = mix(base, blurred, amount);
}
