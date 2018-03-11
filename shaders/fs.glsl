#version 150 core

uniform sampler2D textImg;
in vec2 textCoords;

out vec4 fragColor;

void main(){
  fragColor = texture(textImg, textCoords);
}