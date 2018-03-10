uniform mat4 translation;
uniform mat4 rotationX;
uniform mat4 rotationY;
uniform mat4 projection;

attribute vec3 pos;
attribute vec3 col;

varying vec3 color;

void main(){
  color = col;
  gl_Position = projection * ((rotationX * rotationY) * translation) * vec4(pos, 1.);
}