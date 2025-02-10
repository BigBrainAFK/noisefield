attribute vec2 aPosition;
attribute vec3 aColor;

varying vec3 vColor;

void main() {
    gl_Position = vec4(aPosition, 0.0, 1.0);
    vColor = aColor;
}