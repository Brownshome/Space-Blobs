#version 430

layout(location = 0) in vec2 pos;
layout(location = 1) in vec4 dims; //x0 x1, y0, y1
layout(location = 2) in vec3 data;

layout(location = 0) out vec2 f_pos;
layout(location = 1) out vec3 f_data;

layout(location = 0) uniform vec2 position;
layout(location = 1) uniform vec2 scale;

void main() {
	f_pos = pos * (dims.yw - dims.xz) + dims.xz;
	gl_Position = vec4((f_pos - position) * scale, 0, 1);
	f_data = data;
}