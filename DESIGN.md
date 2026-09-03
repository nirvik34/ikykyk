# Design System: IYKYK (If You Know, You Know)

## 1. Visual Rationale & Brand Identity

**IYKYK** should feel like discovering hidden human relationships inside a video clip. 

The interface stays almost black and quiet; people and the generated collage are the visual focus. Machine intelligence is presented not as a flashy trick with purple gradients and sparkles, but as a precise, editorial tool. 

- **UI Chrome stays quiet:** High-contrast media, dark neutral backdrops, and subtle borders ensure video frames and face crops stand out.
- **Indigo means Machine-Generated:** Electric Indigo is used exclusively to denote system-identified data, active processing, and primary actions.
- **Emerald means Confirmed:** Emerald Green is reserved strictly for completed analysis, saved states, and verified appearance counts.
- **No Decorative Gradients:** Gradients are eliminated except during active processing, where motion indicates computational work.

---

## 2. Color Palette & Functional Roles

Instead of an overwhelming list of verbose tokens, IYKYK uses 5 functional colors:

| Token | Color Value | Purpose |
| :--- | :--- | :--- |
| **Dark Neutral Background** | `#0B0F19` | Main screen backdrop; keeps the focus entirely on video & face crops. |
| **Surface Layer** | `#161E2E` | Container cards for video drops and person tracks. |
| **Subtle Border** | `rgba(255, 255, 255, 0.08)` | Low-key separation between layout components. |
| **Machine Indigo** | `#6366F1` | Primary CTA, machine-detected person highlights, and active pipeline indicator. |
| **Confirmation Emerald** | `#34D399` | Verification pills, completed audit steps, and save confirmation. |
| **Primary Text** | `#F8FAFC` | High-readability titles and primary labels. |
| **Secondary Text** | `#94A3B8` | Timestamps, track details, and secondary captions. |

---

## 3. Typography & Hierarchy

To avoid visual noise, typography is locked to system defaults with 3 sizes and 2 weights:

- **Hero Title**: `20sp` Bold, `#F8FAFC` (Screen headers & main callouts)
- **Section / Card Label**: `15sp` SemiBold, `#F8FAFC`
- **Caption & Microcopy**: `12sp` Regular, `#94A3B8` (Timestamps, appearance metadata)

---

## 4. Quiet UI Components & Interactions

### Video Dropzone & Picker
- **State**: Dark neutral card (`#161E2E`) with a quiet border (`rgba(255, 255, 255, 0.08)`).
- **Instruction**: Direct and simple: "Select a video under 15s". No superfluous promotional copy.

### Pipeline Progress
- **Active State**: Linear Indigo progress bar with minimal step text (`"Detecting faces..."`, `"Clustering identities..."`).
- **Focus**: Motion indicates computational progress without distracting neon effects.

### Person Identity Tracks (`LazyRow`)
- **Structure**: Clean cards featuring circular face crops with a quiet Indigo ring.
- **Badge**: Small pill showing total continuous appearances (`e.g., "3 appearances"`).
- **Interaction**: Tapping opens the appearance track breakdown modal.

### Appearance Audit Breakdown
- **Modal**: Dark neutral sheet listing exact entry/exit timestamps (`e.g., 0.16s - 3.48s`).
- **Role**: Gives users full transparency into how the machine identified each person across video segments.

### High-Resolution Collage Canvas (9:16)
- **Hero Display**: Generated 9:16 portrait layout. Dark backdrop with high-contrast face crops.
- **Branding**: Minimal bottom mark: `IYKYK • PEOPLE COLLAGE`.

### Actions
- **Primary CTA ("Save to Gallery")**: Solid Machine Indigo (`#6366F1`) button.
- **Secondary CTA ("Share Story")**: Quiet outlined button with subtle border (`rgba(255, 255, 255, 0.12)`).

---

## 5. Layout & Spacing Rules

- **Screen Padding**: `16dp` standard margin across screen edges.
- **Component Spacing**: `12dp` vertical gaps between cards; `24dp` between primary sections.
- **Corner Radii**:
  - Cards & Canvas: `16dp` (Clean, subtle rounding).
  - Avatars & Badges: `CircleShape` / Full Pill.
