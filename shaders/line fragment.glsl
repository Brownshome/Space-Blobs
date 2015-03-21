#version 440 core

out vec3 colour;

layout(location = 0) in vec3 c;

void main() {
	colour = c;
}