#version 430 core

layout(binding = 0, location = 1) uniform sampler2D texture;

layout(location = 0) out vec4 colour;

layout(location = 0) in vec2 uv;

void main() {
	colour.a = texture(texture, uv).r;
	colour.rgb = vec3(1.0, 1.0, 1.0);
}