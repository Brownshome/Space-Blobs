#version 440 core

layout(location = 0) out vec4 colour;

layout(location = 0) in vec2 pos;
layout(location = 1) in vec3 data;

float c(float x) {
	return clamp(1 + x * x * x * (x * (15 - 6 * x) - 10), 0, 1);
}

float lengthSq(vec2 v) {
	v = v * v;
	return v.x + v.y;
}

void main() {		
	colour = vec4(1, 0, 0, c(lengthSq(pos - vec2(data.x, data.y)) / (2 * data.z * data.z)));
}