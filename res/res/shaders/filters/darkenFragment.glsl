#version 130

in vec2 pass_textureCoords;

out vec4 out_colour;

layout(binding = 0) uniform sampler2D originalTexture;

uniform float factor;

void main(void) {
    vec3 colour = texture(originalTexture, pass_textureCoords).rgb;
	out_colour = vec4(colour * factor, 1.0);
}