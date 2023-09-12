#version 460

uniform sampler2D img;
in vec2 uv;

out vec4 fragColor;

void main(){
    vec3 col = texture2D(img, 0.5+0.5*uv).rgb;
    col.gb *= 1.;
    fragColor = vec4(col.rgb,1.0);
}