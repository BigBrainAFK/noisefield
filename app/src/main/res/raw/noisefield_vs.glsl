precision highp float;

attribute vec3 aPosition;  // Particle position: x, y, z
attribute float aSpeed;    // Particle speed
attribute float aAlpha;    // Particle alpha

uniform mat4 uMVPMatrix;   // Model-View-Projection matrix
uniform float uScaleSize;  // Scale in relation to dpi

varying float vAlpha;      // Pass alpha to the fragment shader

void main() {
    // Set the position using the MVP matrix
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);

    // Set the point size (particle size)
    gl_PointSize = 1.0 + aSpeed * uScaleSize * 2500.0; // Scale size by density

    // Pass the alpha to the fragment shader
    vAlpha = aAlpha; // Alpha decreases with lifetime
}
