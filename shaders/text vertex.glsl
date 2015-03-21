#version 440 core

layout(location = 0) in vec2 position;
layout(location = 1) in int character;
layout(location = 2) in float screenSize;

layout(location = 0) out int c;
layout(location = 1) out float s;

void main() {
	gl_Position.xy = position;
	c = character;
	s = screenSize;
}