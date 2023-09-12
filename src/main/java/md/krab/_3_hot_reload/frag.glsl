#version 460

uniform float time;
uniform vec2 res;
uniform sampler2D bb;
in vec2 uv;

out vec4 fragColor;

void main(){
    vec3 col = vec3(0);
    float r = 0.5;
    float t = time;
    vec2 rot = vec2(r*cos(t), r*sin(t));
    col += vec3(smoothstep(0.1, 0.05, length(uv-rot)));
    vec3 lastCol = texture2D(bb, 0.5+0.5*uv).rgb;
    lastCol = max(lastCol, col);
    lastCol -= 0.001;
    fragColor = vec4(lastCol,1.0);
}