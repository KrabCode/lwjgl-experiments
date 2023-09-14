#version 460

uniform sampler2D img;
in vec2 uv;

out vec4 fragColor;

vec3 palette(float t)
{
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.00, 0.10, 0.20);
    return a + b*cos(6.28318*(c*t+d));
}


void main(){
    float blue = texture2D(img, 0.5+0.5*uv).b;
    vec3 col = palette(0.4-blue);
    fragColor = vec4(col.rgb,1.0);
}