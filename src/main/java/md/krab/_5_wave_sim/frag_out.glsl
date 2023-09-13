#version 460

uniform sampler2D img;
uniform int frame;
in vec2 uv;

out vec4 fragColor;

void main(){
    vec2 st = 0.5+0.5*uv;
    vec3 col = texture2D(img, st).rgb;
    float payload;
    bool odd = mod(frame, 2) == 0;
    if(odd){
        payload = col.g;
    }else{
        payload = col.r;
    }

    col = vec3(pow(col.g, 1.0));
    fragColor = vec4(col.rgb, 1.0);
}