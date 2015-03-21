#version 430 core

out vec4 colour;

layout(location = 0) in vec3 uva;
layout(location = 1) in vec3 uvb;
layout(location = 2) in float lerp;
layout(location = 3) in float heat;

layout(binding = 0) uniform sampler2DArray tex;
layout(binding = 1) uniform sampler1D hTex;

void main() {
	colour = mix(texture(tex, uva), texture(tex, uvb), lerp);
	vec4 v = texture(hTex, heat);
	colour.rgb = mix(colour.rgb, v.rgb * (colour.rgb * 2) + v.rgb * 0.1, v.a);
}