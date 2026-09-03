# cameo

**cameo** is a privacy-first, on-device Android application that analyzes portrait videos, identifies all unique faces across time, selects the sharpest representative shot for each person, and compiles them into a high-resolution, shareable collage.

---

## Technical Pipeline & Architecture

### 1. Frame Extraction (`VideoFrameExtractor`)
* Extracts video frames using `MediaMetadataRetriever` sampled at 160 ms intervals.
* Automatically handles orientation adjustments based on video metadata rotation flags.

### 2. Face Detection & Quality Assessment (`MLKitFaceDetector`)
* Utilizes Google ML Kit's Face Detection API in **Accurate Mode**.
* Calculates multi-dimensional quality metrics for every face detected:
  * **Frontality Score**: Evaluated from 3D head euler angles (yaw, pitch, roll).
  * **Eye Openness Score**: Combined probability of left and right eyes open.
  * **Smile Score**: Smiling probability multiplier.
  * **Sharpness Score**: Laplacian variance computed across the facial crop region.
  * **Edge Integrity**: Penalizes faces clipped near frame borders.

### 3. Continuous Appearance Tracking (`AppearanceSegmentTracker`)
* Tracks faces across consecutive frames using spatial **Intersection over Union (IoU)** bounding box tracking.
* Groups individual face detections into continuous appearance segments.

### 4. Embedding Generation (`TFLiteEmbeddingExtractor`)
* Crops portrait face regions with margins and resizes to $112 \times 112$ pixels.
* Normalizes pixel channels to $[-1.0, 1.0]$.
* Runs inference through **MobileFaceNet** (`mobilefacenet.tflite`) on TensorFlow Lite to produce a **192-dimensional vector**.
* Applies $L_2$ vector normalization to standardize feature magnitudes.

### 5. Identity Clustering (`IdentityClusterer`)
* Computes mean embedding vectors for each appearance segment.
* Performs hierarchical clustering using **Cosine Distance**:
  $$d(\mathbf{u}, \mathbf{v}) = 1 - \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\|_2 \|\mathbf{v}\|_2}$$
* **Co-Occurrence Guard**: Strictly prevents any two face tracks that appear simultaneously in the same video frame from being merged into the same cluster.
* **Calibrated Threshold**: Merges tracks with cosine distance $\le 0.28\text{f}$.

### 6. Representative Shot Selection (`RepresentativeShotSelector`)
* Evaluates all frames within a person's identity cluster.
* Selects the frame maximizing the weighted quality formula:
  $$\text{Score} = \text{Frontality} \times 0.35 + \text{EyesOpen} \times 0.30 + \text{Sharpness} \times 0.20 + \text{Smile} \times 0.10 + \text{BorderIntegrity} \times 0.05$$
* Generates a padded portrait crop optimized for collage placement.

### 7. Canvas Rendering & Sharing (`CollageRenderer` & `MediaStoreUtils`)
* Dynamically arranges individual identity portraits into a 1080x1920 9:16 story grid layout.
* Saves collages directly to device storage under `Pictures/cameo Collages/` via Android `MediaStore`.
* Integrates native Android share sheet via `FileProvider`.

---

## Distance Metric Rationale

Cosine distance measures vector orientation rather than magnitude, making it invariant to lighting changes and exposure differences. Combined with $L_2$ normalization, cosine distance provides consistent pairwise comparison bounds between $[0.0, 2.0]$.

The threshold of **0.28** (corresponding to cosine similarity $\ge 0.72$) was calibrated specifically for MobileFaceNet embeddings. Enforcing a strict frame co-occurrence constraint guarantees that multiple individuals appearing side-by-side in the same shot are never erroneously combined into a single identity.

---

## Getting Started & Build Instructions

### Prerequisites
* Android Studio Jellyfish (2023.3.1) or newer
* JDK 17+
* Android SDK 34

### Building the Project

```bash
# Clone the repository
git clone https://github.com/cameo/cameo.git
cd cameo

# Build the Debug APK
./gradlew assembleDebug
```

### Output APK Path
Upon a successful build, the compiled APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## License
Distributed under the Apache 2.0 License.
