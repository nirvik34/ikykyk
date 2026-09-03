# Design System: cameo

## 🎨 1. Visual Style: Playful 3D Social / Candy UI

**cameo** is a playful social utility designed like a party game meets Snapchat sticker app. 

### Visual Keywords
**Playful · Social · Bold · Friendly · Fast · Dark · Candy-Colored · Gen-Z**

### What to Avoid
- ❌ Futuristic enterprise AI tropes
- ❌ Heavy glassmorphism & glows
- ❌ Cyberpunk neon purple gradients
- ❌ Overly technical UI jargon
- ❌ Corporate uppercase headers

---

## 🌈 2. Color Palette & Roles

| Role | Name | Hex | Usage |
| :--- | :--- | :--- | :--- |
| **Main Black** | Soft Black | `#080808` | Full app background; makes bright content pop. |
| **Card Dark** | Charcoal | `#242424` | Solid container cards & modal dialogs (no frosted glass). |
| **Primary Pink** | Hot Bubblegum | `#FF2490` | Signature brand CTA button background, Person 1 ring accent. |
| **Primary Blue** | Sky Blue | `#25A9E8` | Secondary accent, environment highlights, Person 2 ring accent. |
| **Primary Yellow** | Sunshine | `#FFD83D` | Playful decoration, Person 3 ring accent. |
| **Primary Green** | Lime / Mint | `#A8F02D` | Success feedback, Person 4 ring accent. |
| **Primary White** | White | `#FFFFFF` | High-contrast body text & button labels. |
| **Secondary Text** | Soft Gray | `#A8A8A8` | Captions, subtitles, timestamps. |

---

## 💗 3. Component Design & Microcopy Rules

### Lowercase Personality
All UI microcopy uses **lowercase text** for a casual, friendly social vibe:
- `"start"` instead of `"START"`
- `"create collage"` instead of `"CREATE COLLAGE"`
- `"save"` instead of `"SAVE TO GALLERY"`
- `"share"` instead of `"SHARE STORY"`

### Invisible AI (Natural Copy)
Replace robotic pipeline text with friendly, human expressions:
- `"finding everyone..."` instead of `"Executing face detection pipeline"`
- `"almost got it..."` instead of `"Extracting TFLite feature embeddings"`
- `"3 people found"` instead of `"Clusters resolved: 3 distinct identities"`

### Signature Buttons (Bubblegum Pink)
- **Primary CTA**: `#FF2490` background, `#FFFFFF` text, full pill shape (`CornerRadius 999dp`), height `56dp`.
- **Secondary Actions**: Charcoal `#242424` container with soft white/gray text.

### Person Cards & Avatar Rings
- **Cards**: Solid charcoal `#242424` rounded containers (`20dp` corner radius).
- **Rings**: Circular photo crops wrapped in rotating candy-colored rings:
  - Person 1 → `#FF2490` (Pink)
  - Person 2 → `#25A9E8` (Blue)
  - Person 3 → `#FFD83D` (Yellow)
  - Person 4 → `#A8F02D` (Green)
