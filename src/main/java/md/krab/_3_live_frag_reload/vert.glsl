#version 460

layout (std430, binding = 0)  buffer ssbo {
    vec2[] verts;
};

out vec2 uv;

void main(){
    gl_Position = vec4(verts[gl_VertexID],0.,1.);
    uv = gl_Position.xy;
}