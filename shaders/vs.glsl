#version 150 core

uniform mat4 projection;
uniform mat4 translation;
uniform mat4 rotationX;
uniform mat4 rotationY;

uniform float viewDist;
uniform vec3 fogCol;

in vec3 pos;
in vec3 textureCoords;

out vec2 textCoords;

void main(){
  vec4 pos = projection * ((rotationX * rotationY) * translation) * vec4(pos, 1.);
  
  //float k = max(min(pos.a / viewDist, 1.), 0.);
  //color = col * (1. - k) + fogCol * k;
  
  textCoords = textureCoords.xy;
  gl_Position = pos;
}