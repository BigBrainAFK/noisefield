precision highp float;

varying float vAlpha;
uniform sampler2D uTexture;

void main() {
    vec4 texColor = texture2D(uTexture, gl_PointCoord);
    texColor.a = texColor.a * vAlpha;
    gl_FragColor = texColor;
}
