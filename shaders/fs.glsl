#version 150 core

uniform sampler2D textImg;
in vec2 textCoords;

out vec4 fragColor;

void main(){
  //fragColor = texture(textImg, textCoords);
  vec3 testCol = texture(textImg, textCoords).rgb;
  testCol = testCol * .5 + vec3(0., 1., 0.) * .5;
  fragColor = vec4(testCol, 1.);
}