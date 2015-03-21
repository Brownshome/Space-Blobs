#version 440 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec3 colour;

layout(location = 0) out vec3 c;

void main() {
	gl_Position = vec4(position, 0, 1);
	c = colour;
}