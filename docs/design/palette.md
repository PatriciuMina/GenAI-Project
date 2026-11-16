# UBB Events – Color Palette

## 1. Design direction

The UBB Events interface uses indigo as its primary brand
color. Indigo communicates trust, professionalism and
reliability while giving the application a modern identity.

The palette is designed for an event management platform
used by students, organizers and administrators.

## 2. Primary and secondary colors

| Name       | Hex     | Usage                               |
| ---------- | ------- | ----------------------------------- |
| Primary    | #3949ab | Main actions, links, brand identity |
| Secondary  | #00796b | Secondary actions and accents       |
| Background | #f5f6fa | Main application background         |
| Paper      | #ffffff | Cards, dialogs and surfaces         |

The primary color is also used by the UBB Events calendar
and checkmark logo.

## 3. Status colors

| Status  | Hex     | Usage                          |
| ------- | ------- | ------------------------------ |
| Success | #2e7d32 | Successful operations          |
| Warning | #8a4b00 | Warnings and attention         |
| Error   | #b71c1c | Errors and destructive actions |
| Info    | #01579b | Informational messages         |

## 4. Event status chip colors

| Event status        | Theme color | Meaning                  |
| ------------------- | ----------- | ------------------------ |
| DRAFT               | warning     | Event is being prepared  |
| PUBLISHED           | primary     | Event is published       |
| REGISTRATION_CLOSED | warning     | Registration is closed   |
| ONGOING             | info        | Event is taking place    |
| COMPLETED           | success     | Event has finished       |
| CANCELLED           | error       | Event has been cancelled |

## 5. Typography and contrast

| Foreground | Background | Contrast ratio |
| ---------- | ---------- | -------------- |
| #ffffff    | #3949ab    | 7.73:1         |
| #ffffff    | #00796b    | 5.33:1         |
| #ffffff    | #2e7d32    | 5.13:1         |
| #ffffff    | #8a4b00    | 6.80:1         |
| #ffffff    | #b71c1c    | 6.57:1         |
| #ffffff    | #01579b    | 7.40:1         |
| #212121    | #f5f6fa    | 14.91:1        |
| #212121    | #ffffff    | 16.10:1        |

All listed text/background combinations meet WCAG 2.1
AA requirements for normal text (minimum 4.5:1).

## 6. Accessibility

- White text is used on the colored primary and status
  backgrounds listed above.
- Dark text is used on light backgrounds.
- Status is communicated through text labels as well
  as color, so users do not need to distinguish colors
  alone.
- Components must use Material UI theme palette values
  rather than hard-coded colors.

## 7. Implementation

The palette is implemented in:

`frontend/src/theme.ts`

The application uses Material UI's `createTheme()`.

To change the brand palette in the future, update the
theme rather than individual components.
