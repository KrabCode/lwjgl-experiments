#version 460

uniform float time;
uniform vec2 resolution;
uniform vec2 mouse;
uniform sampler2D bb;
in vec2 uv;

out vec4 fragColor;

const float dA = 1.0;
const float dB = 0.45;
const float f = 0.03268;
const float k = 0.05907;
const float t = 1.;

vec2 getValuesFromColor(vec4 col){
    return vec2(col.r, col.b);
}

vec4 setValuesFromColor(vec2 vars){
    return vec4(vars.x, 0., vars.y, 1);
}

vec4 getPixel(vec2 pos){
    return texture2D(bb, 0.5+0.5*pos);
}

vec2 getPixelValues(vec2 pos){
    return getValuesFromColor(getPixel(pos));
}

vec2 getLaplacianAverageValues(vec2 p){
    vec2 step = vec2(1) / resolution.xy;
    return
    getPixelValues(p + step*vec2(+0, -1)) * 0.2 +
    getPixelValues(p + step*vec2(+0, +1)) * 0.2 +
    getPixelValues(p + step*vec2(-1, +0)) * 0.2 +
    getPixelValues(p + step*vec2(+1, +0)) * 0.2 +
    getPixelValues(p + step*vec2(+1, +1)) * 0.05 +
    getPixelValues(p + step*vec2(+1, -1)) * 0.05 +
    getPixelValues(p + step*vec2(-1, +1)) * 0.05 +
    getPixelValues(p + step*vec2(-1, -1)) * 0.05;
}

vec2 simulateReactionDiffusion(vec2 p){
    vec2 AB = getPixelValues(p);
    float A = AB.x;
    float B = AB.y;
    vec2 lapDiff = - AB + getLaplacianAverageValues(p);
    float ABsquared = A * B * B;
    float newA = A + (dA * lapDiff.x - ABsquared + f * (1.0 - A)) * t;
    float newB = B + (dB * lapDiff.y + ABsquared - ((k + f) * B)) * t;
    return clamp(vec2(newA, newB), vec2(0), vec2(1));
}


void main(){
    vec4 seedColor;
    vec2 newValues = simulateReactionDiffusion(uv);
    vec2 mouseNorm = mouse/resolution.xy;
    mouseNorm.y = 1.-mouseNorm.y;
    float mouseRadius = 0.06;

    newValues.y = max(newValues.y, smoothstep(mouseRadius, mouseRadius*0.5, length((0.5+0.5*uv)-mouseNorm)));
    fragColor = max(seedColor, setValuesFromColor(newValues));
}