#version 440 core

layout(location = 0) uniform float size;
layout(location = 1) uniform vec2 rotation;
layout(location = 2) uniform vec2 position;

layout(location = 4) uniform vec2 c_position;
layout(location = 5) uniform vec2 c_scale;

layout(location = 0) in vec2 pos;
layout(location = 1) in ivec2 textures;
layout(location = 2) in ivec2 gridPos;
layout(location = 3) in float lerp;
layout(location = 4) in float heat;

layout(location = 0) out vec3 uva;
layout(location = 1) out vec3 uvb;
layout(location = 2) out float p_lerp;
layout(location = 3) out float p_heat;

void main() {
	vec2 p = (pos - vec2(0.5) + gridPos) * size;
	gl_Position = vec4(((position + p * rotation.x + p.yx * rotation.y * vec2(-1, 1)) - c_position) * c_scale, 0, 1);
	uva = vec3(pos, textures.x);
	uvb = vec3(pos, textures.y);
	p_lerp = lerp;
	p_heat = clamp(heat, 0, 1);
}