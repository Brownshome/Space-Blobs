#version 430

out vec4 colour;

layout(location = 0) in vec2 uv;

layout(binding = 0) uniform sampler2D tex;

void main() {
	colour = texture(tex, uv);
	
	colour.rgba = vec4(mix(vec4(0.245, 0.526, 0.7, 0.9), vec4(0.245, 0.7, 0.656, 0.8), float(colour.a > 0.55)) * float(colour.a > 0.5));
}