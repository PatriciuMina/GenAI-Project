# UBB Events Logo

## 1. Concept

The UBB Events logo combines a calendar icon with a checkmark and the **UBB Events** wordmark.

The calendar represents event planning and scheduling, while the checkmark represents successful registration and participation.

The design is simple and recognizable, including at small sizes such as 32 px. It uses the application's existing primary color, `#3949ab`, to maintain visual consistency with the application theme.

The logo is an original design for this project and does not use the official UBB logo or trademark.

## 2. Design variants

Three SVG files are provided:

| Variant    | File                                         | Description                                                                   |
| ---------- | -------------------------------------------- | ----------------------------------------------------------------------------- |
| Light      | `docs/assets/logo/ubb-events.svg`            | Indigo calendar icon with dark wordmark, intended for light backgrounds       |
| On-primary | `docs/assets/logo/ubb-events-on-primary.svg` | White calendar icon and wordmark, intended for the primary-colored background |
| Favicon    | `frontend/public/logo.svg`                   | Calendar icon without wordmark, designed for browser tabs and small sizes     |

## 3. Color palette

| Element                      | Color     | Hex       |
| ---------------------------- | --------- | --------- |
| Primary icon                 | Indigo    | `#3949ab` |
| Light variant wordmark       | Dark gray | `#212121` |
| On-primary icon and wordmark | White     | `#ffffff` |
| On-primary background        | Indigo    | `#3949ab` |

The logo reuses the existing application primary color rather than introducing a new brand color.

## 4. Usage guidelines

- **App bar:** Use the light variant with the complete wordmark.
- **Drawer:** Use the icon-only version when space is limited.
- **Login page:** Use the light variant on light backgrounds or the on-primary variant on an indigo background.
- **Browser favicon:** Use `frontend/public/logo.svg`.

The on-primary variant should be displayed on the primary background (`#3949ab`) to maintain contrast.

The logo should not be stretched, rotated, recolored arbitrarily, or have its proportions changed.

## 5. Source files and implementation

The editable SVG logo sources are stored in:

`docs/assets/logo/`

The application logo component is located at:

`frontend/src/components/Logo.tsx`

The favicon is served from:

`frontend/public/logo.svg`

The favicon is referenced in `frontend/index.html`.

All logo assets use SVG format to remain scalable and sharp at different display sizes.

## 6. Design rationale

The calendar and checkmark communicate the main purpose of UBB Events: discovering, organizing, and participating in events.

The simple icon remains recognizable at small sizes, while the wordmark makes the application name clear.

Using the existing primary indigo color ensures consistency with the application's visual identity. The white variant allows the logo to be displayed on primary-colored backgrounds without losing visibility.

The result is
