#version 460

uniform int frame;
uniform float time;
uniform vec2 resolution;
uniform vec2 mouse;
uniform sampler2D bb;
in vec2 uv;

out vec4 fragColor;

/*
https://web.archive.org/web/20160418004149/http://freespace.virgin.net/hugo.elias/graphics/x_water.htm

*/

float bufferA(vec2 p){
    bool odd = mod(frame, 2) == 0;
    vec3 col = texture(bb, 0.5+0.5*p).rgb;
    return odd ? col.r : col.g;
}

float bufferB(vec2 p){
    bool odd = mod(frame, 2) == 0;
    vec3 col = texture(bb, 0.5+0.5*p).rgb;
    return odd ? col.g : col.r;
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if( x>w ) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    vec2 st = uv;

    if(max(abs(st.x), abs(st.y)) >= 0.999){
        fragColor = vec4(texture2D(bb, 0.5+0.5*st).rgb, 1.);
        return;
    }

    vec2 step = 1. / resolution.xy;
    float resultBufferA = bufferA(st);
    float resultBufferB = (
        bufferA(st+step*vec2(-1, +0)) +
        bufferA(st+step*vec2(+1, +0)) +
        bufferA(st+step*vec2(+0, -1)) +
        bufferA(st+step*vec2(+0, +1))) / 2.
        - bufferB(st);
    float damp = 0.9995;
    resultBufferB *= damp;

    vec2 mPos = (mouse.xy / resolution.xy);
    mPos.y = 1.-mPos.y;
    float d = length(mPos - (0.5+0.5*st));
    resultBufferB += 0.1*cubicPulse(0.05, 0.01, d);
    resultBufferB = clamp(resultBufferB, 0., 1.);
    resultBufferA = clamp(resultBufferA, 0., 1.);
    vec3 col;
    bool odd = mod(frame, 2) == 0;
    if(odd){
        col.rg = vec2(resultBufferA, resultBufferB);
    }else{
        col.rg = vec2(resultBufferB, resultBufferA);
    }
    fragColor = vec4(col, 1.);
}