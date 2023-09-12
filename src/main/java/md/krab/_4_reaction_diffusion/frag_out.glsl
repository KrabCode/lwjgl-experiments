#version 460

uniform sampler2D img;
in vec2 uv;

out vec4 fragColor;

void main(){
    float blue = clamp(texture2D(img, 0.5+0.5*uv).b * 2., 0., 1.);
    vec3 col = vec3(blue);
    fragColor = vec4(col.rgb,1.0);
}