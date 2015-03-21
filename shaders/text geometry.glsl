#version 430 core

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

layout(location = 0) in int[] character;
layout(location = 1) in float[] screenSize;

layout(location = 0) uniform int gridSize;

layout(location = 0) out vec2 uv;

void main() {
    vec2 pos = gl_in[0].gl_Position.xy;
    vec2 texture = vec2(character[0] % gridSize, character[0] / gridSize) / gridSize;
    gl_Position.zw = vec2(-1, 1);
    
    uv = texture + vec2(0, 1) / gridSize;
    gl_Position.xy = vec2(0, 0) * screenSize[0] + pos;
    EmitVertex();
        
    uv = texture + vec2(1, 1) / gridSize;
    gl_Position.xy = vec2(1, 0) * screenSize[0] + pos;
    EmitVertex();
    
    uv = texture + vec2(0, 0) / gridSize;
    gl_Position.xy = vec2(0, 1) * screenSize[0] + pos;
    EmitVertex();    
   
    uv = texture + vec2(1, 0) / gridSize;
    gl_Position.xy = vec2(1, 1) * screenSize[0] + pos;
    EmitVertex();
        
    EndPrimitive();
}