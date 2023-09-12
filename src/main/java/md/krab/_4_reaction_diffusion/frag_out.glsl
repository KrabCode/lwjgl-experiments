#version 460

uniform sampler2D img;
in vec2 uv;

out vec4 fragColor;

void main(){
    float blue = texture2D(img, 0.5+0.5*uv).b;
    vec3 col = clamp(vec3(blue)* 2., 0., 1.);;
    fragColor = vec4(col.rgb,1.0);
}