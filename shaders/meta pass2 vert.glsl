#version 440

layout(location = 0) in vec2 uvpos;

layout(location = 0) out vec2 uv;

void main() {
	gl_Position = vec4(uvpos * 2 - vec2(1), 0, 1);
	uv = uvpos;
}